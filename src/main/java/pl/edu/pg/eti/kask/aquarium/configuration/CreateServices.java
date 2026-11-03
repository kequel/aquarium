package pl.edu.pg.eti.kask.aquarium.configuration;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import pl.edu.pg.eti.kask.aquarium.user.repository.UserRepository;
import pl.edu.pg.eti.kask.aquarium.user.service.AvatarService;
import pl.edu.pg.eti.kask.aquarium.user.service.UserService;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Creates application-scoped components and stores them as servlet context attributes.
 * Registered in web.xml.
 */
public class CreateServices implements ServletContextListener {

    public static final String USER_SERVICE = "userService";
    public static final String AVATAR_SERVICE = "avatarService";

    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServletContext context = event.getServletContext();

        UserRepository userRepository = new UserRepository();
        UserService userService = new UserService(userRepository);
        AvatarService avatarService = new AvatarService(avatarDirectory(context), userService);

        context.setAttribute(USER_SERVICE, userService);
        context.setAttribute(AVATAR_SERVICE, avatarService);
    }

    private static Path avatarDirectory(ServletContext context) {
        String value = context.getInitParameter("avatarDir");
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Context parameter 'avatarDir' is not set in web.xml");
        }
        Path path = Paths.get(value);
        if (!path.isAbsolute()) {
            path = Paths.get(System.getProperty("user.home")).resolve(path);
        }
        context.log("Avatar directory: " + path.toAbsolutePath());
        return path;
    }
}
