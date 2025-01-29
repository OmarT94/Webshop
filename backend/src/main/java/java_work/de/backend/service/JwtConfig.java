package java_work.de.backend.service;

import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

public class JwtConfig {
    //JwtConfig class for the shared key

    public static final String SECRET = "mySuperSecretKeyForJWTSigningAndValidation12345"; // Geheimer Schlüssel für die Signierung des Tokens
    public static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
}
