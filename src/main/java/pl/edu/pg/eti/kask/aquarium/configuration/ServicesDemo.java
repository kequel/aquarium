package pl.edu.pg.eti.kask.aquarium.configuration;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.context.control.RequestContextController;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import pl.edu.pg.eti.kask.aquarium.aquarium.entity.Aquarium;
import pl.edu.pg.eti.kask.aquarium.aquarium.service.AquariumService;
import pl.edu.pg.eti.kask.aquarium.fish.entity.Fish;
import pl.edu.pg.eti.kask.aquarium.fish.entity.Temperament;
import pl.edu.pg.eti.kask.aquarium.fish.service.FishService;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Demonstrates aquarium and fish services on startup (lab 2 has no controllers for them).
 * Output goes to the server log, lines are prefixed with {@code [DEMO]}.
 */
@ApplicationScoped
public class ServicesDemo {

    private static final Logger LOG = Logger.getLogger(ServicesDemo.class.getName());

    private final AquariumService aquariumService;
    private final FishService fishService;
    private final RequestContextController requestContextController;

    @Inject
    public ServicesDemo(AquariumService aquariumService, FishService fishService,
                        RequestContextController requestContextController) {
        this.aquariumService = aquariumService;
        this.fishService = fishService;
        this.requestContextController = requestContextController;
    }

    /** Required by CDI to create a proxy of a normal scoped bean. */
    protected ServicesDemo() {
        this(null, null, null);
    }

    void run(@Observes @Priority(InitializedData.PRIORITY + 1) @Initialized(ApplicationScoped.class) Object event) {
        requestContextController.activate();
        try {
            demo();
        } finally {
            requestContextController.deactivate();
        }
    }

    private void demo() {
        log("=== Wszystkie akwaria ===");
        aquariumService.findAll().forEach(a -> log("  " + a));

        log("=== Wszystkie ryby ===");
        fishService.findAll().forEach(f -> log("  " + f));

        Aquarium amazon = aquariumService.findAll().stream()
                .filter(a -> a.getName().equals("Amazonka")).findFirst().orElseThrow();
        log("=== Ryby w akwarium '" + amazon.getName() + "' ===");
        fishService.findAllByAquarium(amazon.getId()).orElseThrow().forEach(f -> log("  " + f));

        log("=== Dodanie ryby do akwarium '" + amazon.getName() + "' ===");
        Fish guppy = new Fish(UUID.randomUUID(), "Gupik", 4.0, LocalDateTime.now(), 10,
                Temperament.PEACEFUL, amazon);
        fishService.create(guppy);
        fishService.findAllByAquarium(amazon.getId()).orElseThrow().forEach(f -> log("  " + f));

        log("=== Zmiana liczby gupikow na 12 ===");
        guppy.setQuantity(12);
        fishService.update(guppy);
        log("  " + fishService.find(guppy.getId()).orElseThrow());

        log("=== Usuniecie gupikow ===");
        fishService.delete(guppy.getId());
        log("  istnieje po usunieciu: " + fishService.find(guppy.getId()).isPresent());

        log("=== Dodanie i usuniecie akwarium razem z rybami ===");
        Aquarium temporary = new Aquarium(UUID.randomUUID(), "Tymczasowe", 60, 24.0, true);
        aquariumService.create(temporary);
        fishService.create(new Fish(UUID.randomUUID(), "Danio", 5.0, LocalDateTime.now(), 8,
                Temperament.PEACEFUL, temporary));
        log("  ryb w '" + temporary.getName() + "' przed usunieciem: "
                + fishService.findAllByAquarium(temporary.getId()).orElseThrow().size());
        aquariumService.delete(temporary.getId());
        log("  akwarium istnieje po usunieciu: " + aquariumService.find(temporary.getId()).isPresent());
        log("  liczba wszystkich ryb: " + fishService.findAll().size());
    }

    private static void log(String message) {
        LOG.info("[DEMO] " + message);
    }
}
