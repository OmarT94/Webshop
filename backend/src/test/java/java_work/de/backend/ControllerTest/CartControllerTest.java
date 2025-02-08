package java_work.de.backend.ControllerTest;

import java_work.de.backend.contoller.CartController;
import java_work.de.backend.dto.CartDTO;
import java_work.de.backend.model.OrderItem;
import java_work.de.backend.service.CartService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(CartController.class) //  Nur CartController testen
@ExtendWith(MockitoExtension.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc; // Simuliert HTTP-Anfragen an Controller

    @MockBean
    private CartService cartService; // Service wird gemockt

    private static final String USER_EMAIL = "test@gmail.com";
    private static final String PRODUCT_ID = "prod123";
    private static final ObjectId CART_ID = new ObjectId();
    private CartDTO testCartDTO;
    private OrderItem testItem;
    private final ObjectMapper objectMapper = new ObjectMapper(); // JSON umwandeln
    @BeforeEach
    void setUp() {
        testItem = new OrderItem(PRODUCT_ID, "Test Product", "image.jpg", 2, 19.99);
        testCartDTO = new CartDTO(CART_ID.toString(), USER_EMAIL, List.of(testItem));

    }

     //Teste GET /api/cart/{userEmail}
    @Test
    @WithMockUser(username = "test@gmail.com", roles = {"USER"}) // Simuliert eingeloggten Benutzer
    void testGetCart() throws Exception {
        when(cartService.getCart(USER_EMAIL)).thenReturn(testCartDTO);

        mockMvc.perform(get("/api/cart/{userEmail}", USER_EMAIL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userEmail").value(USER_EMAIL))
                .andExpect(jsonPath("$.items[0].productId").value(PRODUCT_ID));

        verify(cartService, times(1)).getCart(USER_EMAIL);
    }


    //Teste POST /api/cart/{userEmail}/add
    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"}) // Simuliert eingeloggten Benutzer
    void testAddToCart() throws Exception {
        when(cartService.addToCart(eq(USER_EMAIL), any(OrderItem.class))).thenReturn(testCartDTO);

        mockMvc.perform(post("/api/cart/{userEmail}/add", USER_EMAIL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testItem))
                        .with(csrf())) //  Fügt ein CSRF-Token hinzu
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userEmail").value(USER_EMAIL))
                .andExpect(jsonPath("$.items[0].productId").value(PRODUCT_ID));

        verify(cartService, times(1)).addToCart(eq(USER_EMAIL), any(OrderItem.class));
    }




    // Teste PUT /api/cart/{userEmail}/update/{productId}
    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"}) // Simuliert eingeloggten Benutzer
    void testUpdateCartQuantity() throws Exception {
        when(cartService.updateCartQuantity(USER_EMAIL, PRODUCT_ID, 5)).thenReturn(testCartDTO);

        mockMvc.perform(put("/api/cart/{userEmail}/update/{productId}", USER_EMAIL, PRODUCT_ID)
                        .param("quantity", "5")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userEmail").value(USER_EMAIL))
                .andExpect(jsonPath("$.items[0].productId").value(PRODUCT_ID));

        verify(cartService, times(1)).updateCartQuantity(USER_EMAIL, PRODUCT_ID, 5);
    }

    // Teste DELETE /api/cart/{userEmail}/remove/{productId}
    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"}) // Simuliert eingeloggten Benutzer
    void testRemoveItem() throws Exception {
        doNothing().when(cartService).removeItem(USER_EMAIL, PRODUCT_ID);

        mockMvc.perform(delete("/api/cart/{userEmail}/remove/{productId}", USER_EMAIL, PRODUCT_ID)
                .with(csrf()))
                .andExpect(status().isOk());

        verify(cartService, times(1)).removeItem(USER_EMAIL, PRODUCT_ID);
    }

    // Teste DELETE /api/cart/{userEmail}/clear
    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"}) // Simuliert eingeloggten Benutzer
    void testClearCart() throws Exception {
        doNothing().when(cartService).clearCart(USER_EMAIL);

        mockMvc.perform(delete("/api/cart/{userEmail}/clear", USER_EMAIL)
                .with(csrf()))
                .andExpect(status().isOk());

        verify(cartService, times(1)).clearCart(USER_EMAIL);
    }
}
