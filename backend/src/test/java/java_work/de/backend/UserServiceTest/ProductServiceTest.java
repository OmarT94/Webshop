package java_work.de.backend.UserServiceTest;

import java_work.de.backend.model.Product;
import java_work.de.backend.repo.ProductRepository;
import java_work.de.backend.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;



class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @BeforeEach
    void setUp() {
        // Initialisiert die @Mock-Objekte
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("findAllProducts: gibt Liste aller Produkte zurück")
    void testFindAllProducts() {
        // Arrange: Wir simulieren, dass das Repo zwei Produkte liefert
        Product p1 = new Product("1", "Laptop", "Desc1", 999.99, 10,"testImage");
        Product p2 = new Product("2", "Smartphone", "Desc2", 499.99, 5,"testImage");
        when(productRepository.findAll()).thenReturn(List.of(p1, p2));

        // Act
        List<Product> result = productService.findAllProducts();

        // Assert
        assertEquals(2, result.size());
        verify(productRepository, times(1)).findAll(); // check Repo-Aufruf
    }

    @Test
    @DisplayName("findProductById: Produkt gefunden")
    void testFindProductById_found() {
        // Arrange
        Product p = new Product("1", "Laptop", "Desc1", 999.99, 10,"testImage");
        when(productRepository.findById("1")).thenReturn(Optional.of(p));

        // Act
        Product result = productService.findProductById("1");

        // Assert
        assertNotNull(result);
        verify(productRepository, times(1)).findById("1");
    }

    @Test
    @DisplayName("findProductById: Produkt nicht gefunden => throw NoSuchElementException")
    void testFindProductById_notFound() {
        // Arrange
        when(productRepository.findById("999")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> productService.findProductById("999"));
        verify(productRepository, times(1)).findById("999");
    }

    @Test
    @DisplayName("saveProduct: Produkt speichern")
    void testSaveProduct() {
        // Arrange
        Product p = new Product(null, "Laptop", "Desc", 999.99, 10,"testImage");
        // Wir simulieren, dass das Repo ein Objekt mit generierter ID zurückgibt
        Product saved = new Product("1", "Laptop", "Desc", 999.99, 10,"testImage");
        when(productRepository.save(p)).thenReturn(saved);

        // Act
        Product result = productService.saveProduct(p);

        // Assert

        verify(productRepository, times(1)).save(p);
    }

    @Test
    @DisplayName("updateProduct: bestehendes Produkt aktualisieren")
    void testUpdateProduct() {
        // Arrange
        Product existing = new Product("1", "Laptop", "OldDesc", 999.99, 10,"testImage");
        Product updated = new Product("1", "Laptop", "NewDesc", 1099.99, 8,"testImage");
        when(productRepository.save(existing)).thenReturn(updated);

        // Act
        Product result = productService.updateProduct(existing);

        // Assert

        verify(productRepository, times(1)).save(existing);
    }

    @Test
    @DisplayName("deleteProduct: Aufruf von deleteById im Repo")
    void testDeleteProduct() {
        // Act
        productService.deleteProduct("1");

        // Assert
        verify(productRepository, times(1)).deleteById("1");
    }
}
