package pl.edu.pg.eti.kask.aquarium.aquarium.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import pl.edu.pg.eti.kask.aquarium.aquarium.entity.Aquarium;
import pl.edu.pg.eti.kask.aquarium.aquarium.repository.AquariumRepository;
import pl.edu.pg.eti.kask.aquarium.fish.repository.FishRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Business logic for aquariums (categories).
 */
@ApplicationScoped
public class AquariumService {

    private final AquariumRepository aquariumRepository;
    private final FishRepository fishRepository;

    @Inject
    public AquariumService(AquariumRepository aquariumRepository, FishRepository fishRepository) {
        this.aquariumRepository = aquariumRepository;
        this.fishRepository = fishRepository;
    }

    /** Required by CDI to create a proxy of a normal scoped bean. */
    protected AquariumService() {
        this(null, null);
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
     * Deletes the aquarium together with its fish. The relation is unidirectional, so there is no cascade
     * from the aquarium side and the fish have to be removed first.
     */
    @Transactional
    public void delete(UUID id) {
        aquariumRepository.find(id).ifPresent(aquarium -> {
            fishRepository.findAllByAquarium(aquarium).forEach(fishRepository::delete);
            aquariumRepository.delete(aquarium);
        });
    }
}
