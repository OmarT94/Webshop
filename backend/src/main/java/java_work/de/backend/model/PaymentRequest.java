package java_work.de.backend.model;

public record PaymentRequest(
        double amount,
        String currency,
        String paymentMethodType //  Zahlungsmethode: sofort, sepa_debit, klarna, card
) {}
