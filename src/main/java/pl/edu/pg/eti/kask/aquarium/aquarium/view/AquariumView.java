package pl.edu.pg.eti.kask.aquarium.aquarium.view;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import pl.edu.pg.eti.kask.aquarium.aquarium.entity.Aquarium;
import pl.edu.pg.eti.kask.aquarium.aquarium.service.AquariumService;
import pl.edu.pg.eti.kask.aquarium.fish.entity.Fish;
import pl.edu.pg.eti.kask.aquarium.fish.service.FishService;
import pl.edu.pg.eti.kask.aquarium.view.HttpErrors;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * Backing bean of the aquarium details view (aquarium/view.xhtml?id=...). View scoped: the aquarium and its fish
 * are kept while the user stays on the page, so a "delete" button in the table knows which fish was clicked.
 */
@ViewScoped
@Named
public class AquariumView implements Serializable {

    private final AquariumService aquariumService;
    private final FishService fishService;

    /** Aquarium identifier, set from the {@code id} request parameter. */
    private UUID id;

    private Aquarium aquarium;

    private List<Fish> fish;

    @Inject
    public AquariumView(AquariumService aquariumService, FishService fishService) {
        this.aquariumService = aquariumService;
        this.fishService = fishService;
    }

    /** Required by CDI to create a proxy of a normal scoped bean. */
    protected AquariumView() {
        this(null, null);
    }

    /**
     * Loads the aquarium. Its fish come with it (EAGER relation), no separate query is needed.
     * Called once, when the page is opened.
     */
    public void init() {
        aquarium = id == null ? null : aquariumService.find(id).orElse(null);
        if (aquarium == null) {
            HttpErrors.notFound();
            return;
        }
        fish = aquarium.getFish();
    }

    /**
     * Deletes a fish and reloads the page (redirect after POST).
     */
    public String deleteFish(Fish toDelete) {
        fishService.delete(toDelete.getId());
        return "/aquarium/view?faces-redirect=true&id=" + id;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Aquarium getAquarium() { return aquarium; }

    public List<Fish> getFish() { return fish; }
}
