package pl.edu.pg.eti.kask.aquarium.security;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.security.enterprise.authentication.mechanism.http.OpenIdAuthenticationMechanismDefinition;
import jakarta.security.enterprise.authentication.mechanism.http.openid.ClaimsDefinition;

/**
 * Authentication with OpenID Connect, using the identity provider from the identity-provider project.
 * A user opening a secured page (see security-constraint in web.xml) is redirected to the provider's login form.
 * After logging in the provider redirects back to /callback with a code, the server exchanges it for tokens
 * and the user is logged in: the name is taken from the {@code sub} claim (login) and the roles from
 * the {@code groups} claim. Then {@link CallbackServlet} shows the list of aquariums.
 */
@OpenIdAuthenticationMechanismDefinition(
        providerURI = "http://localhost:9000",
        clientId = "aquarium",
        clientSecret = "secret",
        redirectURI = "http://localhost:8080/lab7/callback",
        scope = "openid",
        claimsDefinition = @ClaimsDefinition(callerNameClaim = "sub", callerGroupsClaim = "groups")
)
@ApplicationScoped
public class OpenIdConfig {
}
