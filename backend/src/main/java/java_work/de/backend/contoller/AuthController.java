package java_work.de.backend.contoller;

import jakarta.validation.Valid;
import java_work.de.backend.dto.UserLoginDTO;
import java_work.de.backend.dto.UserPasswordUpdateDTO;
import java_work.de.backend.dto.UserRegistrationDTO;
import java_work.de.backend.model.User;
import java_work.de.backend.service.JwtUtil;
import java_work.de.backend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    public AuthController(UserService userService, AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }


    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@Valid @RequestBody UserRegistrationDTO dto) {
        if (userService.findByEmail(dto.email()).isPresent()) {
            return ResponseEntity.badRequest().body("E-Mail existiert bereits!");
        }
        // Falls keine Rolle gesendet wird, Standard: ROLE_USER
        User.Role userRole = (dto.role() != null && dto.role().equalsIgnoreCase("ROLE_ADMIN"))
                ? User.Role.ROLE_ADMIN
                : User.Role.ROLE_USER;
        userService.registerUser(dto.email(), dto.password(), dto.firstName(), dto.lastName(), userRole);
        return ResponseEntity.ok("Registrierung erfolgreich!");
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody UserLoginDTO dto) {
        // Benutzer authentifizieren
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.email(), dto.password())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Benutzerinformationen aus Authentication-Objekt abrufen
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // Benutzer aus der Datenbank holen, um die Rolle zu erhalten
        User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Benutzer nicht gefunden!"));

        // JWT-Token erstellen
        String token = jwtUtil.generateToken(user.email(), user.role());

        // Antwort mit Token zurückgeben (als JSON-Objekt)
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        return ResponseEntity.ok(response);
    }

    //BadCredentialsException ist eine Exception in Spring Security, die geworfen wird,
    // wenn die Anmeldeinformationen (Benutzername/Passwort) ungültig sind.
    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED) //  Gibt jetzt 401 zurück
    public ResponseEntity<Map<String, String>> handleBadCredentials(BadCredentialsException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Falsche Anmeldedaten");
        response.put("status", "401");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
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
