package java_work.de.backend.contoller;

import java_work.de.backend.dto.CartDTO;
import java_work.de.backend.model.OrderItem;
import java_work.de.backend.service.CartService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/{userEmail}")
    public CartDTO getCart(@PathVariable String userEmail) {
        return cartService.getCart(userEmail);
    }

    @PostMapping("/{userEmail}/add")
    public CartDTO addCart(@PathVariable String userEmail, @RequestBody OrderItem item) {
        return cartService.addToCart(userEmail,item);
    }

    @PutMapping("/{userEmail}/update/{productId}")
    public CartDTO updateQuantity(@PathVariable String userEmail, @PathVariable String productId, @RequestParam int quantity) {

        return cartService.updateCartQuantity(userEmail,productId,quantity);
    }

    @DeleteMapping("/{userEmail}/remove/{productId}")
    public void removeCart(@PathVariable String userEmail, @PathVariable String productId) {
        cartService.removeItem(userEmail,productId);
    }

    @DeleteMapping("/{userEmail}/clear")
    public void clearCart(@PathVariable String userEmail) {
        cartService.clearCart(userEmail);
    }

}
