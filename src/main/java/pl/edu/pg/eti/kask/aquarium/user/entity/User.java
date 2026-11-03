package pl.edu.pg.eti.kask.aquarium.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Application user. The relation to fish is intentionally omitted for now (lab 2).
 */
@Entity
@Table(name = "users")
public class User implements Serializable {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String login;

    private String email;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    private Role role;

    public User() {
    }

    public User(UUID id, String login, String email, LocalDate birthDate, Role role) {
        this.id = id;
        this.login = login;
        this.email = email;
        this.birthDate = birthDate;
        this.role = role;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }

    @Override
    public String toString() {
        return "User{id=" + id + ", login='" + login + "', email='" + email + "', birthDate=" + birthDate
                + ", role=" + role + "}";
    }
}
