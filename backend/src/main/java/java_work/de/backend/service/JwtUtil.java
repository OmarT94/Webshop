package java_work.de.backend.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

import static javax.crypto.Cipher.SECRET_KEY;

@Service
public class JwtUtil {

    // Geheimer Schlüssel für die Signierung des Tokens
    private final String SECRET_KEY = "geheimesPasswort";


    /*
     * → Erstellt ein JWT-Token mit Benutzer-E-Mail & Rolle.
     */
    public String generateToken(String username, String role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 Stunden gültig
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }


    /*
     * prüft das Token gültig ist und gibt die Benutzer-E-Mail zurück.
     */
    public String validateToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject(); // E-Mail-Adresse des Benutzers extrahieren
        } catch (JwtException e) {
            return null; // Token ungültig oder abgelaufen
        }
    }
}
