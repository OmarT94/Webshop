package java_work.de.backend.service;

import io.jsonwebtoken.*;
import org.springframework.stereotype.Service;
import java.util.Date;

@Service
public class JwtUtil {

    /*
     * → Erstellt ein JWT-Token mit Benutzer-E-Mail & Rolle.
     */
    public String generateToken(String username, String role) {
        return Jwts.builder()
                .setSubject(username)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 Stunden gültig
                .signWith(JwtConfig.SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }


    /*
     * prüft das Token gültig ist und gibt die Benutzer-E-Mail zurück.
     */
    public String validateToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(JwtConfig.SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject(); // E-Mail-Adresse des Benutzers extrahieren
        } catch (JwtException e) {
            return null; // Token ungültig oder abgelaufen
        }
    }
}
