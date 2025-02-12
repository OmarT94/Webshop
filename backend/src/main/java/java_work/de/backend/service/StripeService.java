package java_work.de.backend.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
public class StripeService {
    private static final Logger logger = LoggerFactory.getLogger(StripeService.class);

    public StripeService(@Value("${STRIPE_SECRET_KEY}") String stripeSecretKey) {
        Stripe.apiKey = stripeSecretKey;
    }

    public String createPaymentIntent(double amount, String currency, List<String> paymentMethodTypes) throws StripeException {
        // Unterstützte Zahlungsmethoden prüfen
        List<String> validMethods = List.of("card", "sofort", "klarna", "sepa_debit");

        for (String method : paymentMethodTypes) {
            if (!validMethods.contains(method)) {
                throw new IllegalArgumentException("Ungültige Zahlungsmethode: " + method);
            }
        }

        PaymentIntentCreateParams.Builder paramsBuilder = PaymentIntentCreateParams.builder()
                .setAmount((long) (amount * 100)) // Betrag in Cent
                .setCurrency(currency)
                .setDescription("Bestellung in unserem Shop");

        // **WICHTIG: Füge SEPA-spezifische Einstellungen hinzu!**
        if (paymentMethodTypes.contains("sepa_debit")) {
            paramsBuilder.setSetupFutureUsage(PaymentIntentCreateParams.SetupFutureUsage.OFF_SESSION);
        }

        for (String method : paymentMethodTypes) {
            paramsBuilder.addPaymentMethodType(method);
        }

        PaymentIntent paymentIntent = PaymentIntent.create(paramsBuilder.build());

        System.out.println(" PaymentIntent erstellt: " + paymentIntent.getId() + " mit Methoden: " + paymentMethodTypes);

        return paymentIntent.getClientSecret(); // Client Secret für das Frontend zurückgeben
    }

}
