package pl.edu.pg.eti.kask.aquarium.view;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;

import java.util.UUID;

/**
 * Converts request parameters such as {@code ?id=...} to {@link UUID}. Registered for the UUID type, so every
 * view parameter bound to a UUID property uses it automatically. A malformed identifier is treated like a missing
 * one, so the view responds with 404 instead of failing.
 */
@FacesConverter(forClass = UUID.class)
public class UuidConverter implements Converter<UUID> {

    @Override
    public UUID getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, UUID value) {
        return value == null ? "" : value.toString();
    }
}
