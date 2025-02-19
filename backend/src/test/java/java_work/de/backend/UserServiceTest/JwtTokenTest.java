package java_work.de.backend.UserServiceTest;


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java_work.de.backend.model.User;
import java_work.de.backend.service.JwtConfig;
import java_work.de.backend.service.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();

    }

    @Test
    void validateToken_InvalidToken_ReturnsNull() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act
        String result = jwtUtil.validateToken(invalidToken);

        // Assert
        assertNull(result, "Expected null for invalid token");
    }

    @Test
    void validateToken_ExpiredToken_ReturnsNull() {
        // Arrange
        String expiredToken = Jwts.builder()
                .setSubject("charlie@example.com")
                .claim("role", "ROLE_USER")
                .setIssuedAt(new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 12)) // 12 hours ago (expired)
                .setExpiration(new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 10)) // 10 hours ago (expired)
                .signWith(JwtConfig.SECRET_KEY, SignatureAlgorithm.HS256) // Signatur mit SecretKey
                .compact();

        // Act
        String result = jwtUtil.validateToken(expiredToken);

        // Assert
        assertNull(result, "Expected null for expired token");
    }

    @Test
    void validateToken_ValidToken_ReturnsUsername() {
        // Arrange: Erstelle ein gültiges Token mit der korrekten SecretKey
        String validToken = Jwts.builder()
                .setSubject("charlie@example.com")
                .claim("role", "ROLE_USER")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 Stunden Gültigkeit
                .signWith(JwtConfig.SECRET_KEY, SignatureAlgorithm.HS256) // Signiere mit demselben Schlüssel
                .compact();

        // Act: Token validieren
        String result = jwtUtil.validateToken(validToken);

        // Assert: Der Benutzername muss übereinstimmen
        assertEquals("charlie@example.com", result, "Expected username for valid token");
    }

    @Test
    @DisplayName("Manipuliertes JWT-Token nicht validieren")
    void validateToken_tampered_fail() {
        // Arrange
        User user = new User(
                "testUser@example.com",
                "hashedPassword",
                "Max",
                "Mustermann",
                User.Role.ROLE_USER
        );

        String validToken = jwtUtil.generateToken(user);

        // Simuliere eine Manipulation am Token (einfach ein Zeichen hinzufügen)
        String tamperedToken = validToken + "1";

        // Act
        String result = jwtUtil.validateToken(tamperedToken);

        // Assert
        assertNull(result, "Manipuliertes Token sollte nicht validiert werden");
    }

    @Test
    @DisplayName("Soll die Rolle aus einem gültigen Token extrahieren")
    void getRoleFromToken_ShouldReturnRole_WhenTokenIsValid() {
        // Given: Ein gültiges JWT-Token mit der Rolle "ROLE_USER"
        String token = generateToken("", User.Role.valueOf("ROLE_USER"));

        // When: Die Methode wird aufgerufen
        String role = jwtUtil.getRoleFromToken(token);

        // Then: Die extrahierte Rolle sollte "ROLE_USER" sein
        assertNotNull(role, "Rolle sollte nicht null sein");
        assertEquals("ROLE_USER", role, "Die extrahierte Rolle sollte korrekt sein");
    }

    @Test
    @DisplayName("Soll null zurückgeben, wenn das Token ungültig ist")
    void getRoleFromToken_ShouldReturnNull_WhenTokenIsInvalid() {
        // Given: Ein ungültiges Token
        String invalidToken = "invalidToken123";

        // When: Die Methode wird aufgerufen
        String role = jwtUtil.getRoleFromToken(invalidToken);

        // Then: Rolle sollte null sein
        assertNull(role, "Bei einem ungültigen Token sollte null zurückgegeben werden");
    }
    public String generateToken(String email, User.Role role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role",role.name())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 Stunden gültig
                .signWith(JwtConfig.SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

}
