package java_work.de.backend.UserServiceTest;

import java_work.de.backend.dto.OrderDTO;
import java_work.de.backend.model.Address;
import java_work.de.backend.model.Order;
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

    @InjectMocks
    private OrderService orderService;

    private Order order;
    private OrderDTO orderDTO;
    private final String ORDER_ID = new ObjectId().toString();
    private final String USER_EMAIL = "user@example.com";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        Address address = new Address("Street", "City", "12345", "Country");

        order = new Order(
                new ObjectId(ORDER_ID),
                USER_EMAIL,
                List.of(),
                99.99,
                address,
                Order.PaymentStatus.PENDING,
                Order.OrderStatus.PROCESSING
        );

        orderDTO = new OrderDTO(
                ORDER_ID,
                USER_EMAIL,
                List.of(),
                99.99,
                address,
                "PENDING",
                "PROCESSING"
        );
    }

    @Test
    void testPlaceOrder() {
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderDTO result = orderService.placeOrder(orderDTO);

        assertNotNull(result);
        assertEquals(USER_EMAIL, result.userEmail());
        assertEquals("PENDING", result.paymentStatus());
    }

    @Test
    void testGetUserOrders() {
        when(orderRepository.findByUserEmail(USER_EMAIL)).thenReturn(List.of(order));

        List<OrderDTO> result = orderService.getUserOrders(USER_EMAIL);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(USER_EMAIL, result.get(0).userEmail());
    }

    @Test
    void testUpdateOrderStatus() {
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderDTO result = orderService.updateOrderStatus(ORDER_ID, "SHIPPED");

        assertNotNull(result);
        assertEquals("SHIPPED", result.orderStatus());
    }

    @Test
    void testCancelOrder_Success() {
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        boolean result = orderService.cancelOrder(ORDER_ID, USER_EMAIL);

        assertTrue(result);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void testCancelOrder_Failure_DifferentUser() {
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        boolean result = orderService.cancelOrder(ORDER_ID, "wrong@example.com");

        assertFalse(result);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testCancelOrder_Failure_AlreadyShipped() {
        Order shippedOrder = new Order(
                new ObjectId(ORDER_ID),
                USER_EMAIL,
                List.of(),
                99.99,
                order.shippingAddress(),
                Order.PaymentStatus.PAID,
                Order.OrderStatus.SHIPPED
        );

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(shippedOrder));

        boolean result = orderService.cancelOrder(ORDER_ID, USER_EMAIL);

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
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));


        OrderDTO result = orderService.updatePaymentStatus(ORDER_ID, "PAID");

        assertNotNull(result);
        assertEquals("PAID", result.paymentStatus());
    }

    @Test
    void testUpdateShippingAddress() {
        Address newAddress = new Address("New Street", "New City", "67890", "New Country");

        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        //  Stelle sicher, dass das gespeicherte Objekt zurückgegeben wird!
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderDTO result = orderService.updateShippingAddress(ORDER_ID, newAddress);

        assertNotNull(result, " Das Ergebnis sollte nicht NULL sein!");
        assertEquals("New City", result.shippingAddress().city(), " Die Stadt wurde nicht korrekt aktualisiert!");
    }


    @Test
    void testDeleteOrder() {
        doNothing().when(orderRepository).deleteById(ORDER_ID);

        assertDoesNotThrow(() -> orderService.deleteOrder(ORDER_ID));
        verify(orderRepository, times(1)).deleteById(ORDER_ID);
    }
}
