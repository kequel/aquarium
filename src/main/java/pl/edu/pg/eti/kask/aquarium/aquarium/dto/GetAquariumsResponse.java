package pl.edu.pg.eti.kask.aquarium.aquarium.dto;

import pl.edu.pg.eti.kask.aquarium.aquarium.entity.Aquarium;

import java.util.List;
import java.util.UUID;

/**
 * JSON representation of the list of aquariums (without fish).
 */
public record GetAquariumsResponse(List<Item> aquariums) {

    public record Item(UUID id, String name) {
    }

    public static GetAquariumsResponse from(List<Aquarium> aquariums) {
        return new GetAquariumsResponse(aquariums.stream()
                .map(aquarium -> new Item(aquarium.getId(), aquarium.getName()))
                .toList());
    }
}
