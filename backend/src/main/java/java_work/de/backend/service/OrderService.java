package java_work.de.backend.service;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Refund;
import com.stripe.param.RefundCreateParams;
import java_work.de.backend.dto.OrderDTO;
import java_work.de.backend.model.Address;
import java_work.de.backend.model.Cart;
import java_work.de.backend.model.Order;
import java_work.de.backend.repo.CartRepository;
import java_work.de.backend.repo.OrderRepository;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
   ;
    public OrderService(OrderRepository orderRepository, CartRepository cartRepository, @Value("${stripe.secret.key}") String stripeSecretKey ) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
       Stripe.apiKey = stripeSecretKey;
    }

    /*
       Bestellung aufgeben (nur über Warenkorb)
       Adresse & Stripe Payment Intent validieren
       Zahlung muss erfolgreich sein, bevor Bestellung gespeichert wird
    */
    public OrderDTO placeOrder(String userEmail, Address shippingAddress, String paymentMethod, String paymentIntentId) {
        Cart cart = cartRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new NoSuchElementException("Warenkorb ist leer!"));

        if (cart.items().isEmpty()) {
            throw new IllegalStateException("Warenkorb ist leer, Bestellung nicht möglich.");
        }

        double totalPrice = cart.items().stream()
                .mapToDouble(item -> item.price() * item.quantity())
                .sum();

        //  Mapping von card → CREDIT_CARD
        Order.PaymentMethod method;
        switch (paymentMethod.toUpperCase()) {
            case "CARD":
            case "CREDIT_CARD":
                method = Order.PaymentMethod.CREDIT_CARD;
                break;
            case "SOFORT":
                method = Order.PaymentMethod.SOFORT;
                break;
            case "KLARNA":
                method = Order.PaymentMethod.KLARNA;
                break;
            case "SEPA":
                method = Order.PaymentMethod.SEPA;
                break;
            default:
                throw new IllegalStateException(" Ungültige Zahlungsmethode: " + paymentMethod);
        }

        Order newOrder = new Order(
                new ObjectId(),
                userEmail,
                cart.items(),
                totalPrice,
                shippingAddress,
                Order.PaymentStatus.PENDING, //  Standard: Zahlung ausstehend
                Order.OrderStatus.PROCESSING, //  Standard: Bestellung wird bearbeitet
                method,
                paymentIntentId, // Speichert Stripe Payment Intent ID
                false
        );


        Order savedOrder = orderRepository.save(newOrder);
        cartRepository.deleteById(cart.id().toString()); //  Warenkorb leeren nach Bestellung

        return mapToDTO(savedOrder);
    }

    /*  Benutzer kann eine Bestellung stornieren (nur wenn noch nicht versendet) */
    public boolean cancelOrder(String orderId, String userEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Bestellung nicht gefunden!"));

        if (!order.userEmail().equals(userEmail)) {
            throw new IllegalStateException("Diese Bestellung gehört nicht dir!");
        }

        if (order.orderStatus() == Order.OrderStatus.SHIPPED) {
            throw new IllegalStateException("Bestellung wurde bereits versendet, Stornierung nicht möglich.");
        }

        if (order.orderStatus() == Order.OrderStatus.CANCELLED) {
            throw new IllegalStateException("Diese Bestellung wurde bereits storniert.");
        }

        Order updatedOrder = new Order(
                order.id(),
                order.userEmail(),
                order.items(),
                order.totalPrice(),
                order.shippingAddress(),
                order.paymentStatus(),
                Order.OrderStatus.CANCELLED,
                order.paymentMethod(),
                order.stripePaymentIntentId(),
                false
        );

        orderRepository.save(updatedOrder);
        return true;
    }

    /*  Benutzer kann eine Rückgabe anfordern */
    public boolean requestReturn(String orderId, String userEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Bestellung nicht gefunden!"));

        if (!order.userEmail().equals(userEmail)) {
            throw new IllegalStateException("Diese Bestellung gehört nicht dir!");
        }

        if (order.orderStatus() != Order.OrderStatus.SHIPPED) {
            throw new IllegalStateException("Rückgabe nur nach Versand möglich.");
        }

        if (order.orderStatus() == Order.OrderStatus.RETURN_REQUESTED) {
            throw new IllegalStateException("Rückgabe wurde bereits angefordert.");
        }

        Order updatedOrder = new Order(
                order.id(),
                order.userEmail(),
                order.items(),
                order.totalPrice(),
                order.shippingAddress(),
                order.paymentStatus(),
                Order.OrderStatus.RETURN_REQUESTED,
                order.paymentMethod(),
                order.stripePaymentIntentId(),
                true
        );

        orderRepository.save(updatedOrder);
        return true;
    }

