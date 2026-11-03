package pl.edu.pg.eti.kask.aquarium.fish.dto;

import pl.edu.pg.eti.kask.aquarium.fish.entity.Fish;

import java.util.List;
import java.util.UUID;

/**
 * JSON representation of the list of fish in one aquarium.
 */
public record GetFishesResponse(List<Item> fish) {

    public record Item(UUID id, String species) {
    }

    public static GetFishesResponse from(List<Fish> fish) {
        return new GetFishesResponse(fish.stream()
                .map(f -> new Item(f.getId(), f.getSpecies()))
                .toList());
    }
}
