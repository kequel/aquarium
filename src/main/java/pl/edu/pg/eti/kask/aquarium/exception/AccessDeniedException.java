package pl.edu.pg.eti.kask.aquarium.exception;

import jakarta.ejb.ApplicationException;

/**
 * The caller is logged in but is not allowed to do the operation, e.g. a user tries to change somebody else's fish.
 * An application exception: the EJB container passes it to the caller unchanged and rolls back the transaction.
 */
@ApplicationException(rollback = true)
public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String message) {
        super(message);
    }
}
