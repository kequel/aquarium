package pl.edu.pg.eti.kask.aquarium.fish.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import pl.edu.pg.eti.kask.aquarium.aquarium.entity.Aquarium;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Fish (element) living in an aquarium. Owner of the unidirectional fish -> aquarium relation.
 * The relation to the user is intentionally omitted for now (lab 2).
 */
@Entity
@Table(name = "fish")
public class Fish implements Serializable {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String species;

    @Column(name = "length_cm")
    private Double lengthCm;

    @Column(name = "purchase_date")
    private LocalDateTime purchaseDate;

    private Integer quantity;

    @Enumerated(EnumType.STRING)
    private Temperament temperament;

    @ManyToOne
    @JoinColumn(name = "aquarium_id", nullable = false)
    private Aquarium aquarium;

    public Fish() {
    }

    public Fish(UUID id, String species, Double lengthCm, LocalDateTime purchaseDate, Integer quantity,
                Temperament temperament, Aquarium aquarium) {
        this.id = id;
        this.species = species;
        this.lengthCm = lengthCm;
        this.purchaseDate = purchaseDate;
        this.quantity = quantity;
        this.temperament = temperament;
        this.aquarium = aquarium;
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
                + ", aquarium=" + (aquarium != null ? aquarium.getName() : null) + "}";
    }
}
