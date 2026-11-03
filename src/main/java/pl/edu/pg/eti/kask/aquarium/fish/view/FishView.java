package pl.edu.pg.eti.kask.aquarium.fish.view;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import pl.edu.pg.eti.kask.aquarium.fish.entity.Fish;
import pl.edu.pg.eti.kask.aquarium.fish.service.FishService;
import pl.edu.pg.eti.kask.aquarium.view.HttpErrors;

import java.util.UUID;

/**
 * Backing bean of the fish details view (fish/view.xhtml?id=...). Request scoped: the page only displays data
 * and has no forms, so nothing has to be kept between requests.
 */
@RequestScoped
@Named
public class FishView {

    private final FishService fishService;

    /** Fish identifier, set from the {@code id} request parameter. */
    private UUID id;

    private Fish fish;

    @Inject
    public FishView(FishService fishService) {
        this.fishService = fishService;
    }

    /** Required by CDI to create a proxy of a normal scoped bean. */
    protected FishView() {
        this(null);
    }

    public void init() {
        fish = id == null ? null : fishService.find(id).orElse(null);
        if (fish == null) {
            HttpErrors.notFound();
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Fish getFish() { return fish; }
}
