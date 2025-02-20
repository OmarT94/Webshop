package java_work.de.backend.dto;

import jakarta.validation.constraints.*;

import java.util.List;

public record ProductDTO(
        String id,

        @NotBlank(message = "Name darf nicht leer sein!")
        String name,

        String description,

        @NotNull(message = "Preis ist erforderlich!")
        @Min(value = 0, message = "Preis darf nicht negativ sein!")
        Double price,

        @NotNull(message = "Lagerbestand ist erforderlich!")
        @Min(value = 0, message = "Lagerbestand darf nicht negativ sein!")
        Integer stock,

        @NotEmpty(message = "Mindestens ein Bild erforderlich!") //  Stellt sicher, dass `images` nicht leer ist
        @Size(min = 1, message = "Es muss mindestens ein Bild vorhanden sein!") //  Mindestanzahl von 1 Bild setzen
        List<String> images,

        @NotBlank(message = "Kategorie darf nicht leer sein!") String category // Kategorie hinzufügen

) {
}
