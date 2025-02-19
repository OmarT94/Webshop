package java_work.de.backend.repo;

import java_work.de.backend.model.Address;
import java_work.de.backend.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;

import java.util.List;
import java.util.Optional;
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);

    // **Nur `addresses`-Feld des Benutzers aktualisieren**
    @Query("{ 'email': ?0 }")  // Finde Benutzer mit passender Email
    @Update("{ '$set': { 'addresses': ?1 } }")  // Setzt neue Adressen in `addresses`-Feld
    void updateAddressesByEmail(String email, List<Address> addresses);


}
