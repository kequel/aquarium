package pl.edu.pg.eti.kask.aquarium.fish.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import pl.edu.pg.eti.kask.aquarium.aquarium.repository.AquariumRepository;
import pl.edu.pg.eti.kask.aquarium.fish.entity.Fish;
import pl.edu.pg.eti.kask.aquarium.fish.repository.FishRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Business logic for fish (elements).
 */
@ApplicationScoped
public class FishService {

    private final FishRepository fishRepository;
    private final AquariumRepository aquariumRepository;

    @Inject
    public FishService(FishRepository fishRepository, AquariumRepository aquariumRepository) {
        this.fishRepository = fishRepository;
        this.aquariumRepository = aquariumRepository;
    }

    /** Required by CDI to create a proxy of a normal scoped bean. */
    protected FishService() {
        this(null, null);
    }

    public List<Fish> findAll() {
        return fishRepository.findAll();
    }

    public Optional<Fish> find(UUID id) {
        return fishRepository.find(id);
    }

    /**
     * @return fish living in the given aquarium, empty if the aquarium does not exist
     */
    public Optional<List<Fish>> findAllByAquarium(UUID aquariumId) {
        return aquariumRepository.find(aquariumId).map(fishRepository::findAllByAquarium);
    }

    @Transactional
    public void create(Fish fish) {
        fishRepository.create(fish);
    }

    @Transactional
    public void update(Fish fish) {
        fishRepository.update(fish);
    }

    @Transactional
    public void delete(UUID id) {
        fishRepository.find(id).ifPresent(fishRepository::delete);
    }
}
