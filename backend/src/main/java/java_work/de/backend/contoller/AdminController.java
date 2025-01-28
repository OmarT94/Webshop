package java_work.de.backend.contoller;

import java_work.de.backend.model.User;
import java_work.de.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userService.findAllUsers();
    }

    // Rolle eines Benutzers ändern
    @PutMapping("/users/{email}/role")
    public ResponseEntity<String> changeUserRole(@PathVariable String email, @RequestParam String role) {
        Optional<User> userOptional = userService.findByEmail(email);
        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("Benutzer nicht gefunden!");
        }

        try {
            // Neue Rolle aus dem übergebenen String extrahieren
            User.Role newRole = User.Role.valueOf(role);

            // Benutzer aus der Datenbank holen und neues User-Objekt mit neuer Rolle erstellen
            User existingUser = userOptional.get();
            User updatedUser = new User(
                    existingUser.id(),
                    existingUser.email(),
                    existingUser.password(),
                    newRole
            );

            // Geänderten Benutzer speichern
            userService.save(updatedUser);
            return ResponseEntity.ok("Rolle erfolgreich geändert!");

        } catch (IllegalArgumentException e) {
            // Fehlerbehandlung, falls eine ungültige Rolle übergeben wird
            return ResponseEntity.badRequest().body("Ungültige Rolle! Verfügbare Rollen: ROLE_ADMIN, ROLE_USER");
        }
    }

    @PutMapping("/users/{id}/email")
    public ResponseEntity<String> updateUserEmail(
            @PathVariable String id,
            @RequestParam String newEmail) {
        try {
            // Admin ändert die Benutzerdaten eines spezifischen Benutzers
            userService.updateUserEmailByAdmin(id,  newEmail);
            return ResponseEntity.ok("Benutzerdetails erfolgreich aktualisiert!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
