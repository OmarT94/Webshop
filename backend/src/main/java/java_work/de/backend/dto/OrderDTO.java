package java_work.de.backend.dto;

import java_work.de.backend.model.Address;
import java_work.de.backend.model.OrderItem;

import java.util.List;

public record OrderDTO(
        String id,
        String userEmail,
        List<OrderItem> items,
        double totalPrice,
        Address shippingAddress,
        String paymentStatus, // "PAID" oder "PENDING"
        String orderStatus, // "PROCESSING", "SHIPPED", "CANCELLED"
        String paymentMethod, // Zahlungsmethode als String
        String stripePaymentIntentId

) {}
