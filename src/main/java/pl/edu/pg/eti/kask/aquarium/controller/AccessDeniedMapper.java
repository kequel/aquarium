package pl.edu.pg.eti.kask.aquarium.controller;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.ExceptionMapper;

/**
 * Common answer when an operation is not allowed:
 * <ul>
 *     <li>401 Unauthorized if the caller is not logged in (no token), with {@code WWW-Authenticate: Bearer}
 *     telling the client to send a token,</li>
 *     <li>403 Forbidden if the caller is logged in but does not have the required role or is not the owner.</li>
 * </ul>
 * Subclasses choose which exception is translated.
 */
public abstract class AccessDeniedMapper<E extends Throwable> implements ExceptionMapper<E> {

    @Context
    private SecurityContext securityContext;

    @Override
    public Response toResponse(E exception) {
        if (securityContext.getUserPrincipal() == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .header(HttpHeaders.WWW_AUTHENTICATE, "Bearer")
                    .build();
        }
        return Response.status(Response.Status.FORBIDDEN).build();
    }
}
