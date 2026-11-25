package pl.edu.pg.eti.kask.aquarium.view;

import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Helpers for views that cannot be rendered, e.g. when the requested object does not exist.
 */
public final class HttpErrors {

    private HttpErrors() {
    }

    /**
     * Responds with 404 Not Found instead of rendering the current view.
     */
    public static void notFound() {
        sendError(HttpServletResponse.SC_NOT_FOUND, "Not found");
    }

    /**
     * Responds with 403 Forbidden instead of rendering the current view, e.g. somebody else's fish.
     */
    public static void forbidden() {
        sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
    }

    private static void sendError(int status, String message) {
        FacesContext context = FacesContext.getCurrentInstance();
        try {
            context.getExternalContext().responseSendError(status, message);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        context.responseComplete();
    }
}
