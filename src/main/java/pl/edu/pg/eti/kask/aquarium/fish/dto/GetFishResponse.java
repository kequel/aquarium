package pl.edu.pg.eti.kask.aquarium.fish.dto;

import pl.edu.pg.eti.kask.aquarium.fish.entity.Fish;
import pl.edu.pg.eti.kask.aquarium.fish.entity.Temperament;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JSON representation of a single fish. The aquarium is already known from the address.
 */
public record GetFishResponse(UUID id, String species, Double lengthCm, LocalDateTime purchaseDate,
                              Integer quantity, Temperament temperament) {

    public static GetFishResponse from(Fish fish) {
        return new GetFishResponse(fish.getId(), fish.getSpecies(), fish.getLengthCm(), fish.getPurchaseDate(),
                fish.getQuantity(), fish.getTemperament());
    }
}
