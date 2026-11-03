package pl.edu.pg.eti.kask.aquarium.aquarium.dto;

import pl.edu.pg.eti.kask.aquarium.aquarium.entity.Aquarium;

import java.util.UUID;

/**
 * JSON representation of a single aquarium (without fish).
 */
public record GetAquariumResponse(UUID id, String name, Integer volumeLiters, Double temperature,
                                  boolean freshwater) {

    public static GetAquariumResponse from(Aquarium aquarium) {
        return new GetAquariumResponse(aquarium.getId(), aquarium.getName(), aquarium.getVolumeLiters(),
                aquarium.getTemperature(), aquarium.isFreshwater());
    }
}
