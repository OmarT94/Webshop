package java_work.de.backend.contoller;

import java_work.de.backend.model.User;
import java_work.de.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    public AuthController(UserService userService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestParam String username, @RequestParam String password) {
        if (userService.findByUsername(username).isPresent()) {
            return ResponseEntity.badRequest().body("Benutzername existiert bereits!");
        }
        // Standardmäßig als 'USER' registrieren
        userService.registerUser(username, password, User.Role.ROLE_USER);
        return ResponseEntity.ok("Registrierung erfolgreich!");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam String username, @RequestParam String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return ResponseEntity.ok("Login erfolgreich!");
    }

    @PutMapping("/me")
    public ResponseEntity<String> updateMyDetails(
            @RequestParam(required = false) String newUsername,
            @RequestParam(required = false) String newPassword) {

        // Den aktuell eingeloggten Benutzer aus dem SecurityContext holen
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        try {
            // Benutzerdaten aktualisieren (nur für den eingeloggten Benutzer)
            userService.updateUserDetails(currentUsername, newUsername, newPassword);
            return ResponseEntity.ok("Deine Daten wurden erfolgreich aktualisiert!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }



}
