package pl.edu.pg.eti.kask.aquarium.aquarium.view;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import pl.edu.pg.eti.kask.aquarium.aquarium.entity.Aquarium;
import pl.edu.pg.eti.kask.aquarium.aquarium.service.AquariumService;

import java.util.List;

/**
 * Backing bean of the aquarium list view (aquarium/list.xhtml). Request scoped: the list is read again
 * on every request, nothing has to be remembered between requests.
 */
@RequestScoped
@Named
public class AquariumList {

    private final AquariumService aquariumService;

    /** Loaded lazily on first access, so the database is queried at most once per request. */
    private List<Aquarium> aquariums;

    @Inject
    public AquariumList(AquariumService aquariumService) {
        this.aquariumService = aquariumService;
    }

    /** Required by CDI to create a proxy of a normal scoped bean. */
    protected AquariumList() {
        this(null);
    }

    public List<Aquarium> getAquariums() {
        if (aquariums == null) {
            aquariums = aquariumService.findAll();
        }
        return aquariums;
    }

    /**
     * Deletes the aquarium together with its fish and reloads the list (redirect after POST).
     */
    public String delete(Aquarium aquarium) {
        aquariumService.delete(aquarium.getId());
        return "/aquarium/list?faces-redirect=true";
    }
}
