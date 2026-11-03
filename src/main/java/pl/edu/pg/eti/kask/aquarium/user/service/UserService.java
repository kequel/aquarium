package pl.edu.pg.eti.kask.aquarium.user.service;

import pl.edu.pg.eti.kask.aquarium.user.entity.User;
import pl.edu.pg.eti.kask.aquarium.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Business logic for users.
 */
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public List<User> findAll() {
        return repository.findAll();
    }

    public Optional<User> find(UUID id) {
        return repository.find(id);
    }

    public boolean exists(UUID id) {
        return repository.exists(id);
    }

    public void create(User user) {
        repository.create(user);
    }
}
