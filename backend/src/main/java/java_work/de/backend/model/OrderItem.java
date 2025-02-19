package java_work.de.backend.model;

import java.util.List;

public record OrderItem(
        String productId, //  Referenz auf das Produkt
        String name, //  Produktname
        List<String> images, //  Produktbild
        int quantity, //  Anzahl des bestellten Produkts
        double price //  Preis pro Stück
) {}
