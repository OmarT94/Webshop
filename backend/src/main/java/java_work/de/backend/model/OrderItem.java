package java_work.de.backend.model;

public record OrderItem(
        String productId, //  Referenz auf das Produkt
        String name, //  Produktname
        String imageBase64, //  Produktbild
        int quantity, //  Anzahl des bestellten Produkts
        double price //  Preis pro Stück
) {}
