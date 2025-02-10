package java_work.de.backend.UserServiceTest;

import java_work.de.backend.dto.OrderDTO;
import java_work.de.backend.model.Address;
import java_work.de.backend.model.Cart;
import java_work.de.backend.model.Order;
import java_work.de.backend.model.OrderItem;
import java_work.de.backend.repo.CartRepository;
import java_work.de.backend.repo.OrderRepository;
import java_work.de.backend.service.OrderService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private OrderService orderService;

    private Order order;
    private OrderDTO orderDTO;
    private final String orderId = new ObjectId().toString();
    private final String userEmail = "user@example.com";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        Address address = new Address("Street", "City", "12345", "Country");

        order = new Order(
                new ObjectId(orderId),
                userEmail,
                List.of(),
                99.99,
                address,
                Order.PaymentStatus.PENDING,
                Order.OrderStatus.PROCESSING,
                Order.PaymentMethod.CREDIT_CARD
        );

        orderDTO = new OrderDTO(
                orderId,
                userEmail,
                List.of(),
                99.99,
                address,
                "PENDING",
                "PROCESSING",
                "CREDIT_CARD"
        );
    }

    @Test
    void testPlaceOrder() {
        Address shippingAddress = new Address("Street", "City", "12345", "Country");
        String paymentMethod = "CREDIT_CARD";

        Cart cart = new Cart(
                new ObjectId(),
                userEmail,
                List.of(new OrderItem("prod123", "Test Produkt", "img.jpg", 2, 19.99)) // Ein Produkt hinzufügen
        );

        when(cartRepository.findByUserEmail(userEmail)).thenReturn(Optional.of(cart)); //  Mock für existierenden Warenkorb
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderDTO result = orderService.placeOrder(userEmail, shippingAddress, paymentMethod);

        assertNotNull(result);
        assertEquals(userEmail, result.userEmail());
        assertEquals(shippingAddress, result.shippingAddress());
        assertEquals(paymentMethod,order.paymentMethod().name());
    }



    @Test
    void testGetUserOrders() {
        when(orderRepository.findByUserEmail(userEmail)).thenReturn(List.of(order));

        List<OrderDTO> result = orderService.getUserOrders(userEmail);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(userEmail, result.get(0).userEmail());
    }

    @Test
    void testUpdateOrderStatus() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderDTO result = orderService.updateOrderStatus(orderId, "SHIPPED");

        assertNotNull(result);
        assertEquals("SHIPPED",result.paymentMethod());
    }

    @Test
    void testCancelOrder_Success() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        boolean result = orderService.cancelOrder(orderId, userEmail);

        assertTrue(result);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void testCancelOrder_Failure_DifferentUser() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        boolean result = orderService.cancelOrder(orderId, "wrong@example.com");

        assertFalse(result);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testCancelOrder_Failure_AlreadyShipped() {
        Order shippedOrder = new Order(
                new ObjectId(orderId),
                userEmail,
                List.of(),
                99.99,
                order.shippingAddress(),
                Order.PaymentStatus.PAID,
                Order.OrderStatus.SHIPPED,
                Order.PaymentMethod.PAYPAL
        );

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(shippedOrder));

        boolean result = orderService.cancelOrder(orderId, userEmail);

        assertFalse(result);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testGetAllOrders() {
        when(orderRepository.findAll()).thenReturn(List.of(order));

        List<OrderDTO> result = orderService.getAllOrders();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testUpdatePaymentStatus() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));


        OrderDTO result = orderService.updatePaymentStatus(orderId, "PAID");

        assertNotNull(result);
        assertEquals("PAID", result.paymentStatus());
    }

    @Test
    void testUpdateShippingAddress() {
        Address newAddress = new Address("New Street", "New City", "67890", "New Country");

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        //  Stelle sicher, dass das gespeicherte Objekt zurückgegeben wird!
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderDTO result = orderService.updateShippingAddress(orderId, newAddress);

        assertNotNull(result, " Das Ergebnis sollte nicht NULL sein!");
        assertEquals("New City", result.shippingAddress().city(), " Die Stadt wurde nicht korrekt aktualisiert!");
    }


    @Test
    void testDeleteOrder() {
        doNothing().when(orderRepository).deleteById(orderId);

        assertDoesNotThrow(() -> orderService.deleteOrder(orderId));
        verify(orderRepository, times(1)).deleteById(orderId);
    }
}
