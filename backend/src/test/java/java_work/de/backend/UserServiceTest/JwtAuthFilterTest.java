package java_work.de.backend.UserServiceTest;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java_work.de.backend.service.JwtAuthFilter;
import java_work.de.backend.service.JwtUtil;
import java_work.de.backend.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private ApplicationContext applicationContext;

    @Mock
    private UserService userService;

    @Mock
    private UserDetailsService userDetailsService;

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
        // (1) Mocks initialisieren
        closeable = MockitoAnnotations.openMocks(this);

        // (2) Danach erst Stubbing
        when(applicationContext.getBean(UserService.class)).thenReturn(userService);

        // (3) Dann Filter konstruieren
        jwtAuthFilter = new JwtAuthFilter(jwtUtil, userDetailsService, applicationContext);
        // (4) SecurityContext leeren, um Tests nicht zu beeinflussen
        SecurityContextHolder.clearContext();

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
    void doFilter_validToken_shouldAuthenticate() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer someValidToken");
        when(jwtUtil.validateToken("someValidToken")).thenReturn("test@example.com");

        jwtAuthFilter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

}
