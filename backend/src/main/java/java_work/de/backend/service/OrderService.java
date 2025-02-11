package java_work.de.backend.service;
import java_work.de.backend.dto.OrderDTO;
import java_work.de.backend.model.Address;
import java_work.de.backend.model.Cart;
import java_work.de.backend.model.Order;
import java_work.de.backend.repo.CartRepository;
import java_work.de.backend.repo.OrderRepository;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;

    public OrderService(OrderRepository orderRepository, CartRepository cartRepository) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
    }

    /*
      Bestellung aus dem Warenkorb aufgeben
      Der Warenkorb wird geleert, nachdem die Bestellung erfolgreich erstellt wurde.
      Der Benutzer muss eine gültige Zahlungsmethode wählen.
     */
public OrderDTO placeOrder(String userEmail, Address shippingAddress) {
    Cart cart = cartRepository.findByUserEmail(userEmail)
            .orElseThrow(() -> new NoSuchElementException("Warenkorb ist leer!"));

    if (cart.items().isEmpty()) {
        throw new IllegalStateException("Warenkorb ist leer, Bestellung nicht möglich.");
    }

    double totalPrice = cart.items().stream()
            .mapToDouble(item -> item.price() * item.quantity())
            .sum();



    Order newOrder = new Order(
            new ObjectId(),
            userEmail,
            cart.items(),
            totalPrice,
            shippingAddress,
            Order.PaymentStatus.PENDING, //  Standard: Zahlung ausstehend
            Order.OrderStatus.PROCESSING //  Standard: Bestellung wird bearbeitet

    );

    Order savedOrder = orderRepository.save(newOrder);
    cartRepository.deleteById(cart.id().toString());//  Warenkorb leeren nach Bestellung

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

        Order.OrderStatus newStatus;
        try {
            newStatus = Order.OrderStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Ungültiger Bestellstatus: " + status);
        }
        Order updatedOrder = new Order(
                order.id(),
                order.userEmail(),
                order.items(),
                order.totalPrice(),
                order.shippingAddress(),
                order.paymentStatus(),
                newStatus

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
                Order.OrderStatus.CANCELLED// Setze den Status auf "CANCELLED"

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
        Order.PaymentStatus newPaymentStatus;
        try {
            newPaymentStatus = Order.PaymentStatus.valueOf(paymentStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Ungültiger Zahlungsstatus: " + paymentStatus);
        }
        Order updatedOrder = new Order(
                order.id(),
                order.userEmail(),
                order.items(),
                order.totalPrice(),
                order.shippingAddress(),
                newPaymentStatus,
                order.orderStatus()


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
                order.orderStatus()

        );
        return mapToDTO(orderRepository.save(updatedOrder));
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
                order.orderStatus().name()
        );
    }


}
