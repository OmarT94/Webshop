package java_work.de.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record AddressDTO(
        String id,
        @NotBlank(message = "Straße darf nicht leer sein!") String street,
        @NotBlank(message = "Hausnummer darf nicht leer sein!") String houseNumber,
        @NotBlank(message = "Stadt darf nicht leer sein!") String city,
        @NotBlank(message = "Postleitzahl darf nicht leer sein!") String postalCode,
        @NotBlank(message = "Land darf nicht leer sein!") String country,
        @NotBlank(message = "Handynummer darf nicht leer sein!") String telephoneNumber,
        boolean isDefault

) {
}
