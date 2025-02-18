//package java_work.de.backend.ControllerTest;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import java_work.de.backend.contoller.ProductController;
//import java_work.de.backend.dto.ProductDTO;
//import java_work.de.backend.model.Product;
//import java_work.de.backend.service.JwtUtil;
//import java_work.de.backend.service.ProductService;
//import java_work.de.backend.service.SecurityConfig;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.context.annotation.Import;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.web.servlet.MockMvc;
//import java.util.List;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf; // Falls du CSRF benötigst
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//
//@WebMvcTest(ProductController.class)
//@Import(SecurityConfig.class) // Ladet deine echte SecurityConfig
//class ProductControllerTest {
//
//
//    @MockBean
//    private JwtUtil jwtUtil;
//    @MockBean
//    private UserDetailsService userDetailsService;
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    private ProductService productService; // wird als Mock injiziert
//
//    @Autowired
//    private ObjectMapper objectMapper; // zum Umwandeln von Java->JSON oder JSON->Java in den Tests
//
//    @Test
//    @DisplayName("GET /api/products - jeder darf lesen => 200 OK")
//    void getAllProducts_success() throws Exception {
//        // Mock: productService.findAllProducts() => Liste
//        List<ProductDTO> mockProducts = List.of(
//                new ProductDTO( null,"Laptop", "Desc", 999.99, 5,"testImage"),
//                new ProductDTO( null,"Smartphone", "Desc2", 499.99, 2,"testImage")
//        );
//        when(productService.findAllProducts()).thenReturn(mockProducts);
//
//        mockMvc.perform(get("/api/products"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.length()").value(2));
//
//
//        verify(productService, times(1)).findAllProducts();
//    }
//
//    @Test
//    @DisplayName("GET /api/products/{id} - jeder darf lesen => 200 OK")
//    void getProduct_success() throws Exception {
//        ProductDTO mockProd = new  ProductDTO( null,"Laptop", "Desc", 999.99, 5,"testImage");
//        when(productService.findProductById("1")).thenReturn(mockProd);
//
//        mockMvc.perform(get("/api/products/1"))
//                .andExpect(status().isOk())
//        // Optional: .andExpect(jsonPath("$.name").value("Laptop"))
//        ;
//
//        verify(productService).findProductById("1");
//    }
//
//    @Test
//    @DisplayName("POST /api/products - nur ADMIN => 200 OK, normaler User => 403")
//    @WithMockUser(roles = "ADMIN")  // Admin darf
//    void addProduct_admin_success() throws Exception {
//        // Angelegtes Produkt
//        ProductDTO inputProd = new  ProductDTO( null,"Laptop", "Desc", 999.99, 5,"testImage");
//        // Service gibt Produkt mit generierter ID zurück
//        ProductDTO savedProd = new  ProductDTO(null,"Laptop", "Desc", 999.99, 5,"testImage");
//        when(productService.saveProduct(any( ProductDTO.class))).thenReturn(savedProd);
//
//        // JSON des inputProd
//        String prodJson = objectMapper.writeValueAsString(inputProd);
//
//        mockMvc.perform(post("/api/products")
//                        .contentType("application/json")
//                        .content(prodJson)
//                        .with(csrf())  // Falls CSRF aktiviert ist
//                )
//                .andExpect(status().isOk())
//        // optional: .andExpect(jsonPath("$.id").value("123"))
//        ;
//        verify(productService).saveProduct(any( ProductDTO.class));
//    }
//
//    @Test
//    @DisplayName("POST /api/products - als USER => 403 Forbidden")
//    @WithMockUser(roles = "USER")
//    void addProduct_user_forbidden() throws Exception {
//        ProductDTO inputProd = new  ProductDTO( null,"Laptop", "Desc", 999.99, 5,"testImage");
//        String prodJson = new ObjectMapper().writeValueAsString(inputProd);
//
//        mockMvc.perform(post("/api/products")
//                        .contentType("application/json")
//                        .content(prodJson)
//                        .with(csrf()) // no effect if .csrf().disable() but safer
//                )
//                .andExpect(status().isForbidden()); // 403
//    }
//
//    @Test
//    @DisplayName("PUT /api/products/{id} - nur ADMIN => 200 OK")
//    @WithMockUser(roles = "ADMIN")
//    void updateProduct_admin_success() throws Exception {
//        ProductDTO updated = new  ProductDTO( null,"Laptop Updated", "Desc2", 1099.99, 4,"testImage");
//        when(productService.updateProduct(eq("1"),any( ProductDTO.class))).thenReturn(updated);
//
//        // JSON
//        String updatedJson = objectMapper.writeValueAsString(updated);
//
//        mockMvc.perform(put("/api/products/1")
//                        .contentType("application/json")
//                        .content(updatedJson)
//                        .with(csrf()))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.name").value("Laptop Updated"));
//        verify(productService).updateProduct(eq("1"),any( ProductDTO.class));
//    }
//
//    @Test
//    @DisplayName("DELETE /api/products/{id} - nur ADMIN => 200 OK")
//    @WithMockUser(roles = "ADMIN")
//    void deleteProduct_admin_success() throws Exception {
//        mockMvc.perform(delete("/api/products/1")
//                        .with(csrf())
//                )
//                .andExpect(status().isOk());
//
//
//        verify(productService).deleteProduct("1");
//    }
//
//    @Test
//    @DisplayName("DELETE /api/products/{id} - als USER => 403 Forbidden")
//    @WithMockUser(roles = "USER")
//    void deleteProduct_user_forbidden() throws Exception {
//        mockMvc.perform(delete("/api/products/1")
//                        .with(csrf())
//                )
//                .andExpect(status().isForbidden());
//    }
//
//}
