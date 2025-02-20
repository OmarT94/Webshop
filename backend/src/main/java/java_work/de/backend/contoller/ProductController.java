package java_work.de.backend.contoller;
import jakarta.validation.Valid;
import java_work.de.backend.dto.ProductDTO;
import java_work.de.backend.model.Product;
import java_work.de.backend.service.ProductService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // JEDER DARF LESEN (GET):
    @GetMapping
    public List<ProductDTO> getProducts() {
        return productService.findAllProducts();
    }

    @GetMapping("/{id}")
    public ProductDTO getProduct(@PathVariable String id) {
        return productService.findProductById(id);
    }

    // NUR ADMIN DARF:
    // -> durch die SecurityConfig geregelt
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping
    public ProductDTO addProduct(@Valid @RequestBody ProductDTO productDTO) {
        return productService.saveProduct(productDTO);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ProductDTO updateProduct(@PathVariable String id,  @Valid @RequestBody ProductDTO productDTO) {
        return productService.updateProduct(id, productDTO);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);
    }


    //  Suche nach Name
    @GetMapping("/search/name")
    public List<Product> searchByName(@RequestParam String name) {
        return productService.searchByName(name);
    }

    //  Suche nach Kategorie
    @GetMapping("/search/category")
    public List<Product> searchByCategory(@RequestParam String category) {
        return productService.searchByCategory(category);
    }

    //  Suche nach Preisbereich
    @GetMapping("/search/price")
    public List<Product> searchByPrice(@RequestParam double minPrice, @RequestParam double maxPrice) {
        return productService.searchByPriceRange(minPrice, maxPrice);
    }
}

