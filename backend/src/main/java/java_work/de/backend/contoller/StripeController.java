package java_work.de.backend.contoller;

import com.stripe.exception.StripeException;
import java_work.de.backend.model.PaymentRequest;
import java_work.de.backend.service.StripeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/stripe")
public class StripeController {
    private static final Logger logger = LoggerFactory.getLogger(StripeController.class);
    private final StripeService stripeService;

    public StripeController(StripeService stripeService) {
        this.stripeService = stripeService;
    }

    @PostMapping("/create-payment-intent")
    public Map<String, String> createPaymentIntent(@RequestBody PaymentRequest request) {
        try {
            String clientSecret = stripeService.createPaymentIntent(
                    request.amount(),
                    request.currency(),
                    request.paymentMethodTypes()
            );
            return Map.of("clientSecret", clientSecret);
        } catch (IllegalArgumentException e) {
            logger.warn(" Fehler: {}", e.getMessage());
            return Map.of("error", e.getMessage());
        } catch (StripeException e) {
            logger.error(" Stripe API Fehler: {}", e.getMessage());
            return Map.of("error", "Fehler bei der Stripe-Integration.");
        } catch (Exception e) {
            logger.error(" Unbekannter Fehler: {}", e.getMessage());
            return Map.of("error", "Ein unerwarteter Fehler ist aufgetreten.");
        }
    }
}
