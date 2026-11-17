package pl.edu.pg.eti.kask.aquarium.exception;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class AlreadyExistsException extends RuntimeException {

    public AlreadyExistsException(String message) {
        super(message);
    }
}
