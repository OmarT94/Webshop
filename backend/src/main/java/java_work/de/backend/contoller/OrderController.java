package java_work.de.backend.contoller;

import com.stripe.exception.StripeException;
import jakarta.servlet.http.HttpServletRequest;
import java_work.de.backend.dto.OrderDTO;
import java_work.de.backend.model.Address;
import java_work.de.backend.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/{userEmail}/checkout")
    public OrderDTO checkout(@PathVariable String userEmail,
                             @RequestParam String paymentIntentId,
                             @RequestParam String paymentMethod,
                             @RequestBody Address shippingAddress) throws StripeException {
        return orderService.placeOrder(userEmail,shippingAddress,paymentMethod,paymentIntentId);
    }

    @GetMapping("/{userEmail}")
    public List<OrderDTO> getUserOrders(@PathVariable String userEmail) {
        return orderService.getUserOrders(userEmail);
    }

    @DeleteMapping("/{orderId}/cancel")
    public ResponseEntity<String> cancelOrder(@PathVariable String orderId) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        boolean cancelled = orderService.cancelOrder(orderId, userEmail);

        if (cancelled) {
            return ResponseEntity.ok("Bestellung erfolgreich storniert!");
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(" Du darfst diese Bestellung nicht stornieren!");
        }
    }


    @PutMapping("/{orderId}/return-request")
    public ResponseEntity<String> requestReturn(@PathVariable String orderId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            logger.error(" Benutzer ist nicht authentifiziert!");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Nicht authentifiziert!");
        }

        // DEBUGGING: Logge alle Benutzer-Informationen
        String userEmail = authentication.getName();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        logger.info(" Benutzer aus Security-Kontext: {}", userEmail);
        logger.info(" Benutzerrollen: {}", authorities.stream().map(GrantedAuthority::getAuthority).toList());

        boolean hasUserRole = authorities.stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_USER"));
        boolean hasAdminRole = authorities.stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        logger.info(" Hat ROLE_USER? {}", hasUserRole);
        logger.info(" Hat ROLE_ADMIN? {}", hasAdminRole);

        if (!hasUserRole && !hasAdminRole) {
            logger.error(" Benutzer hat keine erforderliche Rolle!");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Zugriff verweigert: ROLE_USER oder ROLE_ADMIN erforderlich!");
        }

        // TEST: Kann die Methode überhaupt aufgerufen werden?
        logger.info(" requestReturn wurde aufgerufen für OrderID: {}", orderId);

        boolean requested = orderService.requestReturn(orderId, userEmail);

        if (requested) {
            logger.info(" Rückgabe erfolgreich angefordert für Bestellung: {}", orderId);
            return ResponseEntity.ok("Rückgabe erfolgreich angefordert!");
        } else {
            logger.warn(" Bestellung konnte nicht zurückgegeben werden!");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Rückgabe konnte nicht angefordert werden!");
        }
    }





    @PutMapping("/{orderId}/approve-return")
    public boolean approveReturn(@PathVariable String orderId) throws StripeException {
        return orderService.approveReturn(orderId);
    }


    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public List<OrderDTO> getAllOrders() {
        return orderService.getAllOrders();
    }

    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public OrderDTO updateOrderStatus(@PathVariable String orderId, @RequestParam String status) {
        return orderService.updateOrderStatus(orderId, status);
    }

    @PutMapping("/{orderId}/payment-status")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public OrderDTO updatePaymentStatus(@PathVariable String orderId, @RequestParam String paymentStatus) {
        return orderService.updatePaymentStatus(orderId, paymentStatus);
    }

    @PutMapping("/{orderId}/address")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public OrderDTO updateShippingAddress(@PathVariable String orderId, @RequestBody Address newAddress) {
        return orderService.updateShippingAddress(orderId, newAddress);
    }

    @DeleteMapping("/{orderId}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public void deleteOrder(@PathVariable String orderId) {
        orderService.deleteOrder(orderId);
    }

}
