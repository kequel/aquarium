package pl.edu.pg.eti.kask.aquarium.user.repository;

import jakarta.enterprise.context.RequestScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pl.edu.pg.eti.kask.aquarium.user.entity.User;
import pl.edu.pg.eti.kask.aquarium.repository.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA based user repository. Request scoped, like the container managed EntityManager it uses.
 */
@RequestScoped
public class UserRepository implements Repository<User, UUID> {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<User> find(UUID id) {
        return Optional.ofNullable(em.find(User.class, id));
    }

    @Override
    public List<User> findAll() {
        return em.createQuery("select u from User u order by u.login", User.class).getResultList();
    }

    public Optional<User> findByLogin(String login) {
        return em.createQuery("select u from User u where u.login = :login", User.class)
                .setParameter("login", login)
                .getResultStream()
                .findFirst();
    }

    @Override
    public void create(User entity) {
        em.persist(entity);
    }

    @Override
    public void update(User entity) {
        em.merge(entity);
    }

    @Override
    public void delete(User entity) {
        em.remove(em.contains(entity) ? entity : em.merge(entity));
    }
}
