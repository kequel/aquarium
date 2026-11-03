package pl.edu.pg.eti.kask.aquarium.controller;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbException;
import jakarta.inject.Inject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import pl.edu.pg.eti.kask.aquarium.exception.AlreadyExistsException;
import pl.edu.pg.eti.kask.aquarium.exception.NotFoundException;
import pl.edu.pg.eti.kask.aquarium.user.dto.GetUserResponse;
import pl.edu.pg.eti.kask.aquarium.user.dto.GetUsersResponse;
import pl.edu.pg.eti.kask.aquarium.user.dto.PostUserRequest;
import pl.edu.pg.eti.kask.aquarium.user.entity.User;
import pl.edu.pg.eti.kask.aquarium.user.service.AvatarService;
import pl.edu.pg.eti.kask.aquarium.user.service.UserService;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * REST-like controller for users and avatars. Mapped only to /api/users, the rest of /api is handled by JAX-RS.
 */
@WebServlet(urlPatterns = {"/api/users", "/api/users/*"})
public class ApiServlet extends HttpServlet {

    private static final String UUID_REGEX = "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";

    private static final Pattern USERS = Pattern.compile("^/users/?$");
    private static final Pattern USER = Pattern.compile("^/users/(" + UUID_REGEX + ")/?$");
    private static final Pattern USER_AVATAR = Pattern.compile("^/users/(" + UUID_REGEX + ")/avatar/?$");

    private static final String PNG = "image/png";

    private final Jsonb jsonb = JsonbBuilder.create();

    /* The container creates servlets with the no-arg constructor, so CDI beans are injected into fields. */

    @Inject
    private UserService userService;

    @Inject
    private AvatarService avatarService;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = path(request);
        Matcher matcher;
        try {
            if (USERS.matcher(path).matches()) {
                writeJson(response, GetUsersResponse.from(userService.findAll()));
            } else if ((matcher = USER.matcher(path)).matches()) {
                UUID id = UUID.fromString(matcher.group(1));
                userService.find(id)
                        .map(GetUserResponse::from)
                        .ifPresentOrElse(
                                user -> writeJson(response, user),
                                () -> response.setStatus(HttpServletResponse.SC_NOT_FOUND));
            } else if ((matcher = USER_AVATAR.matcher(path)).matches()) {
                byte[] avatar = avatarService.find(UUID.fromString(matcher.group(1)));
                response.setContentType(PNG);
                response.setContentLength(avatar.length);
                response.getOutputStream().write(avatar);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (NotFoundException ex) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, ex.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (USERS.matcher(path(request)).matches()) {
            createUser(request, response);
            return;
        }
        Matcher matcher = USER_AVATAR.matcher(path(request));
        if (!matcher.matches()) {
            response.sendError(isKnown(request) ? HttpServletResponse.SC_METHOD_NOT_ALLOWED
                    : HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        if (!isPng(request)) {
            response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, "Expected " + PNG);
            return;
        }
        try {
            avatarService.create(UUID.fromString(matcher.group(1)), request.getInputStream());
            response.setHeader("Location", request.getRequestURL().toString());
            response.setStatus(HttpServletResponse.SC_CREATED);
        } catch (NotFoundException ex) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, ex.getMessage());
        } catch (AlreadyExistsException ex) {
            response.sendError(HttpServletResponse.SC_CONFLICT, ex.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Matcher matcher = USER_AVATAR.matcher(path(request));
        if (!matcher.matches()) {
            response.sendError(isKnown(request) ? HttpServletResponse.SC_METHOD_NOT_ALLOWED
                    : HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        if (!isPng(request)) {
            response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, "Expected " + PNG);
            return;
        }
        try {
            avatarService.update(UUID.fromString(matcher.group(1)), request.getInputStream());
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (NotFoundException ex) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, ex.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Matcher userMatcher = USER.matcher(path(request));
        if (userMatcher.matches()) {
            try {
                userService.delete(UUID.fromString(userMatcher.group(1)));
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            } catch (NotFoundException ex) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, ex.getMessage());
            }
            return;
        }
        Matcher matcher = USER_AVATAR.matcher(path(request));
        if (!matcher.matches()) {
            response.sendError(isKnown(request) ? HttpServletResponse.SC_METHOD_NOT_ALLOWED
                    : HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        try {
            avatarService.delete(UUID.fromString(matcher.group(1)));
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (NotFoundException ex) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, ex.getMessage());
        }
    }

    /**
     * Creates a user from a JSON body. Responds 201 with the new user's address in the Location header,
     * 400 for a malformed body, 409 if the login is taken.
     */
    private void createUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PostUserRequest body;
        try {
            body = jsonb.fromJson(request.getInputStream(), PostUserRequest.class);
        } catch (JsonbException ex) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Malformed JSON body");
            return;
        }
        if (body == null || body.login() == null || body.login().isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Field 'login' is required");
            return;
        }
        User user = body.toEntity();
        try {
            userService.create(user);
        } catch (AlreadyExistsException ex) {
            response.sendError(HttpServletResponse.SC_CONFLICT, ex.getMessage());
            return;
        }
        String collection = request.getRequestURL().toString().replaceAll("/$", "");
        response.setHeader("Location", collection + "/" + user.getId());
        response.setStatus(HttpServletResponse.SC_CREATED);
        writeJson(response, GetUserResponse.from(user));
    }

    /** Path after /api, e.g. /users/{id}/avatar. */
    private static String path(HttpServletRequest request) {
        String path = request.getServletPath().substring("/api".length());
        return request.getPathInfo() != null ? path + request.getPathInfo() : path;
    }

    /** Whether the path names an existing resource type (so a wrong method gives 405, not 404). */
    private static boolean isKnown(HttpServletRequest request) {
        String path = path(request);
        return USERS.matcher(path).matches() || USER.matcher(path).matches();
    }

    private static boolean isPng(HttpServletRequest request) {
        String type = request.getContentType();
        return type != null && type.toLowerCase().startsWith(PNG);
    }

    private void writeJson(HttpServletResponse response, Object body) {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try {
            response.getWriter().write(jsonb.toJson(body));
        } catch (IOException ex) {
            throw new java.io.UncheckedIOException(ex);
        }
    }

    @Override
    public void destroy() {
        try {
            jsonb.close();
        } catch (Exception ignored) {
            // nothing to do on shutdown
        }
    }
}
