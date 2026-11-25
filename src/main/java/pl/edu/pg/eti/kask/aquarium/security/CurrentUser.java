package pl.edu.pg.eti.kask.aquarium.security;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.security.enterprise.SecurityContext;

import java.io.IOException;

/**
 * The logged-in user, for views: name shown in the header, role checks for showing buttons and logging out.
 */
@RequestScoped
@Named
public class CurrentUser {

    /** Logout page of the identity provider (ends the session there too, otherwise the user is logged in again). */
    private static final String PROVIDER_LOGOUT = "http://localhost:9000/logout";

    private final SecurityContext securityContext;

    @Inject
    public CurrentUser(SecurityContext securityContext) {
        this.securityContext = securityContext;
    }

    /** Required by CDI to create a proxy of a normal scoped bean. */
    protected CurrentUser() {
        this(null);
    }

    /** @return login of the logged-in user, null for an anonymous one */
    public String getName() {
        return securityContext.getCallerPrincipal() != null ? securityContext.getCallerPrincipal().getName() : null;
    }

    public boolean isAdmin() {
        return securityContext.isCallerInRole(UserRoles.ADMIN);
    }

    /**
     * Logs out from the application (the OpenID mechanism keeps the login in the session, so ending the session
     * is enough) and sends the user to the identity provider to log out there too.
     */
    public void logout() throws IOException {
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        context.invalidateSession();
        context.redirect(PROVIDER_LOGOUT);
    }
}
