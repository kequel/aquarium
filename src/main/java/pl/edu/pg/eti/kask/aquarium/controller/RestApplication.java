package pl.edu.pg.eti.kask.aquarium.controller;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * Turns on JAX-RS. All resources (classes annotated with {@code @Path}) are available under /api.
 * Users and avatars under /api/users are still handled by {@link ApiServlet}.
 */
@ApplicationPath("/api")
public class RestApplication extends Application {
}
