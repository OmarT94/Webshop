package java_work.de.backend.UserServiceTest;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java_work.de.backend.service.JwtAuthFilter;
import java_work.de.backend.service.JwtUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        SecurityContextHolder.clearContext();  // WICHTIG: Context vorher löschen!
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
        SecurityContextHolder.clearContext();  // WICHTIG: Context nach jedem Test leeren!
    }

    @Test
    @DisplayName("Ungültiges Token sollte nicht authentifizieren")
    void doFilter_invalidToken_shouldNotAuthenticate() throws ServletException, IOException {
        // Arrange: Ungültiges Token simulieren
        String token = "invalid-jwt-token";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.validateToken(token)).thenReturn(null);

        // Act
        jwtAuthFilter.doFilter(request, response, filterChain);

        // Assert
        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication(), "SecurityContext sollte keine Authentifizierung enthalten.");
    }

    @Test
    @DisplayName("Gültiges Token sollte authentifizieren")
    void doFilter_validToken_shouldAuthenticate() throws ServletException, IOException {
        // Arrange: Gültiges Token simulieren
        String token = "valid-jwt-token";
        String email = "test@example.com";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(jwtUtil.validateToken(token)).thenReturn(email);

        UserDetails userDetails = new User(email, "", Collections.singleton(() -> "ROLE_USER"));

        // Act
        jwtAuthFilter.doFilter(request, response, filterChain);

        // Assert
        verify(filterChain, times(1)).doFilter(request, response);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth, "SecurityContext sollte eine Authentifizierung enthalten.");
        assertEquals(email, ((User) auth.getPrincipal()).getUsername(), "Der Benutzername sollte mit dem extrahierten Email übereinstimmen.");
    }
}
