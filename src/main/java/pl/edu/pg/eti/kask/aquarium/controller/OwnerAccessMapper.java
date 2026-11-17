package pl.edu.pg.eti.kask.aquarium.controller;

import jakarta.ws.rs.ext.Provider;
import pl.edu.pg.eti.kask.aquarium.exception.AccessDeniedException;

/**
 * A service rejected the call because the caller is not the owner of the object.
 */
@Provider
public class OwnerAccessMapper extends AccessDeniedMapper<AccessDeniedException> {
}
