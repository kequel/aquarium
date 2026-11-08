package pl.edu.pg.eti.kask.aquarium.fish.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import pl.edu.pg.eti.kask.aquarium.aquarium.entity.Aquarium;
import pl.edu.pg.eti.kask.aquarium.aquarium.repository.AquariumRepository;
import pl.edu.pg.eti.kask.aquarium.exception.NotFoundException;
import pl.edu.pg.eti.kask.aquarium.fish.entity.Fish;
import pl.edu.pg.eti.kask.aquarium.fish.repository.FishRepository;
import pl.edu.pg.eti.kask.aquarium.user.entity.User;
import pl.edu.pg.eti.kask.aquarium.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Business logic for fish (elements). Every change updates both sides of the relations: the fish
 * (owning side, saved in the database) and the lists in its aquarium and user (kept in memory and in the
 * persistence provider's cache, so without the update they would show stale data).
 */
@ApplicationScoped
public class FishService {

    private final FishRepository fishRepository;
    private final AquariumRepository aquariumRepository;
    private final UserRepository userRepository;

    @Inject
    public FishService(FishRepository fishRepository, AquariumRepository aquariumRepository,
                       UserRepository userRepository) {
        this.fishRepository = fishRepository;
        this.aquariumRepository = aquariumRepository;
        this.userRepository = userRepository;
    }

    /** Required by CDI to create a proxy of a normal scoped bean. */
    protected FishService() {
        this(null, null, null);
    }

    public List<Fish> findAll() {
        return fishRepository.findAll();
    }

    public Optional<Fish> find(UUID id) {
        return fishRepository.find(id);
    }

    /**
     * @return the fish, empty if it does not exist or lives in another aquarium
     */
    public Optional<Fish> find(UUID aquariumId, UUID id) {
        return fishRepository.find(id).filter(fish -> fish.getAquarium().getId().equals(aquariumId));
    }

    /**
     * @return fish living in the given aquarium, taken from the aquarium itself (no separate query),
     * empty if the aquarium does not exist
     */
    public Optional<List<Fish>> findAllByAquarium(UUID aquariumId) {
        return aquariumRepository.find(aquariumId).map(Aquarium::getFish);
    }

    /**
     * Saves a new fish and adds it to the fish lists of its aquarium and user.
     *
     * @throws NotFoundException if the aquarium or the user does not exist
     */
    @Transactional
    public void create(Fish fish) {
        Aquarium aquarium = aquariumRepository.find(fish.getAquarium().getId())
                .orElseThrow(() -> new NotFoundException("Aquarium " + fish.getAquarium().getId() + " not found"));
        fish.setAquarium(aquarium);
        aquarium.getFish().add(fish);
        if (fish.getUser() != null) {
            User user = userRepository.find(fish.getUser().getId())
                    .orElseThrow(() -> new NotFoundException("User " + fish.getUser().getId() + " not found"));
            fish.setUser(user);
            user.getFish().add(fish);
        }
        fishRepository.create(fish);
    }

    /**
     * Saves changes of a fish. If it was moved to another aquarium, it is also moved between the aquariums'
     * fish lists.
     *
     * @throws NotFoundException if the fish or its new aquarium does not exist
     */
    @Transactional
    public void update(Fish fish) {
        Fish current = fishRepository.find(fish.getId())
                .orElseThrow(() -> new NotFoundException("Fish " + fish.getId() + " not found"));
        if (!current.getAquarium().equals(fish.getAquarium())) {
            Aquarium newAquarium = aquariumRepository.find(fish.getAquarium().getId())
                    .orElseThrow(() -> new NotFoundException("Aquarium " + fish.getAquarium().getId() + " not found"));
            current.getAquarium().getFish().remove(current);
            newAquarium.getFish().add(current);
        }
        fishRepository.update(fish);
    }

    /**
     * Deletes a fish and removes it from the fish lists of its aquarium and user.
     */
    @Transactional
    public void delete(UUID id) {
        fishRepository.find(id).ifPresent(fish -> {
            fish.getAquarium().getFish().remove(fish);
            if (fish.getUser() != null) {
                fish.getUser().getFish().remove(fish);
            }
            fishRepository.delete(fish);
        });
    }
}
