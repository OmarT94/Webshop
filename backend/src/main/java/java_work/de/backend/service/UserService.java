package java_work.de.backend.service;

import java_work.de.backend.model.User;
import java_work.de.backend.repo.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 1) loadUserByUsername: Wird von Spring Security beim Login aufgerufen.
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Benutzer aus der Datenbank laden
        User userRecord = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Benutzer nicht gefunden: " + username));

        // in ein Spring-Security-UserDetails-Objekt umwandeln
        // userRecord.role() könnte z. B. "ROLE_USER" oder "ROLE_ADMIN" sein
        return org.springframework.security.core.userdetails.User
                .withUsername(userRecord.username())
                .password(userRecord.password()) // gehashter Wert
                // Spring erwartet die Kurzform der Rolle, z. B. "USER" statt "ROLE_USER"
                .roles(userRecord.role().name().replace("ROLE_", ""))
                .build();
    }

    // 2) Benutzer registrieren
    public void registerUser(String username, String password, User.Role role) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Benutzername existiert bereits!");
        }

        String encryptedPassword = passwordEncoder.encode(password);
        User user = new User(
                null, // ID durch DB generiert
                username,
                encryptedPassword,
                role
        );
        userRepository.save(user);
    }

    // 3) Weitere Methoden (CRUD)
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public void save(User user) {
        userRepository.save(user);
    }

    public void deleteUserById(String id) {
        userRepository.deleteById(id);
    }

    public void updateUserDetails(String username, String newUsername, String newPassword) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            User existingUser = userOptional.get();
            User updatedUser = new User(
                    existingUser.id(),
                    (newUsername != null) ? newUsername : existingUser.username(),
                    (newPassword != null) ? passwordEncoder.encode(newPassword) : existingUser.password(),
                    existingUser.role()
            );
            userRepository.save(updatedUser);
        } else {
            throw new IllegalArgumentException("Benutzer nicht gefunden!");
        }
    }
}
