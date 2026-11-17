package pl.edu.pg.eti.kask.aquarium.controller;

import jakarta.ejb.AccessLocalException;
import jakarta.ejb.EJBAccessException;
import jakarta.ejb.EJBException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@code @RolesAllowed} on a service (EJB) method rejected the call. Depending on the server it is reported as
 * {@link EJBAccessException} or {@link AccessLocalException} (Payara); other EJB errors stay 500.
 */
@Provider
public class EjbAccessMapper extends AccessDeniedMapper<EJBException> {

    private static final Logger LOG = Logger.getLogger(EjbAccessMapper.class.getName());

    @Override
    public Response toResponse(EJBException exception) {
        if (exception instanceof EJBAccessException || exception instanceof AccessLocalException) {
            return super.toResponse(exception);
        }
        LOG.log(Level.SEVERE, "Service error", exception);
        return Response.serverError().build();
    }
}
