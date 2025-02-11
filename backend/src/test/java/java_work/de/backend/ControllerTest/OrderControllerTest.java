//package java_work.de.backend.ControllerTest;
//
//import java_work.de.backend.contoller.OrderController;
//import java_work.de.backend.dto.OrderDTO;
//import java_work.de.backend.model.Address;
//import java_work.de.backend.model.OrderItem;
//import java_work.de.backend.service.OrderService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContext;
//import org.springframework.security.core.context.SecurityContextHolder;
//
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//class OrderControllerTest {
//
//    @Mock
//    private OrderService orderService;
//
//    @InjectMocks
//    private OrderController orderController;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    void testPlaceOrder() {
//        // Mock-Daten für den Checkout
//        String userEmail = "user@example.com";
//        Address shippingAddress = new Address("Street", "City", "12345", "Country");
//
//
//        // Erwartetes Ergebnis nach Bestellung
//        OrderDTO expectedOrder = new OrderDTO(
//                "1",
//                userEmail,
//                List.of(new OrderItem("prod123", "Product Name", "image.jpg", 2, 49.99)), // Beispielprodukt
//                99.98, // Gesamtpreis (2 x 49.99)
//                shippingAddress,
//                "PENDING",
//                "PROCESSING",
//                "PAYPAL"
//
//
//        );
//
//        // Mock-Verhalten für den Service definieren
//        when(orderService.placeOrder(eq(userEmail), eq(shippingAddress)))
//                .thenReturn(expectedOrder);
//
//        // Teste die Controller-Methode direkt
//
//        OrderDTO result = orderController.checkout(userEmail,shippingAddress);
//
//        // Assertions
//        assertNotNull(result);
//        assertEquals(userEmail, result.userEmail());
//        assertEquals(99.98, result.totalPrice());
//        assertEquals("PENDING", result.paymentStatus());
//        assertEquals("PROCESSING", result.orderStatus());
//
//    }
//
//
//    @Test
//    void testGetUserOrders() {
//        OrderDTO orderDTO = new OrderDTO("1", "user@example.com", List.of(),
//                99.99,
//                new Address("Street", "City", "12345", "Country"),
//                "PENDING",
//                "PROCESSING",
//                "PAYPAL"
//                );
//
//        when(orderService.getUserOrders("user@example.com")).thenReturn(List.of(orderDTO));
//
//        List<OrderDTO> result = orderController.getUserOrders("user@example.com");
//
//        assertNotNull(result);
//        assertEquals(1, result.size());
//        assertEquals("user@example.com", result.get(0).userEmail());
//    }
//
//    @Test
//    void testCancelOrder_Success() {
//        // Mocking Security Context
//        Authentication authentication = mock(Authentication.class);
//        when(authentication.getName()).thenReturn("user@example.com");
//        SecurityContext securityContext = mock(SecurityContext.class);
//        when(securityContext.getAuthentication()).thenReturn(authentication);
//        SecurityContextHolder.setContext(securityContext);
//
//        when(orderService.cancelOrder("1", "user@example.com")).thenReturn(true);
//
//        ResponseEntity<String> response = orderController.cancelOrder("1");
//
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertEquals("Bestellung erfolgreich storniert!", response.getBody());
//    }
//
//    @Test
//    void testCancelOrder_Forbidden() {
//        Authentication authentication = mock(Authentication.class);
//        when(authentication.getName()).thenReturn("user@example.com");
//        SecurityContext securityContext = mock(SecurityContext.class);
//        when(securityContext.getAuthentication()).thenReturn(authentication);
//        SecurityContextHolder.setContext(securityContext);
//
//        when(orderService.cancelOrder("1", "user@example.com")).thenReturn(false);
//
//        ResponseEntity<String> response = orderController.cancelOrder("1");
//
//        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
//        assertEquals(" Du darfst diese Bestellung nicht stornieren!", response.getBody());
//    }
//
//    @Test
//    void testGetAllOrders() {
//        OrderDTO orderDTO = new OrderDTO("1", "admin@example.com", List.of(), 200.00, new Address("Street", "City", "12345", "Country"), "PAID", "SHIPPED","PAYPAL");
//
//        when(orderService.getAllOrders()).thenReturn(List.of(orderDTO));
//
//        List<OrderDTO> result = orderController.getAllOrders();
//
//        assertNotNull(result);
//        assertEquals(1, result.size());
//        assertEquals("admin@example.com", result.get(0).userEmail());
//    }
//
//    @Test
//    void testUpdateOrderStatus() {
//        OrderDTO orderDTO = new OrderDTO("1", "user@example.com", List.of(),
//                99.99,
//                new Address("Street", "City", "12345", "Country"),
//                "PENDING",
//                "SHIPPED",
//                "PAYPAL"
//                );
//
//        when(orderService.updateOrderStatus("1", "SHIPPED")).thenReturn(orderDTO);
//
//        OrderDTO result = orderController.updateOrderStatus("1", "SHIPPED");
//
//        assertNotNull(result);
//        assertEquals("SHIPPED", result.orderStatus());
//    }
//
//    @Test
//    void testUpdatePaymentStatus() {
//        OrderDTO orderDTO = new OrderDTO("1", "user@example.com", List.of(),
//                99.99,
//                new Address("Street", "City", "12345", "Country"),
//                "PAID",
//                "PROCESSING",
//                "PAYPAL"
//                );
//
//        when(orderService.updatePaymentStatus("1", "PAID")).thenReturn(orderDTO);
//
//        OrderDTO result = orderController.updatePaymentStatus("1", "PAID");
//
//        assertNotNull(result);
//        assertEquals("PAID", result.paymentStatus());
//    }
//
//    @Test
//    void testUpdateShippingAddress() {
//        Address newAddress = new Address("New Street", "New City", "67890", "New Country");
//        OrderDTO orderDTO = new OrderDTO("1", "user@example.com", List.of(),
//                99.99, newAddress,
//                "PENDING",
//                "PROCESSING",
//                "PAYPAL"
//                );
//
//        when(orderService.updateShippingAddress("1", newAddress)).thenReturn(orderDTO);
//
//        OrderDTO result = orderController.updateShippingAddress("1", newAddress);
//
//        assertNotNull(result);
//        assertEquals("New City", result.shippingAddress().city());
//    }
//
//    @Test
//    void testDeleteOrder() {
//        doNothing().when(orderService).deleteOrder("1");
//
//        assertDoesNotThrow(() -> orderController.deleteOrder("1"));
//        verify(orderService, times(1)).deleteOrder("1");
//    }
//}
