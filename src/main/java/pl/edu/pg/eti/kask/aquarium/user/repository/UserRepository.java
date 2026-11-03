package pl.edu.pg.eti.kask.aquarium.user.repository;

import pl.edu.pg.eti.kask.aquarium.user.entity.User;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory user storage. Shared by all requests, so it is thread safe
 * and always hands out copies of stored objects.
 */
public class UserRepository {

    private final Map<UUID, User> users = new ConcurrentHashMap<>();

    public List<User> findAll() {
        return users.values().stream()
                .sorted(Comparator.comparing(User::getLogin))
                .map(User::new)
                .toList();
    }

    public Optional<User> find(UUID id) {
        return Optional.ofNullable(users.get(id)).map(User::new);
    }

    public void create(User user) {
        if (users.putIfAbsent(user.getId(), new User(user)) != null) {
            throw new IllegalArgumentException("User with id " + user.getId() + " already exists");
        }
    }

    public boolean exists(UUID id) {
        return users.containsKey(id);
    }
}
