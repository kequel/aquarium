package pl.edu.pg.eti.kask.aquarium.fish.service;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.LocalBean;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.security.enterprise.SecurityContext;
import pl.edu.pg.eti.kask.aquarium.aquarium.entity.Aquarium;
import pl.edu.pg.eti.kask.aquarium.aquarium.repository.AquariumRepository;
import pl.edu.pg.eti.kask.aquarium.exception.AccessDeniedException;
import pl.edu.pg.eti.kask.aquarium.exception.NotFoundException;
import pl.edu.pg.eti.kask.aquarium.fish.entity.Fish;
import pl.edu.pg.eti.kask.aquarium.fish.repository.FishRepository;
import pl.edu.pg.eti.kask.aquarium.logging.LogOperation;
import pl.edu.pg.eti.kask.aquarium.security.UserRoles;
import pl.edu.pg.eti.kask.aquarium.user.entity.User;
import pl.edu.pg.eti.kask.aquarium.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Business logic for fish (elements). A stateless EJB: roles are checked by the server ({@code @RolesAllowed}),
 * the owner is checked here, because only the business logic knows who owns a fish:
 * <ul>
 *     <li>an administrator sees and changes all fish,</li>
 *     <li>a user sees and changes only own fish (lists are filtered by the database query),</li>
 *     <li>a new fish always belongs to the caller.</li>
 * </ul>
 * Every change also updates both sides of the relations (the fish and the lists in its aquarium and owner).
 */
@LocalBean
@Stateless
public class FishService {

    private final FishRepository fishRepository;
    private final AquariumRepository aquariumRepository;
    private final UserRepository userRepository;
    private final SecurityContext securityContext;

    @Inject
    public FishService(FishRepository fishRepository, AquariumRepository aquariumRepository,
                       UserRepository userRepository, SecurityContext securityContext) {
        this.fishRepository = fishRepository;
        this.aquariumRepository = aquariumRepository;
        this.userRepository = userRepository;
        this.securityContext = securityContext;
    }

    /** Required by the EJB container. */
    public FishService() {
        this(null, null, null, null);
    }

    /**
     * @return all fish for an administrator, only own fish for a user
     */
    @RolesAllowed({UserRoles.ADMIN, UserRoles.USER})
    public List<Fish> findAll() {
        return isAdmin() ? fishRepository.findAll() : fishRepository.findAllByUser(callerLogin());
    }

    /**
     * @return the fish, empty if it does not exist
     * @throws AccessDeniedException if it belongs to another user
     */
    @RolesAllowed({UserRoles.ADMIN, UserRoles.USER})
    public Optional<Fish> find(UUID id) {
        Optional<Fish> fish = fishRepository.find(id);
        fish.ifPresent(this::checkOwner);
        return fish;
    }

    /**
     * @return the fish, empty if it does not exist or lives in another aquarium
     * @throws AccessDeniedException if it belongs to another user
     */
    @RolesAllowed({UserRoles.ADMIN, UserRoles.USER})
    public Optional<Fish> find(UUID aquariumId, UUID id) {
        Optional<Fish> fish = fishRepository.find(id)
                .filter(f -> f.getAquarium().getId().equals(aquariumId));
        fish.ifPresent(this::checkOwner);
        return fish;
    }

    /**
     * @return fish living in the given aquarium (an administrator gets all of them straight from the aquarium,
     * a user only own ones, filtered by the database query), empty if the aquarium does not exist
     */
    @RolesAllowed({UserRoles.ADMIN, UserRoles.USER})
    public Optional<List<Fish>> findAllByAquarium(UUID aquariumId) {
        return aquariumRepository.find(aquariumId).map(aquarium -> isAdmin()
                ? aquarium.getFish()
                : fishRepository.findAllByAquariumAndUser(aquarium, callerLogin()));
    }

    /**
     * Saves a new fish of the caller and adds it to the fish lists of its aquarium and owner.
     * The owner is always the logged-in caller, whatever was set before.
     *
     * @throws NotFoundException if the aquarium does not exist
     */
    @RolesAllowed({UserRoles.ADMIN, UserRoles.USER})
    @LogOperation
    public void create(Fish fish) {
        Aquarium aquarium = aquariumRepository.find(fish.getAquarium().getId())
                .orElseThrow(() -> new NotFoundException("Aquarium " + fish.getAquarium().getId() + " not found"));
        User owner = userRepository.findByLogin(callerLogin())
                .orElseThrow(() -> new NotFoundException("User " + callerLogin() + " not found"));
        fish.setAquarium(aquarium);
        fish.setUser(owner);
        aquarium.getFish().add(fish);
        owner.getFish().add(fish);
        fishRepository.create(fish);
    }

    /**
     * Saves changes of a fish. The owner cannot be changed. If the fish was moved to another aquarium,
     * it is also moved between the aquariums' fish lists.
     *
     * @throws NotFoundException     if the fish or its new aquarium does not exist
     * @throws AccessDeniedException if the fish belongs to another user
     */
    @RolesAllowed({UserRoles.ADMIN, UserRoles.USER})
    @LogOperation
    public void update(Fish fish) {
        Fish current = fishRepository.find(fish.getId())
                .orElseThrow(() -> new NotFoundException("Fish " + fish.getId() + " not found"));
        checkOwner(current);
        fish.setUser(current.getUser());
        if (!current.getAquarium().equals(fish.getAquarium())) {
            Aquarium newAquarium = aquariumRepository.find(fish.getAquarium().getId())
                    .orElseThrow(() -> new NotFoundException("Aquarium " + fish.getAquarium().getId() + " not found"));
            current.getAquarium().getFish().remove(current);
            newAquarium.getFish().add(current);
        }
        fishRepository.update(fish);
    }

    /**
     * Deletes a fish and removes it from the fish lists of its aquarium and owner.
     *
     * @throws AccessDeniedException if the fish belongs to another user
     */
    @RolesAllowed({UserRoles.ADMIN, UserRoles.USER})
    @LogOperation
    public void delete(UUID id) {
        fishRepository.find(id).ifPresent(fish -> {
            checkOwner(fish);
            fish.getAquarium().getFish().remove(fish);
            if (fish.getUser() != null) {
                fish.getUser().getFish().remove(fish);
            }
            fishRepository.delete(fish);
        });
    }

    private boolean isAdmin() {
        return securityContext.isCallerInRole(UserRoles.ADMIN);
    }

    private String callerLogin() {
        return securityContext.getCallerPrincipal().getName();
    }

    /**
     * @throws AccessDeniedException if the caller is neither an administrator nor the owner of the fish
     */
    private void checkOwner(Fish fish) {
        if (isAdmin()) {
            return;
        }
        if (fish.getUser() == null || !fish.getUser().getLogin().equals(callerLogin())) {
            throw new AccessDeniedException("Fish " + fish.getId() + " belongs to another user");
        }
    }
}
