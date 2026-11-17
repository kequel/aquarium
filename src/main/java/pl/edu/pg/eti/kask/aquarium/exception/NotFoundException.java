package pl.edu.pg.eti.kask.aquarium.exception;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }
}
