package java_work.de.backend.contoller;

import jakarta.validation.Valid;
import java_work.de.backend.dto.UserLoginDTO;
import java_work.de.backend.dto.UserPasswordUpdateDTO;
import java_work.de.backend.dto.UserRegistrationDTO;
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
    public ResponseEntity<String> registerUser(@Valid @RequestBody UserRegistrationDTO dto) {
        if (userService.findByEmail(dto.email()).isPresent()) {
            return ResponseEntity.badRequest().body("E-Mail existiert bereits!");
        }
        // Standardmäßig als 'USER' registrieren
        userService.registerUser(dto.email(), dto.password(), User.Role.ROLE_USER);
        return ResponseEntity.ok("Registrierung erfolgreich!");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody UserLoginDTO dto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.email(), dto.password())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return ResponseEntity.ok("Login erfolgreich!");
    }

    @PutMapping("/me")
    public ResponseEntity<String> updateMyDetails(
            @Valid @RequestBody UserPasswordUpdateDTO dto) {

        // Den aktuell eingeloggten Benutzer aus dem SecurityContext holen
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        try {
            // Benutzerdaten aktualisieren (nur für den eingeloggten Benutzer)
            userService.updateUserPassword(currentEmail,dto.newPassword());
            return ResponseEntity.ok("Deine Daten wurden erfolgreich aktualisiert!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }



}
