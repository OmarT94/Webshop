package java_work.de.backend.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;



@Service
public class StripeService {

    public StripeService(@Value("${STRIPE_SECRET_KEY}") String stripeSecretKey) {
        Stripe.apiKey = stripeSecretKey; //  API-Key aus den Umgebungsvariablen
    }

    public String createPaymentIntent(double amount, String currency, String paymentMethodType) throws StripeException {
        PaymentIntentCreateParams.Builder paramsBuilder = PaymentIntentCreateParams.builder()
                .setAmount((long) (amount * 100)) // Betrag in Cent
                .setCurrency(currency)
                .addPaymentMethodType(paymentMethodType) //  Diese Methode funktioniert in neueren Versionen
                .setDescription("Bestellung in unserem Shop");

        PaymentIntent paymentIntent = PaymentIntent.create(paramsBuilder.build());
        return paymentIntent.getClientSecret(); //  Client Secret für das Frontend zurückgeben
    }


}
