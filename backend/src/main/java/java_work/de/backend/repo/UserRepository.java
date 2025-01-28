package java_work.de.backend.repo;
import java_work.de.backend.model.User;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);


}
