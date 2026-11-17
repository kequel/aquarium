package pl.edu.pg.eti.kask.aquarium.controller;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

import java.util.Map;

/**
 * Turns on JAX-RS. All resources (classes annotated with {@code @Path}) are available under /api.
 * Users and avatars under /api/users are still handled by {@link ApiServlet}.
 */
@ApplicationPath("/api")
public class RestApplication extends Application {

    /**
     * Turns on checking of {@code @RolesAllowed} on controller methods (a Jersey feature, off by default).
     */
    @Override
    public Map<String, Object> getProperties() {
        return Map.of("jersey.config.server.provider.classnames",
                "org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature");
    }
}
