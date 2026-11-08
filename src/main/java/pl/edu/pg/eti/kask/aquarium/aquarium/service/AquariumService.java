package pl.edu.pg.eti.kask.aquarium.aquarium.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import pl.edu.pg.eti.kask.aquarium.aquarium.entity.Aquarium;
import pl.edu.pg.eti.kask.aquarium.aquarium.repository.AquariumRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Business logic for aquariums (categories).
 */
@ApplicationScoped
public class AquariumService {

    private final AquariumRepository aquariumRepository;

    @Inject
    public AquariumService(AquariumRepository aquariumRepository) {
        this.aquariumRepository = aquariumRepository;
    }

    /** Required by CDI to create a proxy of a normal scoped bean. */
    protected AquariumService() {
        this(null);
    }

    public List<Aquarium> findAll() {
        return aquariumRepository.findAll();
    }

    public Optional<Aquarium> find(UUID id) {
        return aquariumRepository.find(id);
    }

    @Transactional
    public void create(Aquarium aquarium) {
        aquariumRepository.create(aquarium);
    }

    @Transactional
    public void update(Aquarium aquarium) {
        aquariumRepository.update(aquarium);
    }

    /**
     * Deletes the aquarium. Its fish are deleted automatically (cascade REMOVE on {@code Aquarium.fish}),
     * here they are only removed from their owners' fish lists, so the other side of the relation stays correct.
     */
    @Transactional
    public void delete(UUID id) {
        aquariumRepository.find(id).ifPresent(aquarium -> {
            aquarium.getFish().stream()
                    .filter(fish -> fish.getUser() != null)
                    .forEach(fish -> fish.getUser().getFish().remove(fish));
            aquariumRepository.delete(aquarium);
        });
    }
}
