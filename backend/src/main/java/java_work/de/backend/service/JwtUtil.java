package java_work.de.backend.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtUtil {

    // Geheimer Schlüssel für die Signierung des Tokens
    private final SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // Token-Gültigkeitsdauer (1 Tag)
    private final long EXPIRATION_TIME = 24 * 60 * 60 * 1000;

    /*
     * Erstellt ein JWT-Token für einen Benutzer.
     */
    public String generateToken(String email, String role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role) // Benutzerrolle als Claim speichern
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(secretKey)
                .compact();
    }

    /*
     * Validiert ein JWT-Token und gibt die E-Mail aus dem Payload zurück.
     */
    public String validateToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject(); // E-Mail-Adresse des Benutzers extrahieren
        } catch (JwtException e) {
            return null; // Token ungültig oder abgelaufen
        }
    }
}
