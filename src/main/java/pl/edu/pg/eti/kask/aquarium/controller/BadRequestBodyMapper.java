package pl.edu.pg.eti.kask.aquarium.controller;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * A request body that is not valid JSON (or has a wrong value, e.g. text instead of a number) is the client's
 * fault, so it gives 400 Bad Request instead of 500 Internal Server Error.
 */
@Provider
public class BadRequestBodyMapper implements ExceptionMapper<ProcessingException> {

    @Override
    public Response toResponse(ProcessingException exception) {
        return Response.status(Response.Status.BAD_REQUEST).build();
    }
}
