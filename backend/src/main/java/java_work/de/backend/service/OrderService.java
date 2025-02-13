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

import java.util.EnumSet;
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


//Bestellungen eines Benutzers abrufen
    public List<OrderDTO> getUserOrders(String userEmail) {
        return orderRepository.findByUserEmail(userEmail)
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
     Bestellung stornieren (nur wenn nicht versandt)
     Der Benutzer kann eine Bestellung nur stornieren, wenn sie noch nicht versandt wurde.
     */
    public boolean cancelOrder(String orderId, String userEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException(" Bestellung mit ID " + orderId + " nicht gefunden!"));
        logger.info(" Token-Benutzer: {}", userEmail);
        logger.info(" Bestellung gehört zu: {}", order.userEmail());
        if (!order.userEmail().equals(userEmail)) {
            logger.warn(" Zugriff verweigert: Benutzer '{}' darf Bestellung '{}' nicht stornieren!", userEmail, order.id());
            return false; //  Stornierung verweigern statt Exception zu werfen
        }
        if (order.orderStatus() == Order.OrderStatus.SHIPPED) {
            logger.warn(" Bestellung '{}' kann nicht storniert werden, da sie bereits versandt wurde!", order.id());
            return false; //  Bestellung kann nicht storniert werden
        }
        if (order.orderStatus() == Order.OrderStatus.CANCELLED) {
            throw new IllegalStateException("Bestellung wurde bereits storniert!");
        }
        // Bestellung auf "CANCELLED" setzen
        logger.info(" Bestellung '{}' wird storniert...", order.id());
        Order updatedOrder = new Order(
                order.id(),
                order.userEmail(),
                order.items(),
                order.totalPrice(),
                order.shippingAddress(),
                order.paymentStatus(),
                Order.OrderStatus.CANCELLED, // Setze den Status auf "CANCELLED"
                order.paymentMethod(),
                order.stripePaymentIntentId(),
                false


        );

        orderRepository.save(updatedOrder);
        logger.info(" Bestellung '{}' erfolgreich storniert!", order.id());
        return true;
    }

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
    /*
      Bestellung löschen
    */
    public void deleteOrder(String orderId) {
        orderRepository.deleteById(orderId);
    }


    //Benutzer kann eine Rückgabe anfordern
    public boolean requestReturn(String orderId, String userEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException(" Bestellung nicht gefunden!"));

        logger.info(" Prüfe Benutzer: '{}'", userEmail);
        logger.info(" Bestellung gehört zu: '{}'", order.userEmail());

        if (!order.userEmail().equalsIgnoreCase(userEmail)) {
            logger.warn(" Rückgabe nicht erlaubt: Benutzer stimmt nicht überein!");
            throw new IllegalStateException(" Rückgabe nicht erlaubt: Falscher Benutzer!");
        }

        if (order.orderStatus() != Order.OrderStatus.SHIPPED) {
            logger.warn(" Rückgabe nicht erlaubt: Bestellung nicht versandt!");
            throw new IllegalStateException(" Rückgabe nur nach Versand erlaubt!");
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
        logger.info(" Rückgabe erfolgreich angefordert!");
        return true;
    }




    /*
       Admin kann eine Rückgabe genehmigen und erstatten
    */
    public boolean approveReturn(String orderId) throws StripeException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Bestellung nicht gefunden!"));

        if (order.orderStatus() != Order.OrderStatus.RETURN_REQUESTED) {
            throw new IllegalStateException("Keine Rückgabe-Anfrage für diese Bestellung.");
        }

        // Stripe-Erstattung durchführen
        RefundCreateParams params = RefundCreateParams.builder()
                .setPaymentIntent(order.stripePaymentIntentId())
                .setAmount((long) (order.totalPrice() * 100))
                .build();

        Refund.create(params);

        // Bestellung als zurückgegeben markieren + Zahlungsstatus auf REFUNDED
        Order updatedOrder = new Order(
                order.id(),
                order.userEmail(),
                order.items(),
                order.totalPrice(),
                order.shippingAddress(),
                Order.PaymentStatus.REFUNDED, //  Geändert: Zahlungsstatus auf REFUNDED
                Order.OrderStatus.RETURNED,   //  Geändert: Bestellstatus auf RETURNED
                order.paymentMethod(),
                order.stripePaymentIntentId(),
                false
        );

        orderRepository.save(updatedOrder);
        return true;
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
