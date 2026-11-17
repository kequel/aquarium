package pl.edu.pg.eti.kask.aquarium.configuration;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.context.control.RequestContextController;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import pl.edu.pg.eti.kask.aquarium.aquarium.entity.Aquarium;
import pl.edu.pg.eti.kask.aquarium.aquarium.repository.AquariumRepository;
import pl.edu.pg.eti.kask.aquarium.fish.entity.Fish;
import pl.edu.pg.eti.kask.aquarium.fish.entity.Temperament;
import pl.edu.pg.eti.kask.aquarium.fish.repository.FishRepository;
import pl.edu.pg.eti.kask.aquarium.user.entity.Role;
import pl.edu.pg.eti.kask.aquarium.user.entity.User;
import pl.edu.pg.eti.kask.aquarium.user.service.UserService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Loads test data when the application starts. Fixed UUIDs keep request.http usable between restarts.
 * Aquarium and fish services are secured and nobody is logged in during startup, so aquariums and fish are
 * saved with repositories, in one transaction. Users are registered with the (unsecured) user service,
 * which stores password hashes. Password of every test user is the same as the login.
 */
@ApplicationScoped
public class InitializedData {

    private final UserService userService;
    private final AquariumRepository aquariumRepository;
    private final FishRepository fishRepository;
    private final RequestContextController requestContextController;

    @Inject
    public InitializedData(UserService userService, AquariumRepository aquariumRepository,
                           FishRepository fishRepository, RequestContextController requestContextController) {
        this.userService = userService;
        this.aquariumRepository = aquariumRepository;
        this.fishRepository = fishRepository;
        this.requestContextController = requestContextController;
    }

    /** Required by CDI to create a proxy of a normal scoped bean. */
    protected InitializedData() {
        this(null, null, null, null);
    }

    @Transactional
    void init(@Observes @Initialized(ApplicationScoped.class) Object event) {
        // Repositories are request scoped, and there is no HTTP request during startup.
        requestContextController.activate();
        try {
            loadUsers();
            loadAquariumsAndFish();
        } finally {
            requestContextController.deactivate();
        }
    }

    private final User admin = new User(UUID.fromString("c4804e0f-769e-4ab9-9ebe-0578fb4f00a6"),
            "admin", "admin@example.com", LocalDate.of(1990, 10, 21), Role.ADMIN);
    private final User kevin = new User(UUID.fromString("81e1c2a9-7f57-439b-b53d-6db88b071e4e"),
            "kevin", "kevin@example.com", LocalDate.of(2001, 1, 16), Role.USER);
    private final User alice = new User(UUID.fromString("ed6cfb2a-cad7-47dd-9b56-9d1e3c7a4197"),
            "alice", "alice@example.com", LocalDate.of(2002, 3, 19), Role.USER);
    private final User bob = new User(UUID.fromString("f08ef7e3-7f2a-4378-b1fb-2922d730c70d"),
            "bob", "bob@example.com", LocalDate.of(1999, 7, 4), Role.USER);

    private void loadUsers() {
        for (User user : new User[]{admin, kevin, alice, bob}) {
            user.setPassword(user.getLogin());
            userService.create(user);
        }
    }

    /** Every fish has an owner: kevin has 2 fish, alice 2, bob 1, admin none. */
    private void loadAquariumsAndFish() {
        Aquarium amazon = new Aquarium(UUID.fromString("5d1f2b8a-3c47-4e0d-9a61-7b2e8f4c1a01"),
                "Amazonka", 240, 26.5, true);
        Aquarium reef = new Aquarium(UUID.fromString("9a7e4c12-6b3d-4f85-8e2a-1c9d5b7f3a02"),
                "Rafa koralowa", 400, 25.0, false);
        Aquarium pond = new Aquarium(UUID.fromString("2c8b6e4f-1a9d-4b73-a5e0-6f3c2d8b9a03"),
                "Oczko wodne", 1500, 18.0, true);
        aquariumRepository.create(amazon);
        aquariumRepository.create(reef);
        aquariumRepository.create(pond);

        addFish(new Fish(UUID.fromString("a1b2c3d4-0001-4000-8000-000000000001"),
                "Neon Innesa", 3.5, LocalDateTime.of(2024, 5, 12, 14, 30), 15, Temperament.PEACEFUL, amazon, kevin));
        addFish(new Fish(UUID.fromString("a1b2c3d4-0002-4000-8000-000000000002"),
                "Skalar", 12.0, LocalDateTime.of(2024, 6, 3, 10, 0), 4, Temperament.SEMI_AGGRESSIVE, amazon, kevin));
        addFish(new Fish(UUID.fromString("a1b2c3d4-0003-4000-8000-000000000003"),
                "Błazenek", 8.0, LocalDateTime.of(2025, 2, 20, 16, 45), 2, Temperament.SEMI_AGGRESSIVE, reef, alice));
        addFish(new Fish(UUID.fromString("a1b2c3d4-0004-4000-8000-000000000004"),
                "Pokolec królewski", 15.0, LocalDateTime.of(2025, 3, 8, 11, 15), 1, Temperament.PEACEFUL, reef, alice));
        addFish(new Fish(UUID.fromString("a1b2c3d4-0005-4000-8000-000000000005"),
                "Karp koi", 45.0, LocalDateTime.of(2023, 8, 1, 9, 0), 6, Temperament.PEACEFUL, pond, bob));
    }

    /** Saves a fish and adds it to the fish lists of its aquarium and owner (both sides of the relations). */
    private void addFish(Fish fish) {
        fish.getAquarium().getFish().add(fish);
        fish.getUser().getFish().add(fish);
        fishRepository.create(fish);
    }
}
