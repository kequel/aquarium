package pl.edu.pg.eti.kask.aquarium.fish.view;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import pl.edu.pg.eti.kask.aquarium.aquarium.entity.Aquarium;
import pl.edu.pg.eti.kask.aquarium.aquarium.service.AquariumService;
import pl.edu.pg.eti.kask.aquarium.fish.entity.Fish;
import pl.edu.pg.eti.kask.aquarium.fish.entity.Temperament;
import pl.edu.pg.eti.kask.aquarium.fish.service.FishService;
import pl.edu.pg.eti.kask.aquarium.view.HttpErrors;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * Backing bean of the "edit fish" form (fish/edit.xhtml?id=...). View scoped: the fish being edited is kept
 * between displaying the form and submitting it.
 */
@ViewScoped
@Named
public class FishEdit implements Serializable {

    private final FishService fishService;
    private final AquariumService aquariumService;

    /** Fish identifier, set from the {@code id} request parameter. */
    private UUID id;

    private Fish fish;

    private List<Aquarium> aquariums;

    @Inject
    public FishEdit(FishService fishService, AquariumService aquariumService) {
        this.fishService = fishService;
        this.aquariumService = aquariumService;
    }

    /** Required by CDI to create a proxy of a normal scoped bean. */
    protected FishEdit() {
        this(null, null);
    }

    /**
     * Loads the fish to edit. Called once, when the form is opened.
     */
    public void init() {
        fish = id == null ? null : fishService.find(id).orElse(null);
        if (fish == null) {
            HttpErrors.notFound();
        }
    }

    /**
     * Saves the changes and shows the fish's aquarium (redirect after POST).
     */
    public String save() {
        fishService.update(fish);
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

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Fish getFish() { return fish; }
}
