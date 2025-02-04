package java_work.de.backend.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "orders")
public record Order(
        @Id ObjectId id,
        String userEmail, //  Verknüpfung zum Benutzer
        List<OrderItem> items, //  Enthält bestellte Produkte
        double totalPrice, //  Gesamtpreis
        Address shippingAddress, //  Lieferadresse
        PaymentStatus paymentStatus, // Bezahlt oder ausstehend (nur Admin)
        OrderStatus orderStatus // Bestellstatus (nur Admin)
) {
    //  Enum für den Zahlungsstatus
    public enum PaymentStatus {
        PAID, PENDING
    }

    // Enum für den Bestellstatus
    public enum OrderStatus {
        PROCESSING, SHIPPED, CANCELLED
    }
}


