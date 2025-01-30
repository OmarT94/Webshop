package java_work.de.backend.service;

import java_work.de.backend.dto.ProductDTO;
import java_work.de.backend.model.Product;
import java_work.de.backend.repo.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class ProductService {
private final ProductRepository productRepo;

    public ProductService(ProductRepository productRepo) {
        this.productRepo = productRepo;
    }

    public List<ProductDTO> findAllProducts() {
        return productRepo.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public ProductDTO findProductById(String id) {
        return productRepo.findById(id)
                .map(this::mapToDTO)
                .orElseThrow(()
        -> new NoSuchElementException("Kein Produkt mit ID " + id + " gefunden!"));
    }

    public ProductDTO saveProduct(ProductDTO productDTO) {
        Product product = productRepo.save(mapToEntity(productDTO));
        return mapToDTO(product);
    }

    // Produkt aktualisieren
    public ProductDTO updateProduct(String id, ProductDTO productDTO) {
        Product existingProduct = productRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Produkt mit ID " + id + " nicht gefunden!"));

        Product updatedProduct = new Product(
                id,
                productDTO.name(),
                productDTO.description(),
                productDTO.price(),
                productDTO.stock(),
                productDTO.image()
        );

        productRepo.save(updatedProduct);
        return mapToDTO(updatedProduct);
    }

    // Produkt löschen
    public void deleteProduct(String id) {
        productRepo.deleteById(id);
    }

    // Mapping Methoden
    private ProductDTO mapToDTO(Product product) {
        return new ProductDTO(
                product.id(),
                product.name(),
                product.description(),
                product.price(),
                product.stock(),
                product.image()
        );
    }
    private Product mapToEntity(ProductDTO productDTO) {
        return new Product(
                productDTO.id(),
                productDTO.name(),
                productDTO.description(),
                productDTO.price(),
                productDTO.stock(),
                productDTO.image()
        );
    }

    //Trennung von DTO und Entity
    //Klare Mapping-Methoden (mapToDTO, mapToEntity)

}
