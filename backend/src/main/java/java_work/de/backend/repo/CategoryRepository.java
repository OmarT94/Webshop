package java_work.de.backend.repo;

import java_work.de.backend.model.Category;
import org.springframework.data.mongodb.repository.MongoRepository;



import java.util.Optional;

public interface CategoryRepository extends MongoRepository<Category, String> {


    //  Prüfen, ob eine Kategorie existiert
    boolean existsByName(String name);

    //  Kategorie nach Name abrufen
    Optional<Category> findByName(String name);
}
