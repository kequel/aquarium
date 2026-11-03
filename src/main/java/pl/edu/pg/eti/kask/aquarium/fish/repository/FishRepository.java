package pl.edu.pg.eti.kask.aquarium.fish.repository;

import jakarta.enterprise.context.RequestScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import pl.edu.pg.eti.kask.aquarium.aquarium.entity.Aquarium;
import pl.edu.pg.eti.kask.aquarium.fish.entity.Fish;
import pl.edu.pg.eti.kask.aquarium.repository.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequestScoped
public class FishRepository implements Repository<Fish, UUID> {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<Fish> find(UUID id) {
        return Optional.ofNullable(em.find(Fish.class, id));
    }

    @Override
    public List<Fish> findAll() {
        return em.createQuery("select f from Fish f order by f.species", Fish.class).getResultList();
    }

    public List<Fish> findAllByAquarium(Aquarium aquarium) {
        return em.createQuery("select f from Fish f where f.aquarium = :aquarium order by f.species", Fish.class)
                .setParameter("aquarium", aquarium)
                .getResultList();
    }

    @Override
    public void create(Fish entity) {
        em.persist(entity);
    }

    @Override
    public void update(Fish entity) {
        em.merge(entity);
    }

    @Override
    public void delete(Fish entity) {
        em.remove(em.contains(entity) ? entity : em.merge(entity));
    }
}
