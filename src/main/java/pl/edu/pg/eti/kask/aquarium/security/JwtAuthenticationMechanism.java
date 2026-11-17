package pl.edu.pg.eti.kask.aquarium.security;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.security.enterprise.AuthenticationStatus;
import jakarta.security.enterprise.authentication.mechanism.http.HttpAuthenticationMechanism;
import jakarta.security.enterprise.authentication.mechanism.http.HttpMessageContext;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Own Jakarta Security authentication mechanism: the caller is identified by a JWT token from the identity
 * provider, sent in the {@code Authorization: Bearer <token>} header. Called by the server for every request.
 * <ul>
 *     <li>no token: the request continues as anonymous, secured operations answer 401,</li>
 *     <li>valid token: the caller is logged in as the token's subject ({@code sub}, the login) with the roles
 *     from the {@code groups} claim,</li>
 *     <li>invalid or expired token: 401 Unauthorized.</li>
 * </ul>
 * The address of the provider is configured in web.xml ({@code jwtIssuer}, {@code jwtJwksUri}).
 */
@ApplicationScoped
public class JwtAuthenticationMechanism implements HttpAuthenticationMechanism {

    private static final Logger LOG = Logger.getLogger(JwtAuthenticationMechanism.class.getName());

    private static final String BEARER = "Bearer ";

    private final JwtVerifier verifier;

    @Inject
    public JwtAuthenticationMechanism(JwtVerifier verifier) {
        this.verifier = verifier;
    }

    /** Required by CDI to create a proxy of a normal scoped bean. */
    protected JwtAuthenticationMechanism() {
        this(null);
    }

    @Override
    public AuthenticationStatus validateRequest(HttpServletRequest request, HttpServletResponse response,
                                                HttpMessageContext context) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith(BEARER)) {
            return context.doNothing();
        }
        ServletContext servletContext = request.getServletContext();
        try {
            JsonObject claims = verifier.verify(header.substring(BEARER.length()).trim(),
                    servletContext.getInitParameter("jwtIssuer"), servletContext.getInitParameter("jwtJwksUri"));
            Set<String> groups = claims.containsKey("groups")
                    ? claims.getJsonArray("groups").getValuesAs(JsonString.class).stream()
                    .map(JsonString::getString)
                    .collect(Collectors.toSet())
                    : Set.of();
            return context.notifyContainerAboutLogin(claims.getString("sub"), groups);
        } catch (JwtVerifier.InvalidTokenException ex) {
            LOG.info("Rejected token: " + ex.getMessage());
            response.setHeader("WWW-Authenticate", "Bearer error=\"invalid_token\"");
            return context.responseUnauthorized();
        }
    }
}
