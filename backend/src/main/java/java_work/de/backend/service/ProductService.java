package java_work.de.backend.service;

import java_work.de.backend.dto.ProductDTO;
import java_work.de.backend.model.Product;
import java_work.de.backend.repo.CategoryRepository;
import java_work.de.backend.repo.ProductRepository;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.support.SimpleMongoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class ProductService {
private final ProductRepository productRepo;
private final CategoryRepository categoryRepo;

    public ProductService(ProductRepository productRepo, CategoryRepository categoryRepo) {
        this.productRepo = productRepo;
        this.categoryRepo = categoryRepo;
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
        // Prüfen, ob die Kategorie existiert
        if (!categoryRepo.existsById(productDTO.categoryId())) {
            throw new IllegalArgumentException("Kategorie existiert nicht!");
        }
        Product newProduct = new Product(
                new ObjectId(),
                productDTO.name(),
                productDTO.description(),
                productDTO.price(),
                productDTO.stock(),
                productDTO.images(), //Hier wird das Base64-Bild gespeichert
                productDTO.categoryId()
        );

        Product savedProduct = productRepo.save(newProduct);
        return mapToDTO(savedProduct); //  Richtiges Produkt zurückgeben
    }


    // Produkt aktualisieren
    public ProductDTO updateProduct(String id, ProductDTO productDTO) {
        Product existingProduct = productRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Produkt mit ID " + id + " nicht gefunden!"));

        Product updatedProduct = new Product(

                existingProduct.id(),
                productDTO.name(),
                productDTO.description(),
                productDTO.price(),
                productDTO.stock(),
                productDTO.images(),
                productDTO.categoryId()
        );

         Product saveProduct = productRepo.save(updatedProduct);
        return mapToDTO(saveProduct);
    }

    // Produkt löschen
    public void deleteProduct(String id) {
        productRepo.deleteById(id);
    }

    //  Suche nach Name
    public List<Product> searchByName(String name) {
        return productRepo.findByNameRegexIgnoreCase(name);
    }

    //  Suche nach Beschreibung (Kategorie)
    public List<Product> searchByCategory(String category) {
        System.out.println("Gesuchte Kategorie: " + category); // Debugging
        List<Product> products = productRepo.findByCategoryContainingIgnoreCase(category);
        System.out.println("Gefundene Produkte: " + products); // Debugging
        return products;
    }

    //  Suche nach Preisbereich
    public List<Product> searchByPriceRange(double minPrice, double maxPrice) {
        return productRepo.findByPriceBetween(minPrice, maxPrice);
    }


    // Mapping Methoden
    private ProductDTO mapToDTO(Product product) {
        return new ProductDTO(
                product.id().toString(), //  ObjectId zu String konvertieren!
                product.name(),
                product.description(),
                product.price(),
                product.stock(),
                product.images(), //  Sende das Bild zurück ans Frontend
                product.categoryId()
        );
    }


    //Trennung von DTO und Entity
    //Klare Mapping-Methoden (mapToDTO, mapToEntity)

}
