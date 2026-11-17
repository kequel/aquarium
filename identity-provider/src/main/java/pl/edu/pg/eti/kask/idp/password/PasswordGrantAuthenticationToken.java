package pl.edu.pg.eti.kask.idp.password;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationGrantAuthenticationToken;

import java.util.Map;

/**
 * Token request with the "password" grant: the client application sends the user's login and password
 * and gets an access token for that user.
 */
public class PasswordGrantAuthenticationToken extends OAuth2AuthorizationGrantAuthenticationToken {

    public static final AuthorizationGrantType PASSWORD = new AuthorizationGrantType("password");

    private final String username;

    private final String password;

    public PasswordGrantAuthenticationToken(String username, String password, Authentication clientPrincipal) {
        super(PASSWORD, clientPrincipal, Map.of());
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
