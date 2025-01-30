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

    /*
     * Wird von Spring Security beim Login aufgerufen.
     * Hier behandeln wir 'email' als Schlüssel statt 'username'.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Benutzer aus der Datenbank laden
        User userRecord = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Benutzer nicht gefunden: " + email));

        // in ein Spring-Security-UserDetails-Objekt umwandeln
        // userRecord.role() könnte z. B. "ROLE_USER" oder "ROLE_ADMIN" sein
        return org.springframework.security.core.userdetails.User
                .withUsername(userRecord.email())            // E-Mail als "Username" im Security-Kontext
                .password(userRecord.password())             // gehashter Wert
                // Spring erwartet die Kurzform der Rolle, z. B. "USER" statt "ROLE_USER"
                .roles(userRecord.role().name().replace("ROLE_", ""))
                .build();
    }

    /*
     * Registrierung eines neuen Benutzers mit E-Mail und Passwort.
     * Role kann ROLE_USER oder ROLE_ADMIN sein.
     */
    public User registerUser(String email, String password, User.Role role) {
        // Überprüfen, ob die E-Mail bereits existiert
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Ein Benutzer mit dieser E-Mail existiert bereits!");
        }


        String encryptedPassword = passwordEncoder.encode(password);
        User user = new User(
                null, // ID durch DB generiert
                email,
                encryptedPassword,
                role
        );
        return userRepository.save(user);
    }

    /*
     * Liste aller Benutzer (z. B. für Admin).
     */
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    /*
     * Einzelnen Benutzer anhand der E-Mail finden.
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /*
     * Speichert einen Benutzer-Datensatz direkt (z. B. bei Updates).
     */
    public void saveUser(User user) {
        userRepository.save(user);
    }

    /*
     * Löscht einen Benutzer über die ID.
     */
    public void deleteUserById(String id) {
        // Prüfung, ob User existiert
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("Benutzer nicht gefunden!");
        }
        userRepository.deleteById(id);
    }

    /*
     * Ein normaler Benutzer kann sein Passwort ändern.
     * E-Mail bleibt unverändert.
     */
    public void updateUserPassword(String email, String newPassword) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("Benutzer nicht gefunden!");
        }
        User existingUser = userOptional.get();

        User updatedUser = new User(
                existingUser.id(),
                existingUser.email(), // E-Mail bleibt unverändert
                (newPassword != null) ? passwordEncoder.encode(newPassword) : existingUser.password(),
                existingUser.role()
        );
        userRepository.save(updatedUser);
    }

    /*
     * Nur für Admin: E-Mail eines Benutzers anhand der ID ändern.
     * Rolle und Passwort bleiben unverändert.
     */
    public void updateUserEmailByAdmin(String userId, String newEmail) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("Benutzer nicht gefunden!");
        }
        // Prüfen, ob die neue E-Mail bereits existiert
        if (userRepository.findByEmail(newEmail).isPresent()) {
            throw new IllegalArgumentException("E-Mail wird bereits verwendet!");
        }

        User existingUser = userOptional.get();
        User updatedUser = new User(
                existingUser.id(),
                newEmail,
                existingUser.password(), // Passwort bleibt unverändert
                existingUser.role()
        );
        userRepository.save(updatedUser);
    }
}
