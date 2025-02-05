package java_work.de.backend.contoller;

import java_work.de.backend.dto.OrderDTO;
import java_work.de.backend.model.Address;
import java_work.de.backend.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public OrderDTO placeOrder(@RequestBody OrderDTO orderDTO) {
        return orderService.placeOrder(orderDTO);
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

    @PutMapping("/{orderId}/payment")
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
