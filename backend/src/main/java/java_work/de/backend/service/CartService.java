package java_work.de.backend.service;

import java_work.de.backend.dto.CartDTO;
import java_work.de.backend.model.Cart;
import java_work.de.backend.model.OrderItem;
import java_work.de.backend.repo.CartRepository;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private final CartRepository cartRepository;

    public CartService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    public CartDTO getCart(String userEmail) {
        Cart cart = cartRepository.findByUserEmail(userEmail)
                .orElse(new Cart(new ObjectId(),userEmail, List.of())); // Falls kein Warenkorb existiert, erstelle neuen

        return mapTODTO(cart);
    }

    public CartDTO addToCart(String userEmail, OrderItem item) {
        Cart cart = cartRepository.findByUserEmail(userEmail)
                .orElse(new Cart(new ObjectId(),userEmail, List.of()));

        List<OrderItem> updateItems = new ArrayList<>(cart.items()); //  eine veränderbare Liste!

        Optional<OrderItem> existingItem=updateItems.stream()
                .filter(i ->i.productId().equals(item.productId()))
                .findFirst();

        if(existingItem.isPresent()) {
            OrderItem newItem = new OrderItem(
                    item.productId(),
                    item.name(),
                    item.imageBase64(),
                    existingItem.get().quantity()+item.quantity(),
                    item.price()
            );
            updateItems.remove(existingItem.get());
            updateItems.add(newItem);
        }else {
            updateItems.add(item);
        }
        Cart updatedCart=new Cart(cart.id(),userEmail,updateItems);
        cartRepository.save(updatedCart);
        return mapTODTO(updatedCart);
    }

    public CartDTO updateCartQuantity(String userEmail, String productId, int quantity) {
        Cart cart = cartRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Warenkorb not found"));

        List<OrderItem> updateItems = cart.items().stream()
                .map(item -> item.productId().equals(productId)
                ?new OrderItem(item.productId(),item.name(),item.imageBase64(),quantity,item.price())
                        :item)
                .filter(item -> item.quantity() > 0) //  Falls die Menge 0 ist, wird das Produkt entfernt!
                .toList();

        Cart updatedCart=new Cart(cart.id(),userEmail,updateItems);
        cartRepository.save(updatedCart);
        return mapTODTO(updatedCart);

    }

    public void removeItem(String userEmail, String productId) {
        Cart cart = cartRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Warenkorb nicht gefunden"));

        List<OrderItem> updatedItems = cart.items().stream()
                .filter(item -> !item.productId().equals(productId)) //  Nur die übriggebliebenen Items behalten
                .toList();

        Cart updatedCart = new Cart(cart.id(), userEmail, updatedItems);
        cartRepository.save(updatedCart); //  Aktualisierte Liste speichern!
    }


    public void clearCart(String userEmail) {
        cartRepository.findByUserEmail(userEmail)
                .ifPresent(cart -> cartRepository.deleteById(cart.id().toString())); //  Lösche das Cart anhand der ObjectId!
    }



    private CartDTO mapTODTO(Cart cart) {
        return new CartDTO(cart.id().toString(),cart.userEmail(),cart.items());
    }
}
