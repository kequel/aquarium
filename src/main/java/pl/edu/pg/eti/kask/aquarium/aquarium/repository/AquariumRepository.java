package pl.edu.pg.eti.kask.aquarium.aquarium.repository;

import jakarta.enterprise.context.RequestScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pl.edu.pg.eti.kask.aquarium.aquarium.entity.Aquarium;
import pl.edu.pg.eti.kask.aquarium.repository.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequestScoped
public class AquariumRepository implements Repository<Aquarium, UUID> {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<Aquarium> find(UUID id) {
        return Optional.ofNullable(em.find(Aquarium.class, id));
    }

    @Override
    public List<Aquarium> findAll() {
        return em.createQuery("select a from Aquarium a order by a.name", Aquarium.class).getResultList();
    }

    @Override
    public void create(Aquarium entity) {
        em.persist(entity);
    }

    @Override
    public void update(Aquarium entity) {
        em.merge(entity);
    }

    @Override
    public void delete(Aquarium entity) {
        em.remove(em.contains(entity) ? entity : em.merge(entity));
    }
}
