package pl.edu.pg.eti.kask.aquarium.fish.controller;

import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import pl.edu.pg.eti.kask.aquarium.fish.dto.GetFishesResponse;
import pl.edu.pg.eti.kask.aquarium.fish.service.FishService;
import pl.edu.pg.eti.kask.aquarium.security.UserRoles;

/**
 * REST controller for fish from all aquariums: /api/fish. An administrator gets all fish, a user only own ones
 * (decided by {@link FishService}).
 */
@Path("/fish")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
public class AllFishController {

    private final FishService fishService;

    @Inject
    public AllFishController(FishService fishService) {
        this.fishService = fishService;
    }

    /** Required by CDI to create a proxy of a normal scoped bean. */
    protected AllFishController() {
        this(null);
    }

    /** GET /fish -> 200 */
    @GET
    @RolesAllowed({UserRoles.ADMIN, UserRoles.USER})
    public GetFishesResponse getFishes() {
        return GetFishesResponse.from(fishService.findAll());
    }
}
