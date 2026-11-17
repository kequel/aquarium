package pl.edu.pg.eti.kask.aquarium.aquarium.service;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import pl.edu.pg.eti.kask.aquarium.aquarium.entity.Aquarium;
import pl.edu.pg.eti.kask.aquarium.aquarium.repository.AquariumRepository;
import pl.edu.pg.eti.kask.aquarium.security.UserRoles;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Business logic for aquariums (categories). A stateless EJB, so the server checks {@code @RolesAllowed}
 * before every method (EJBAccessException if not allowed) and every method runs in a transaction.
 */
@LocalBean
@Stateless
public class AquariumService {

    private final AquariumRepository aquariumRepository;

    @Inject
    public AquariumService(AquariumRepository aquariumRepository) {
        this.aquariumRepository = aquariumRepository;
    }

    /** Required by the EJB container. */
    public AquariumService() {
        this(null);
    }

    @RolesAllowed({UserRoles.ADMIN, UserRoles.USER})
    public List<Aquarium> findAll() {
        return aquariumRepository.findAll();
    }

    @RolesAllowed({UserRoles.ADMIN, UserRoles.USER})
    public Optional<Aquarium> find(UUID id) {
        return aquariumRepository.find(id);
    }

    @RolesAllowed(UserRoles.ADMIN)
    public void create(Aquarium aquarium) {
        aquariumRepository.create(aquarium);
    }

    @RolesAllowed(UserRoles.ADMIN)
    public void update(Aquarium aquarium) {
        aquariumRepository.update(aquarium);
    }

    /**
     * Deletes the aquarium. Its fish are deleted automatically (cascade REMOVE on {@code Aquarium.fish}),
     * here they are only removed from their owners' fish lists, so the other side of the relation stays correct.
     */
    @RolesAllowed(UserRoles.ADMIN)
    public void delete(UUID id) {
        aquariumRepository.find(id).ifPresent(aquarium -> {
            aquarium.getFish().stream()
                    .filter(fish -> fish.getUser() != null)
                    .forEach(fish -> fish.getUser().getFish().remove(fish));
            aquariumRepository.delete(aquarium);
        });
    }
}
