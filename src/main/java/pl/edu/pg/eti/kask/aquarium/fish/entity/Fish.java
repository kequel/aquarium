package pl.edu.pg.eti.kask.aquarium.fish.entity;

import pl.edu.pg.eti.kask.aquarium.aquarium.entity.Aquarium;
import pl.edu.pg.eti.kask.aquarium.user.entity.User;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Fish {

    private UUID id;
    private String species;
    private Double lengthCm;
    private LocalDateTime purchaseDate;
    private Integer quantity;
    private Temperament temperament;
    private Aquarium aquarium;
    private User owner;

    public Fish() {
    }

    public Fish(UUID id, String species, Double lengthCm, LocalDateTime purchaseDate, Integer quantity,
                Temperament temperament, Aquarium aquarium, User owner) {
        this.id = id;
        this.species = species;
        this.lengthCm = lengthCm;
        this.purchaseDate = purchaseDate;
        this.quantity = quantity;
        this.temperament = temperament;
        this.aquarium = aquarium;
        this.owner = owner;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getSpecies() { return species; }
    public void setSpecies(String species) { this.species = species; }

    public Double getLengthCm() { return lengthCm; }
    public void setLengthCm(Double lengthCm) { this.lengthCm = lengthCm; }

    public LocalDateTime getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDateTime purchaseDate) { this.purchaseDate = purchaseDate; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Temperament getTemperament() { return temperament; }
    public void setTemperament(Temperament temperament) { this.temperament = temperament; }

    public Aquarium getAquarium() { return aquarium; }
    public void setAquarium(Aquarium aquarium) { this.aquarium = aquarium; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Fish that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }

    @Override
    public String toString() {
        return "Fish{id=" + id + ", species='" + species + "', lengthCm=" + lengthCm
                + ", purchaseDate=" + purchaseDate + ", quantity=" + quantity + ", temperament=" + temperament
                + ", aquarium=" + (aquarium != null ? aquarium.getName() : null)
                + ", owner=" + (owner != null ? owner.getLogin() : null) + "}";
    }
}
