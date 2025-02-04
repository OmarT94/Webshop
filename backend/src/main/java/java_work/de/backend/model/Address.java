package java_work.de.backend.model;

public record Address(
        String street,
        String city,
        String postalCode,
        String country
) {}
