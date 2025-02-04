package java_work.de.backend.contoller;

import java_work.de.backend.dto.OrderDTO;
import java_work.de.backend.service.OrderService;
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

    @PutMapping("/{orderId}/status")
    public OrderDTO updateOrderStatus(@PathVariable String orderId, @RequestParam String status) {
        return orderService.updateOrderStatus(orderId, status);
    }

    @DeleteMapping("/{orderId}")
    public void cancelOrder(@PathVariable String orderId) {
        orderService.cancelOrder(orderId);
    }
}
