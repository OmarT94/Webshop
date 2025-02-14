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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;


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
        // 1. Mock the request to return a valid JWT token
        when(request.getHeader("Authorization")).thenReturn("Bearer someValidToken");

        // 2. Mock the JWT utility to validate the token and return the username
        when(jwtUtil.validateToken("someValidToken")).thenReturn("test@example.com");

        // 3. Mock the role extraction from JWT
        when(jwtUtil.getRoleFromToken("someValidToken")).thenReturn("ROLE_USER"); // ✅ FIXED

        // 4. Ensure SecurityContextHolder starts clean
        SecurityContextHolder.clearContext();

        // 5. Run the filter
        jwtAuthFilter.doFilter(request, response, filterChain);

        // 6. Verify that the filter proceeds with the request
        verify(filterChain).doFilter(request, response);

        // 7. Check if authentication is correctly set
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication, "Authentication should not be null");
        assertEquals("test@example.com", authentication.getName(), "Username should match the validated token");

        // 8. Check if the role is properly set
        assertTrue(authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")),
                "User should have ROLE_USER authority");
    }

}
