package pl.edu.pg.eti.kask.aquarium.fish.view;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import pl.edu.pg.eti.kask.aquarium.aquarium.entity.Aquarium;
import pl.edu.pg.eti.kask.aquarium.aquarium.service.AquariumService;
import pl.edu.pg.eti.kask.aquarium.fish.entity.Fish;
import pl.edu.pg.eti.kask.aquarium.fish.entity.Temperament;
import pl.edu.pg.eti.kask.aquarium.fish.service.FishService;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/**
 * Backing bean of the "new fish" form (fish/create.xhtml?aquarium=...). View scoped: the fish being filled in is
 * kept between displaying the form and submitting it.
 */
@ViewScoped
@Named
public class FishCreate implements Serializable {

    private final FishService fishService;
    private final AquariumService aquariumService;

    /** Optional {@code aquarium} request parameter: the aquarium selected in the form by default. */
    private UUID aquariumId;

    private Fish fish;

    private List<Aquarium> aquariums;

    @Inject
    public FishCreate(FishService fishService, AquariumService aquariumService) {
        this.fishService = fishService;
        this.aquariumService = aquariumService;
    }

    /** Required by CDI to create a proxy of a normal scoped bean. */
    protected FishCreate() {
        this(null, null);
    }

    /**
     * Prepares an empty fish with sensible defaults. Called once, when the form is opened.
     */
    public void init() {
        fish = new Fish();
        fish.setQuantity(1);
        fish.setPurchaseDate(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
        if (aquariumId != null) {
            aquariumService.find(aquariumId).ifPresent(fish::setAquarium);
        }
    }

    /**
     * Saves the new fish and shows its aquarium (redirect after POST).
     */
    public String save() {
        fish.setId(UUID.randomUUID());
        fishService.create(fish);
        return "/aquarium/view?faces-redirect=true&id=" + fish.getAquarium().getId();
    }

    /** Options of the aquarium drop-down list. */
    public List<Aquarium> getAquariums() {
        if (aquariums == null) {
            aquariums = aquariumService.findAll();
        }
        return aquariums;
    }

    /** Options of the temperament drop-down list. */
    public Temperament[] getTemperaments() {
        return Temperament.values();
    }

    public UUID getAquariumId() { return aquariumId; }
    public void setAquariumId(UUID aquariumId) { this.aquariumId = aquariumId; }

    public Fish getFish() { return fish; }
}
