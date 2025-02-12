package java_work.de.backend.model;

import java.util.List;

public record PaymentRequest(
        double amount,
        String currency,
        List<String> paymentMethodTypes //  Jetzt eine Liste statt einzelner String!
) {}
