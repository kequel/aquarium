package pl.edu.pg.eti.kask.aquarium.user.entity;

import pl.edu.pg.eti.kask.aquarium.fish.entity.Fish;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class User {

    private UUID id;
    private String login;
    private String email;
    private LocalDate birthDate;
    private Role role;
    private List<Fish> fish = new ArrayList<>();

    public User() {
    }

    public User(UUID id, String login, String email, LocalDate birthDate, Role role) {
        this.id = id;
        this.login = login;
        this.email = email;
        this.birthDate = birthDate;
        this.role = role;
    }

    /**
     * Copy constructor, used by the repository so that callers never share stored instances.
     */
    public User(User other) {
        this(other.id, other.login, other.email, other.birthDate, other.role);
        this.fish = new ArrayList<>(other.fish);
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

    public List<Fish> getFish() { return fish; }
    public void setFish(List<Fish> fish) { this.fish = fish; }

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
