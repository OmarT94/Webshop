package java_work.de.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

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

        @NotBlank(message = "Bild ist erforderlich!") // Base64-String erforderlich
        String imageBase64
) {
}
