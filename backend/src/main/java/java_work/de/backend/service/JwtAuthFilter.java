package java_work.de.backend.service;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/*
Damit Spring Security JWT automatisch verarbeitet,
brauchen wir einen Filter, der bei jeder Anfrage den Token prüft.
*/

public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final ApplicationContext applicationContext;  // <--- Feld hinzufügen

    private UserService userService; // Lazy-Loading

    public JwtAuthFilter(JwtUtil jwtUtil,
                         UserDetailsService userDetailsService,
                         ApplicationContext applicationContext) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.applicationContext = applicationContext;      // <--- Im Konstruktor zuweisen
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        String email = jwtUtil.validateToken(token);
        String roleString = jwtUtil.getRoleFromToken(token); // Rolle aus Token extrahieren

        if (email != null && roleString != null && !roleString.isEmpty()) {
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority(roleString); //  Rolle richtig setzen!

            UserDetails userDetails = new User(
                    email,
                    "",
                    List.of(authority) //  Rolle als Authority übergeben!
            );

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                    );

            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        chain.doFilter(request, response);
    }

}
