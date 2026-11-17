package pl.edu.pg.eti.kask.aquarium.security;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.Json;
import jakarta.json.JsonException;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.math.BigInteger;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.RSAPublicKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Checks JWT tokens issued by the identity provider. A token has three parts separated by dots:
 * header, claims (both JSON, base64url encoded) and an RS256 signature. The token is valid when the signature
 * matches one of the provider's public keys (published at its JWKS address), it was issued by the expected
 * provider and it has not expired.
 */
@ApplicationScoped
public class JwtVerifier {

    /** Public keys of the identity provider by their identifier ({@code kid}), downloaded when first needed. */
    private final Map<String, PublicKey> keys = new ConcurrentHashMap<>();

    /**
     * @return claims of the token
     * @throws InvalidTokenException if the token is malformed, forged, from another issuer or expired
     */
    public JsonObject verify(String token, String issuer, String jwksUri) throws InvalidTokenException {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new InvalidTokenException("Malformed token");
        }
        JsonObject header = decodeJson(parts[0]);
        if (!"RS256".equals(header.getString("alg", null)) || header.getString("kid", null) == null) {
            throw new InvalidTokenException("Unsupported token signature");
        }
        PublicKey key = key(header.getString("kid"), jwksUri);
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(key);
            signature.update((parts[0] + "." + parts[1]).getBytes(StandardCharsets.US_ASCII));
            if (!signature.verify(Base64.getUrlDecoder().decode(parts[2]))) {
                throw new InvalidTokenException("Invalid token signature");
            }
        } catch (GeneralSecurityException | IllegalArgumentException ex) {
            throw new InvalidTokenException("Invalid token signature");
        }
        JsonObject claims = decodeJson(parts[1]);
        if (!issuer.equals(claims.getString("iss", null))) {
            throw new InvalidTokenException("Token issued by another provider");
        }
        if (!claims.containsKey("exp")
                || Instant.now().getEpochSecond() >= claims.getJsonNumber("exp").longValue()) {
            throw new InvalidTokenException("Token expired");
        }
        return claims;
    }

    private PublicKey key(String kid, String jwksUri) throws InvalidTokenException {
        PublicKey key = keys.get(kid);
        if (key == null) {
            // Unknown key: the provider may have been restarted with a new key, so download the keys again.
            loadKeys(jwksUri);
            key = keys.get(kid);
        }
        if (key == null) {
            throw new InvalidTokenException("Token signed with an unknown key");
        }
        return key;
    }

    private void loadKeys(String jwksUri) throws InvalidTokenException {
        try (InputStream in = URI.create(jwksUri).toURL().openStream(); JsonReader reader = Json.createReader(in)) {
            for (JsonValue value : reader.readObject().getJsonArray("keys")) {
                JsonObject jwk = value.asJsonObject();
                if ("RSA".equals(jwk.getString("kty", null))) {
                    BigInteger modulus = new BigInteger(1, Base64.getUrlDecoder().decode(jwk.getString("n")));
                    BigInteger exponent = new BigInteger(1, Base64.getUrlDecoder().decode(jwk.getString("e")));
                    keys.put(jwk.getString("kid"),
                            KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(modulus, exponent)));
                }
            }
        } catch (IOException | JsonException | GeneralSecurityException ex) {
            throw new InvalidTokenException("Cannot download keys of the identity provider: " + ex.getMessage());
        }
    }

    private static JsonObject decodeJson(String part) throws InvalidTokenException {
        try (JsonReader reader = Json.createReader(new StringReader(
                new String(Base64.getUrlDecoder().decode(part), StandardCharsets.UTF_8)))) {
            return reader.readObject();
        } catch (IllegalArgumentException | JsonException ex) {
            throw new InvalidTokenException("Malformed token");
        }
    }

    public static class InvalidTokenException extends Exception {

        public InvalidTokenException(String message) {
            super(message);
        }
    }
}
