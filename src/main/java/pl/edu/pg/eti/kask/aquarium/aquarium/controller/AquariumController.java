package pl.edu.pg.eti.kask.aquarium.aquarium.controller;

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
import pl.edu.pg.eti.kask.aquarium.aquarium.dto.AquariumRequest;
import pl.edu.pg.eti.kask.aquarium.aquarium.dto.GetAquariumResponse;
import pl.edu.pg.eti.kask.aquarium.aquarium.dto.GetAquariumsResponse;
import pl.edu.pg.eti.kask.aquarium.aquarium.entity.Aquarium;
import pl.edu.pg.eti.kask.aquarium.aquarium.service.AquariumService;

import java.net.URI;
import java.util.UUID;

/**
 * REST controller for aquariums (categories): /api/aquariums.
 * A missing aquarium gives 404, an invalid body gives 400 (thrown and mapped by JAX-RS).
 */
@Path("/aquariums")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AquariumController {

    private final AquariumService aquariumService;

    @Inject
    public AquariumController(AquariumService aquariumService) {
        this.aquariumService = aquariumService;
    }

    /** Required by CDI to create a proxy of a normal scoped bean. */
    protected AquariumController() {
        this(null);
    }

    /** GET /aquariums -> 200 */
    @GET
    public GetAquariumsResponse getAquariums() {
        return GetAquariumsResponse.from(aquariumService.findAll());
    }

    /** GET /aquariums/{id} -> 200 or 404 */
    @GET
    @Path("/{id}")
    public GetAquariumResponse getAquarium(@PathParam("id") UUID id) {
        return aquariumService.find(id)
                .map(GetAquariumResponse::from)
                .orElseThrow(NotFoundException::new);
    }

    /** POST /aquariums -> 201 with the Location header of the new aquarium */
    @POST
    public Response postAquarium(@NotNull @Valid AquariumRequest request, @Context UriInfo uriInfo) {
        Aquarium aquarium = request.toEntity();
        aquariumService.create(aquarium);
        URI location = uriInfo.getAbsolutePathBuilder().path(aquarium.getId().toString()).build();
        return Response.created(location).entity(GetAquariumResponse.from(aquarium)).build();
    }

    /** PUT /aquariums/{id} -> 204 or 404 */
    @PUT
    @Path("/{id}")
    public void putAquarium(@PathParam("id") UUID id, @NotNull @Valid AquariumRequest request) {
        Aquarium aquarium = aquariumService.find(id).orElseThrow(NotFoundException::new);
        request.applyTo(aquarium);
        aquariumService.update(aquarium);
    }

    /** DELETE /aquariums/{id} -> 204 or 404, the fish of the aquarium are deleted too */
    @DELETE
    @Path("/{id}")
    public void deleteAquarium(@PathParam("id") UUID id) {
        aquariumService.find(id).orElseThrow(NotFoundException::new);
        aquariumService.delete(id);
    }
}
