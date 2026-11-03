package pl.edu.pg.eti.kask.aquarium.user.dto;

import pl.edu.pg.eti.kask.aquarium.user.entity.User;

import java.util.List;
import java.util.UUID;

/**
 * JSON representation of the user collection: only identifiers and logins.
 */
public record GetUsersResponse(List<Item> users) {

    public record Item(UUID id, String login) {
    }

    public static GetUsersResponse from(List<User> users) {
        return new GetUsersResponse(users.stream()
                .map(user -> new Item(user.getId(), user.getLogin()))
                .toList());
    }
}
