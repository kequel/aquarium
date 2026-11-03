package pl.edu.pg.eti.kask.aquarium.user.service;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.servlet.ServletContext;
import pl.edu.pg.eti.kask.aquarium.exception.AlreadyExistsException;
import pl.edu.pg.eti.kask.aquarium.exception.NotFoundException;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Stores user avatars as PNG files named {@code <userId>.png} in the configured directory.
 * At most one avatar per user, no history.
 */
@ApplicationScoped
public class AvatarService {

    private final UserService userService;
    private final ServletContext servletContext;

    private Path directory;

    @Inject
    public AvatarService(UserService userService, ServletContext servletContext) {
        this.userService = userService;
        this.servletContext = servletContext;
    }

    /** Required by CDI to create a proxy of a normal scoped bean. */
    protected AvatarService() {
        this(null, null);
    }

    /**
     * Reads the directory from the {@code avatarDir} context parameter in web.xml. An absolute path is used
     * as is, a relative one is resolved against the user's home directory.
     */
    @PostConstruct
    void init() {
        String value = servletContext.getInitParameter("avatarDir");
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Context parameter 'avatarDir' is not set in web.xml");
        }
        Path path = Paths.get(value);
        if (!path.isAbsolute()) {
            path = Paths.get(System.getProperty("user.home")).resolve(path);
        }
        directory = path;
        servletContext.log("Avatar directory: " + directory.toAbsolutePath());
        try {
            Files.createDirectories(directory);
        } catch (IOException ex) {
            throw new UncheckedIOException("Cannot create avatar directory " + directory, ex);
        }
    }

    /**
     * @throws NotFoundException if the user or their avatar does not exist
     */
    public byte[] find(UUID userId) {
        requireUser(userId);
        try {
            return Files.readAllBytes(path(userId));
        } catch (NoSuchFileException ex) {
            throw new NotFoundException("Avatar of user " + userId + " not found");
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    /**
     * Uploads a new avatar.
     *
     * @throws NotFoundException      if the user does not exist
     * @throws AlreadyExistsException if the user already has an avatar
     */
    public void create(UUID userId, InputStream image) {
        requireUser(userId);
        try {
            // Without REPLACE_EXISTING the copy fails if the file is already there.
            Files.copy(image, path(userId));
        } catch (FileAlreadyExistsException ex) {
            throw new AlreadyExistsException("Avatar of user " + userId + " already exists");
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    /**
     * Overwrites an existing avatar.
     *
     * @throws NotFoundException if the user or their avatar does not exist
     */
    public void update(UUID userId, InputStream image) {
        requireUser(userId);
        Path target = path(userId);
        if (!Files.exists(target)) {
            throw new NotFoundException("Avatar of user " + userId + " not found");
        }
        try {
            Files.copy(image, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    /**
     * @throws NotFoundException if the user or their avatar does not exist
     */
    public void delete(UUID userId) {
        requireUser(userId);
        try {
            if (!Files.deleteIfExists(path(userId))) {
                throw new NotFoundException("Avatar of user " + userId + " not found");
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private void requireUser(UUID userId) {
        if (!userService.exists(userId)) {
            throw new NotFoundException("User " + userId + " not found");
        }
    }

    private Path path(UUID userId) {
        return directory.resolve(userId + ".png");
    }
}
