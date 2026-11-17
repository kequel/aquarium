package pl.edu.pg.eti.kask.aquarium.security;

/**
 * Role names used in {@code @RolesAllowed}. They are the same as the values of
 * {@link pl.edu.pg.eti.kask.aquarium.user.entity.Role} stored in the database and put by the identity provider
 * into the {@code groups} claim of the token.
 */
public final class UserRoles {

    public static final String ADMIN = "ADMIN";

    public static final String USER = "USER";

    private UserRoles() {
    }
}
