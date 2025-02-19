package java_work.de.backend.model;

import jakarta.validation.constraints.NotBlank;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

public record Address(
        @Id ObjectId id, // ID für jede Adresse
        @NotBlank(message = "Straße darf nicht leer sein!") String street,
        @NotBlank(message = "Hausnummer darf nicht leer sein!") String houseNumber,
        @NotBlank(message = "Stadt darf nicht leer sein!") String city,
        @NotBlank(message = "Postleitzahl darf nicht leer sein!") String postalCode,
        @NotBlank(message = "Land darf nicht leer sein!") String country,
        @NotBlank(message = "Handynummer darf nicht leer sein!") String telephoneNumber,

        boolean isDefault // Ist diese Adresse die Standard-Adresse?
) {

    // withIsDefault Methode, um eine Kopie mit geändertem isDefault-Wert zu erstellen
    public Address withIsDefault(boolean isDefault) {
        return new Address(this.id, this.street, this.houseNumber, this.city, this.postalCode, this.country, this.telephoneNumber, isDefault);
    }
}
