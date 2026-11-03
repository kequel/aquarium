package pl.edu.pg.eti.kask.aquarium.aquarium.view;

import jakarta.enterprise.context.Dependent;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;
import pl.edu.pg.eti.kask.aquarium.aquarium.entity.Aquarium;
import pl.edu.pg.eti.kask.aquarium.aquarium.service.AquariumService;

import java.util.UUID;

/**
 * Converts between an aquarium and its identifier, so that an aquarium can be chosen from a drop-down list.
 * Registered for the {@link Aquarium} type, so it is used automatically by every input bound to an aquarium.
 * {@code managed = true} makes Faces take it from CDI, which allows injecting the service; {@code @Dependent} makes
 * it a CDI bean (beans.xml only discovers classes with a scope annotation).
 */
@Dependent
@FacesConverter(forClass = Aquarium.class, managed = true)
public class AquariumConverter implements Converter<Aquarium> {

    private final AquariumService aquariumService;

    @Inject
    public AquariumConverter(AquariumService aquariumService) {
        this.aquariumService = aquariumService;
    }

    @Override
    public Aquarium getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return aquariumService.find(UUID.fromString(value)).orElse(null);
        } catch (IllegalArgumentException ex) {
            throw new ConverterException(new FacesMessage("Niepoprawne akwarium."), ex);
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Aquarium value) {
        return value == null ? "" : value.getId().toString();
    }
}
