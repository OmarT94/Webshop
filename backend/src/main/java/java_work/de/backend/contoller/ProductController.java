package java_work.de.backend.contoller;
import java_work.de.backend.model.Product;
import java_work.de.backend.service.ProductService;
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
    public List<Product> getProducts() {
        return productService.findAllProducts();
    }

    @GetMapping("/{id}")
    public Product getProduct(@PathVariable String id) {
        return productService.findProductById(id);
    }

    // NUR ADMIN DARF:
    // -> durch die SecurityConfig geregelt
    @PostMapping
    public Product addProduct(@RequestBody Product product) {
        return productService.saveProduct(product);
    }

    @PutMapping("/{id}")
    public Product updateProduct(@PathVariable String id, @RequestBody Product product) {
        return productService.updateProduct(product);
    }

    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);
    }
}
