package java_work.de.backend.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;


import java.util.List;


public record Order(
        @Id ObjectId id,
        String userEmail, //  Verknüpfung zum Benutzer
        List<OrderItem> items, //  Enthält bestellte Produkte
        double totalPrice, //  Gesamtpreis
        Address shippingAddress, //  Lieferadresse
        PaymentStatus paymentStatus, // Bezahlt oder ausstehend (nur Admin)
        OrderStatus orderStatus, // Bestellstatus (nur Admin)
        PaymentMethod paymentMethod, //  Zahlungsmethode hinzugefügt
        String stripePaymentIntentId, // Stripe Payment Intent speichern
        boolean returnRequested //  Neue Spalte für Rückgabe-Anfrage
) {
    //  Enum für den Zahlungsstatus
    public enum PaymentStatus {
        PAID, PENDING,REFUNDED
    }

    // Enum für den Bestellstatus
    public enum OrderStatus {
        PROCESSING, SHIPPED, CANCELLED,RETURN_REQUESTED,RETURNED
    }

    // Enum für die Zahlungsmethode
    public enum PaymentMethod {
        KLARNA,
        CREDIT_CARD,
        SOFORT,
        SEPA
    }
}


