package pl.edu.pg.eti.kask.aquarium.user.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.mindrot.jbcrypt.BCrypt;
import pl.edu.pg.eti.kask.aquarium.exception.AlreadyExistsException;
import pl.edu.pg.eti.kask.aquarium.exception.NotFoundException;
import pl.edu.pg.eti.kask.aquarium.user.entity.User;
import pl.edu.pg.eti.kask.aquarium.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Business logic for users. Stateless, so application scoped.
 */
@ApplicationScoped
public class UserService {

    private final UserRepository repository;

    @Inject
    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    /** Required by CDI to create a proxy of a normal scoped bean. */
    protected UserService() {
        this(null);
    }

    public List<User> findAll() {
        return repository.findAll();
    }

    public Optional<User> find(UUID id) {
        return repository.find(id);
    }

    public boolean exists(UUID id) {
        return repository.find(id).isPresent();
    }

    /**
     * Registers a user. Not secured: everybody can register. The password is given in plain text and only its
     * BCrypt hash is stored.
     *
     * @throws AlreadyExistsException if a user with the same login already exists
     */
    @Transactional
    public void create(User user) {
        if (repository.findByLogin(user.getLogin()).isPresent()) {
            throw new AlreadyExistsException("User with login " + user.getLogin() + " already exists");
        }
        user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        repository.create(user);
    }

    /**
     * Deletes the user. The user's fish stay in their aquariums, they just lose their owner.
     *
     * @throws NotFoundException if the user does not exist
     */
    @Transactional
    public void delete(UUID id) {
        User user = repository.find(id)
                .orElseThrow(() -> new NotFoundException("User " + id + " not found"));
        user.getFish().forEach(fish -> fish.setUser(null));
        user.getFish().clear();
        repository.delete(user);
    }
}