//Bestellungen eines Benutzers abrufen
    public List<OrderDTO> getUserOrders(String userEmail) {
        return orderRepository.findByUserEmail(userEmail)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    /// /////////ADMIN ////////

    /*
      Alle Bestellungen abrufen (nur für Admins)
     */
    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    /*
     Bestellstatus ändern ( PROCESSING - SHIPPED)
     Nur Admin kann den Status ändern.
     */
    public OrderDTO updateOrderStatus(String orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Bestellung nicht gefunden"));


        Order updatedOrder = new Order(
                order.id(),
                order.userEmail(),
                order.items(),
                order.totalPrice(),
                order.shippingAddress(),
                order.paymentStatus(),
                Order.OrderStatus.valueOf(status.toUpperCase()),
                order.paymentMethod(),
                order.stripePaymentIntentId(),
                false

        );

        return mapToDTO(orderRepository.save(updatedOrder));
    }

    /*
      Zahlungsstatus aktualisieren (nur Admin)
     */
    public OrderDTO updatePaymentStatus(String orderId, String paymentStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Bestellung nicht gefunden!"));

        Order updatedOrder = new Order(
                order.id(),
                order.userEmail(),
                order.items(),
                order.totalPrice(),
                order.shippingAddress(),
                Order.PaymentStatus.valueOf(paymentStatus.toUpperCase()),
                order.orderStatus(),
                order.paymentMethod(),
                order.stripePaymentIntentId(),
                false

        );
        return mapToDTO(orderRepository.save(updatedOrder));
    }

    /*
      Lieferadresse aktualisieren
    */
    public OrderDTO updateShippingAddress(String orderId, Address newShippingAddress) {
        Order order =orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Bestellung nicht gefunden!!"));
        Order updatedOrder = new Order(
                order.id(),
                order.userEmail(),
                order.items(),
                order.totalPrice(),
                newShippingAddress,
                order.paymentStatus(),
                order.orderStatus(),
                order.paymentMethod(),
                order.stripePaymentIntentId(),
                false

        );
        return mapToDTO(orderRepository.save(updatedOrder));
    }

    /*  Admin kann eine Rückgabe genehmigen und erstatten */
    public boolean approveReturn(String orderId) throws StripeException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Bestellung nicht gefunden!"));

        if (order.orderStatus() != Order.OrderStatus.RETURN_REQUESTED) {
            throw new IllegalStateException("Keine Rückgabe-Anfrage für diese Bestellung.");
        }

        try {
            RefundCreateParams params = RefundCreateParams.builder()
                    .setPaymentIntent(order.stripePaymentIntentId())
                    .setAmount((long) (order.totalPrice() * 100))
                    .build();
            Refund.create(params);
        } catch (StripeException e) {
            throw new IllegalStateException("Stripe-Erstattung fehlgeschlagen: " + e.getMessage());
        }

        Order updatedOrder = new Order(
                order.id(),
                order.userEmail(),
                order.items(),
                order.totalPrice(),
                order.shippingAddress(),
                Order.PaymentStatus.REFUNDED,
                Order.OrderStatus.RETURNED,
                order.paymentMethod(),
                order.stripePaymentIntentId(),
                false
        );

        orderRepository.save(updatedOrder);
        return true;
    }

    /*
      Bestellung löschen
    */
    public void deleteOrder(String orderId) {
        orderRepository.deleteById(orderId);
    }

    /*
      Hilfsfunktion: Mapping von Order zu OrderDTO
     */
    private OrderDTO mapToDTO(Order order) {
        return new OrderDTO(
                order.id().toString(),
                order.userEmail(),
                order.items(),
                order.totalPrice(),
                order.shippingAddress(),
                order.paymentStatus().name(),
                order.orderStatus().name(),
                order.paymentMethod().name(),
                order.stripePaymentIntentId(),
                false
        );
    }


}
