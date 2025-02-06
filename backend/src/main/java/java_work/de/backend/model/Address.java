package java_work.de.backend.model;

import jakarta.validation.constraints.NotBlank;

public record Address(
        @NotBlank(message = "Straße darf nicht leer sein!") String street,
        @NotBlank(message = "Stadt darf nicht leer sein!") String city,
        @NotBlank(message = "Postleitzahl darf nicht leer sein!") String postalCode,
        @NotBlank(message = "Land darf nicht leer sein!") String country
) {}
