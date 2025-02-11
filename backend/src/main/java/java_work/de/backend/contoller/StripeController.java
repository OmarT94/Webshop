package java_work.de.backend.contoller;

import com.stripe.exception.StripeException;
import java_work.de.backend.model.PaymentRequest;
import java_work.de.backend.service.StripeService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/stripe")
public class StripeController {
    private final StripeService stripeService;

    public StripeController(StripeService stripeService) {
        this.stripeService = stripeService;
    }


    /*
        Diese Route wird vom Frontend aufgerufen, um eine Zahlung mit Stripe zu starten
        Gibt ein `clientSecret` zurück, das das Frontend für die Bezahlung benutzt
    */
    @PostMapping("/create-payment-intent")
    public Map<String, String> createPaymentIntent(@RequestBody PaymentRequest request) throws StripeException {
        String clientSecret = stripeService.createPaymentIntent(request.amount(), request.currency(), request.paymentMethodType());
        return Map.of("clientSecret", clientSecret);
    }
}
