package pl.edu.pg.eti.kask.idp.password;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.StringUtils;

/**
 * Reads a "password" grant request from the token endpoint:
 * {@code grant_type=password&username=...&password=...}. The client application itself has already been
 * authenticated (HTTP Basic with client id and secret).
 */
public class PasswordGrantAuthenticationConverter implements AuthenticationConverter {

    @Override
    public Authentication convert(HttpServletRequest request) {
        if (!PasswordGrantAuthenticationToken.PASSWORD.getValue().equals(request.getParameter("grant_type"))) {
            return null;
        }
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_REQUEST);
        }
        Authentication client = SecurityContextHolder.getContext().getAuthentication();
        return new PasswordGrantAuthenticationToken(username, password, client);
    }
}
