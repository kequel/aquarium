package pl.edu.pg.eti.kask.aquarium.fish.controller;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import pl.edu.pg.eti.kask.aquarium.aquarium.entity.Aquarium;
import pl.edu.pg.eti.kask.aquarium.aquarium.service.AquariumService;
import pl.edu.pg.eti.kask.aquarium.fish.dto.FishRequest;
import pl.edu.pg.eti.kask.aquarium.fish.dto.GetFishResponse;
import pl.edu.pg.eti.kask.aquarium.fish.dto.GetFishesResponse;
import pl.edu.pg.eti.kask.aquarium.fish.entity.Fish;
import pl.edu.pg.eti.kask.aquarium.fish.service.FishService;

import java.net.URI;
import java.util.UUID;

/**
 * REST controller for fish (elements), nested under their aquarium: /api/aquariums/{aquariumId}/fish.
 * The aquarium always comes from the address, never from the request body.
 */
@Path("/aquariums/{aquariumId}/fish")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FishController {

    private final FishService fishService;
    private final AquariumService aquariumService;

    @Inject
    public FishController(FishService fishService, AquariumService aquariumService) {
        this.fishService = fishService;
        this.aquariumService = aquariumService;
    }

    /** Required by CDI to create a proxy of a normal scoped bean. */
    protected FishController() {
        this(null, null);
    }

    /** GET /aquariums/{aquariumId}/fish -> 200, or 404 if the aquarium does not exist */
    @GET
    public GetFishesResponse getFishes(@PathParam("aquariumId") UUID aquariumId) {
        return fishService.findAllByAquarium(aquariumId)
                .map(GetFishesResponse::from)
                .orElseThrow(NotFoundException::new);
    }

    /** GET /aquariums/{aquariumId}/fish/{id} -> 200, or 404 if there is no such fish in this aquarium */
    @GET
    @Path("/{id}")
    public GetFishResponse getFish(@PathParam("aquariumId") UUID aquariumId, @PathParam("id") UUID id) {
        return fishService.find(aquariumId, id)
                .map(GetFishResponse::from)
                .orElseThrow(NotFoundException::new);
    }

    /** POST /aquariums/{aquariumId}/fish -> 201 with the Location header, or 404 if the aquarium does not exist */
    @POST
    public Response postFish(@PathParam("aquariumId") UUID aquariumId, @NotNull @Valid FishRequest request,
                             @Context UriInfo uriInfo) {
        Aquarium aquarium = aquariumService.find(aquariumId).orElseThrow(NotFoundException::new);
        Fish fish = request.toEntity(aquarium);
        fishService.create(fish);
        URI location = uriInfo.getAbsolutePathBuilder().path(fish.getId().toString()).build();
        return Response.created(location).entity(GetFishResponse.from(fish)).build();
    }

    /** PUT /aquariums/{aquariumId}/fish/{id} -> 204, or 404 if there is no such fish in this aquarium */
    @PUT
    @Path("/{id}")
    public void putFish(@PathParam("aquariumId") UUID aquariumId, @PathParam("id") UUID id,
                        @NotNull @Valid FishRequest request) {
        Fish fish = fishService.find(aquariumId, id).orElseThrow(NotFoundException::new);
        request.applyTo(fish);
        fishService.update(fish);
    }

    /** DELETE /aquariums/{aquariumId}/fish/{id} -> 204, or 404 if there is no such fish in this aquarium */
    @DELETE
    @Path("/{id}")
    public void deleteFish(@PathParam("aquariumId") UUID aquariumId, @PathParam("id") UUID id) {
        fishService.find(aquariumId, id).orElseThrow(NotFoundException::new);
        fishService.delete(id);
    }
}
