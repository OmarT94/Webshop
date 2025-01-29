package java_work.de.backend.service;

import java_work.de.backend.model.Product;
import java_work.de.backend.repo.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class ProductService {
private final ProductRepository productRepo;

    public ProductService(ProductRepository productRepo) {
        this.productRepo = productRepo;
    }

    public List<Product> findAllProducts() {
        return productRepo.findAll();
    }

    public Product findProductById(String id) {
        return productRepo.findById(id)
                .orElseThrow(()
        -> new NoSuchElementException("Kein Produkt mit ID " + id + " gefunden!"));
    }

    public Product saveProduct(Product product) {
        return productRepo.save(product);
    }

    public Product updateProduct(Product product) {
        return productRepo.save(product);
    }

    public void deleteProduct(String id) {
        productRepo.deleteById(id);
    }


}
