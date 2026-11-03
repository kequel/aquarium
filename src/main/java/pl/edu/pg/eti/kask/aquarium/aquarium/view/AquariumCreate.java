package pl.edu.pg.eti.kask.aquarium.aquarium.view;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import pl.edu.pg.eti.kask.aquarium.aquarium.entity.Aquarium;
import pl.edu.pg.eti.kask.aquarium.aquarium.service.AquariumService;

import java.io.Serializable;
import java.util.UUID;

/**
 * Backing bean of the "new aquarium" form (aquarium/create.xhtml). View scoped: the aquarium being filled in is
 * kept between displaying the form and submitting it.
 */
@ViewScoped
@Named
public class AquariumCreate implements Serializable {

    private final AquariumService aquariumService;

    private final Aquarium aquarium = new Aquarium();

    @Inject
    public AquariumCreate(AquariumService aquariumService) {
        this.aquariumService = aquariumService;
    }

    /** Required by CDI to create a proxy of a normal scoped bean. */
    protected AquariumCreate() {
        this(null);
    }

    /**
     * Saves the new aquarium and shows it (redirect after POST).
     */
    public String save() {
        aquarium.setId(UUID.randomUUID());
        aquariumService.create(aquarium);
        return "/aquarium/view?faces-redirect=true&id=" + aquarium.getId();
    }

    public Aquarium getAquarium() { return aquarium; }
}
