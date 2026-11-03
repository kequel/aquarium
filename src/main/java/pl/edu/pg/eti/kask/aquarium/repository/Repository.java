package pl.edu.pg.eti.kask.aquarium.repository;

import java.util.List;
import java.util.Optional;

/**
 * Basic data access operations shared by all repositories.
 *
 * @param <E> entity type
 * @param <K> primary key type
 */
public interface Repository<E, K> {

    Optional<E> find(K id);

    List<E> findAll();

    void create(E entity);

    void update(E entity);

    void delete(E entity);
}
