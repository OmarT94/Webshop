package java_work.de.backend.service;

import io.jsonwebtoken.*;
import java_work.de.backend.model.User;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;

@Service
public class JwtUtil {

    /*
     * → Erstellt ein JWT-Token mit Benutzer-E-Mail & Rolle.
     */
    public String generateToken(String email, User.Role role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role",role.name())
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

    //eine Methode, die die Rolle aus dem JWT-Token extrahiert.
    public String getRoleFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(JwtConfig.SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.get("role", String.class); // 🛠️ **Rolle aus dem Token extrahieren**
        } catch (JwtException e) {
            return null; // Falls Token ungültig ist
        }
    }

}
