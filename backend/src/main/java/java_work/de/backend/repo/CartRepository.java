package java_work.de.backend.repo;

import java_work.de.backend.model.Cart;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CartRepository extends MongoRepository<Cart, String> {
    Optional<Cart> findByUserEmail(String userEmail); //Warenkorb eines Nutzers abrufen
}
