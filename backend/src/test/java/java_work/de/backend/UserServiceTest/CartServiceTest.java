package java_work.de.backend.UserServiceTest;

import java_work.de.backend.dto.CartDTO;
import java_work.de.backend.model.Cart;
import java_work.de.backend.model.OrderItem;
import java_work.de.backend.repo.CartRepository;
import java_work.de.backend.service.CartService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Aktiviert Mockito für die Tests
class CartServiceTest {

    @Mock
    private CartRepository cartRepository; // Simuliert die MongoDB-Repository-Schicht

    @InjectMocks
    private CartService cartService; // Testet den CartService mit Mock-Repository

    private static final String USER_EMAIL = "test@example.com";
    private static final ObjectId CART_ID = new ObjectId();
    private Cart testCart;
    private OrderItem testItem;

    //Vor jedem Test wird ein Beispiel-Warenkorb erstellt
    @BeforeEach
    void setUp() {
        testItem = new OrderItem("prod123", "Test Product", "image.jpg", 2, 19.99);
        testCart = new Cart(CART_ID, USER_EMAIL, List.of(testItem));
    }

    /*  Teste getCart() Wenn kein Warenkorb existiert, wird einer erstellt */
    @Test
    void testGetCart_WhenCartExists() {
        when(cartRepository.findByUserEmail(USER_EMAIL)).thenReturn(Optional.of(testCart)); //Simuliert, dass der Benutzer einen Warenkorb hat

        CartDTO cartDTO = cartService.getCart(USER_EMAIL);

        assertNotNull(cartDTO);
        assertEquals(USER_EMAIL, cartDTO.userEmail());
        assertEquals(1, cartDTO.items().size());//Stellt sicher, dass genau 1 Produkt im Warenkorb ist
        verify(cartRepository, times(1)).findByUserEmail(USER_EMAIL);//Prüft, ob die Methode genau 1x aufgerufen wurde
    }

    @Test
    void testGetCart_WhenCartDoesNotExist() {
        when(cartRepository.findByUserEmail(USER_EMAIL)).thenReturn(Optional.empty());

        CartDTO cartDTO = cartService.getCart(USER_EMAIL);

        assertNotNull(cartDTO);
        assertEquals(USER_EMAIL, cartDTO.userEmail());
        assertTrue(cartDTO.items().isEmpty());
        verify(cartRepository, times(1)).findByUserEmail(USER_EMAIL);
    }

    /* Teste addToCart() Neues Produkt wird hinzugefügt */
    @Test
    void testAddToCart_WhenNewItemAdded() {
        when(cartRepository.findByUserEmail(USER_EMAIL)).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        OrderItem newItem = new OrderItem("prod456", "New Product", "image2.jpg", 1, 29.99);
        CartDTO updatedCart = cartService.addToCart(USER_EMAIL, newItem);

        assertEquals(2, updatedCart.items().size()); // Neues Produkt wurde hinzugefügt //Warenkorb hat jetzt 2 Produkte
        verify(cartRepository, times(1)).save(any(Cart.class)); //Der Warenkorb wurde gespeichert
    }

    /* Teste addToCart() Menge eines bestehenden Produkts wird erhöht */
    @Test
    void testAddToCart_WhenItemAlreadyExists() {
        when(cartRepository.findByUserEmail(USER_EMAIL)).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        OrderItem sameItem = new OrderItem("prod123", "Test Product", "image.jpg", 3, 19.99);
        CartDTO updatedCart = cartService.addToCart(USER_EMAIL, sameItem);

        assertEquals(1, updatedCart.items().size()); // Keine doppelten Einträge
        assertEquals(5, updatedCart.items().get(0).quantity()); // Menge sollte 5 sein (2 + 3)
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    /* Teste updateCartQuantity() Menge eines Produkts wird aktualisiert */
    @Test
    void testUpdateCartQuantity() {
        when(cartRepository.findByUserEmail(USER_EMAIL)).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        CartDTO updatedCart = cartService.updateCartQuantity(USER_EMAIL, "prod123", 5);

        assertEquals(5, updatedCart.items().get(0).quantity());
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    /* Teste updateCartQuantity() Produkt wird entfernt, wenn Menge 0 */
    @Test
    void testUpdateCartQuantity_RemoveItemIfZero() {
        when(cartRepository.findByUserEmail(USER_EMAIL)).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        CartDTO updatedCart = cartService.updateCartQuantity(USER_EMAIL, "prod123", 0);

        assertEquals(0, updatedCart.items().size()); // Produkt sollte entfernt sein
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    /*Teste removeItem() Ein Produkt wird aus dem Warenkorb entfernt */
    @Test
    void testRemoveItem() {
        when(cartRepository.findByUserEmail(USER_EMAIL)).thenReturn(Optional.of(testCart));

        cartService.removeItem(USER_EMAIL, "prod123");

        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    /*Teste clearCart() Der gesamte Warenkorb wird gelöscht */
    @Test
    void testClearCart() {
        when(cartRepository.findByUserEmail(USER_EMAIL)).thenReturn(Optional.of(testCart));

        cartService.clearCart(USER_EMAIL);

        verify(cartRepository, times(1)).deleteById(CART_ID.toString());
    }
}
