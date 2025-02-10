package java_work.de.backend.model;

public record CheckoutRequest(
        Address shippingAddress,
        String paymentMethod
) {
}
