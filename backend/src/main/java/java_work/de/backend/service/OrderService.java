package java_work.de.backend.service;
import java_work.de.backend.dto.OrderDTO;
import java_work.de.backend.model.Address;
import java_work.de.backend.model.Order;
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

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public OrderDTO placeOrder(OrderDTO orderDTO) {
        Order newOrder = new Order(
                new ObjectId(),
                orderDTO.userEmail(),
                orderDTO.items(),
                orderDTO.totalPrice(),
                orderDTO.shippingAddress(),
                Order.PaymentStatus.PENDING, //  Standard: Zahlung ausstehend
                Order.OrderStatus.PROCESSING //  Standard: Bestellung wird bearbeitet
        );
        return mapToDTO(orderRepository.save(newOrder));
    }

    public List<OrderDTO> getUserOrders(String userEmail) {
        return orderRepository.findByUserEmail(userEmail)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    public OrderDTO updateOrderStatus(String orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Bestellung nicht gefunden"));

        Order.OrderStatus newStatus = Order.OrderStatus.valueOf(status);
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
                Order.OrderStatus.CANCELLED // Setze den Status auf "CANCELLED"
        );

        orderRepository.save(updatedOrder);
        logger.info(" Bestellung '{}' erfolgreich storniert!", order.id());
        return true;
    }

    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }
    public OrderDTO updatePaymentStatus(String orderId, String paymentStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Bestellung nicht gefunden!"));
        Order.PaymentStatus newPaymentStatus = Order.PaymentStatus.valueOf(paymentStatus);
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
    public void deleteOrder(String orderId) {
        orderRepository.deleteById(orderId);
    }

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
