package pl.edu.pg.eti.kask.aquarium.controller;

import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.ext.Provider;

/**
 * {@code @RolesAllowed} on a controller method rejected the call (JAX-RS always reports it as 403, also for
 * a caller without a token, so it is corrected here to 401 or 403).
 */
@Provider
public class RolesAllowedMapper extends AccessDeniedMapper<ForbiddenException> {
}
