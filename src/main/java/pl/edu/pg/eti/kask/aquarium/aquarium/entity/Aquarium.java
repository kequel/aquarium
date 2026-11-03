package pl.edu.pg.eti.kask.aquarium.aquarium.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Aquarium (category). The relation to fish is unidirectional and owned by {@code Fish},
 * so the aquarium does not keep a list of its fish.
 */
@Entity
@Table(name = "aquariums")
public class Aquarium implements Serializable {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "volume_liters")
    private Integer volumeLiters;

    private Double temperature;

    private boolean freshwater;

    public Aquarium() {
    }

    public Aquarium(UUID id, String name, Integer volumeLiters, Double temperature, boolean freshwater) {
        this.id = id;
        this.name = name;
        this.volumeLiters = volumeLiters;
        this.temperature = temperature;
        this.freshwater = freshwater;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getVolumeLiters() { return volumeLiters; }
    public void setVolumeLiters(Integer volumeLiters) { this.volumeLiters = volumeLiters; }

    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }

    public boolean isFreshwater() { return freshwater; }
    public void setFreshwater(boolean freshwater) { this.freshwater = freshwater; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Aquarium that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }

    @Override
    public String toString() {
        return "Aquarium{id=" + id + ", name='" + name + "', volumeLiters=" + volumeLiters
                + ", temperature=" + temperature + ", freshwater=" + freshwater + "}";
    }
}
