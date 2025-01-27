package java_work.de.backend.service;


import java_work.de.backend.model.User;
import java_work.de.backend.repo.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    public User registerUser(String username, String password, User.Role role) {
        String encodePassword=passwordEncoder.encode(password);
        User user = new User(null,username, encodePassword,role);
        return userRepository.save(user);
    }



}
