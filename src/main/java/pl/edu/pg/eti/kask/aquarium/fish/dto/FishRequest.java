package pl.edu.pg.eti.kask.aquarium.fish.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import pl.edu.pg.eti.kask.aquarium.aquarium.entity.Aquarium;
import pl.edu.pg.eti.kask.aquarium.fish.entity.Fish;
import pl.edu.pg.eti.kask.aquarium.fish.entity.Temperament;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JSON body of a request creating (POST) or modifying (PUT) a fish. There is no aquarium field on purpose:
 * the aquarium comes from the address (/aquariums/{aquariumId}/fish).
 */
public record FishRequest(@NotBlank String species, @Positive Double lengthCm, LocalDateTime purchaseDate,
                          @Positive Integer quantity, Temperament temperament) {

    public Fish toEntity(Aquarium aquarium) {
        return new Fish(UUID.randomUUID(), species, lengthCm, purchaseDate, quantity, temperament, aquarium);
    }

    /** Copies the new values, the fish stays in its aquarium. */
    public void applyTo(Fish fish) {
        fish.setSpecies(species);
        fish.setLengthCm(lengthCm);
        fish.setPurchaseDate(purchaseDate);
        fish.setQuantity(quantity);
        fish.setTemperament(temperament);
    }
}
