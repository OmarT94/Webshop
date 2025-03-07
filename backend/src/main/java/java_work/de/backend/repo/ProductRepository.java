package java_work.de.backend.repo;

import java_work.de.backend.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ProductRepository extends MongoRepository<Product, String> {

    //  Suche nach Name (case-insensitive)
    @Query("{ 'name': { $regex: ?0, $options: 'i' } }")
    List<Product> findByNameRegexIgnoreCase(String name);

    //  Suche nach Kategorie
    @Query("{ 'category': { $regex: ?0, $options: 'i' } }")
    List<Product> findByCategoryContainingIgnoreCase(String category);

    // Produkte nach Kategorie abrufen
    List<Product> findByCategoryId(String categoryId);

    //  Suche nach Preisbereich
    @Query("{ 'price': { $gte: ?0, $lte: ?1 } }")
    List<Product> findByPriceBetween(double minPrice, double maxPrice);


}
