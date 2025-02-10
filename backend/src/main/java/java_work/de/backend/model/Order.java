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
        PaymentMethod paymentMethod //  Zahlungsmethode
) {
    //  Enum für den Zahlungsstatus
    public enum PaymentStatus {
        PAID, PENDING
    }

    // Enum für den Bestellstatus
    public enum OrderStatus {
        PROCESSING, SHIPPED, CANCELLED
    }

    public enum PaymentMethod {
        PAYPAL,
        KLARNA,
        CREDIT_CARD,
        BANK_TRANSFER }
}


