package pl.edu.pg.eti.kask.aquarium.configuration;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import pl.edu.pg.eti.kask.aquarium.user.entity.Role;
import pl.edu.pg.eti.kask.aquarium.user.entity.User;
import pl.edu.pg.eti.kask.aquarium.user.service.UserService;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Loads test users on startup. Fixed UUIDs keep request.http usable between restarts.
 * Registered in web.xml after {@link CreateServices}.
 */
public class InitializedData implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent event) {
        UserService userService = (UserService) event.getServletContext()
                .getAttribute(CreateServices.USER_SERVICE);

        userService.create(new User(UUID.fromString("c4804e0f-769e-4ab9-9ebe-0578fb4f00a6"),
                "admin", "admin@example.com", LocalDate.of(1990, 10, 21), Role.ADMIN));
        userService.create(new User(UUID.fromString("81e1c2a9-7f57-439b-b53d-6db88b071e4e"),
                "kevin", "kevin@example.com", LocalDate.of(2001, 1, 16), Role.USER));
        userService.create(new User(UUID.fromString("ed6cfb2a-cad7-47dd-9b56-9d1e3c7a4197"),
                "alice", "alice@example.com", LocalDate.of(2002, 3, 19), Role.USER));
        userService.create(new User(UUID.fromString("f08ef7e3-7f2a-4378-b1fb-2922d730c70d"),
                "bob", "bob@example.com", LocalDate.of(1999, 7, 4), Role.USER));
    }
}
