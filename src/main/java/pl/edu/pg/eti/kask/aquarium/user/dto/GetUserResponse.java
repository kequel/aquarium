package pl.edu.pg.eti.kask.aquarium.user.dto;

import pl.edu.pg.eti.kask.aquarium.user.entity.Role;
import pl.edu.pg.eti.kask.aquarium.user.entity.User;

import java.time.LocalDate;
import java.util.UUID;

/**
 * JSON representation of a single user.
 */
public record GetUserResponse(UUID id, String login, String email, LocalDate birthDate, Role role) {

    public static GetUserResponse from(User user) {
        return new GetUserResponse(user.getId(), user.getLogin(), user.getEmail(), user.getBirthDate(),
                user.getRole());
    }
}
