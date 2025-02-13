package java_work.de.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
public class SecurityConfig {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final String ROLE_ADMIN = "ROLE_ADMIN"; // Vermeidet Duplizierung!

    // `JwtAuthFilter` als Bean registrieren (Circular Dependency verhindern)
    @Bean
    public JwtAuthFilter jwtAuthFilter(ApplicationContext applicationContext , JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        return new JwtAuthFilter(jwtUtil, userDetailsService, applicationContext);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter) throws Exception {
        logger.info("🔍 Lade Sicherheitskonfiguration..."); // Richtig platziert!

        http
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(List.of("http://localhost:5173"));
                    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    config.setAllowedHeaders(List.of("*"));
                    config.setAllowCredentials(true);
                    return config;
                }))
                .csrf(AbstractHttpConfigurer::disable) // CSRF-Schutz deaktivieren
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(HttpMethod.GET, "/api/products/**").permitAll();
                    auth.requestMatchers("/api/auth/register", "/api/auth/login").permitAll();

                    //  Admin-Rechte
                    auth.requestMatchers("/api/admin/**").hasAnyAuthority(ROLE_ADMIN);
                    auth.requestMatchers(HttpMethod.POST, "/api/products/**").hasAnyAuthority(ROLE_ADMIN);
                    auth.requestMatchers(HttpMethod.PUT, "/api/products/**").hasAnyAuthority(ROLE_ADMIN);
                    auth.requestMatchers(HttpMethod.DELETE, "/api/products/**").hasAnyAuthority(ROLE_ADMIN);

                    // Admin darf alle Bestellungen verwalten:
                    auth.requestMatchers(HttpMethod.GET, "/api/orders").hasAuthority(ROLE_ADMIN);
                    auth.requestMatchers(HttpMethod.PUT, "/api/orders/**").hasAuthority(ROLE_ADMIN);
                    auth.requestMatchers(HttpMethod.DELETE, "/api/orders/{orderId}/cancel").hasAnyAuthority("ROLE_USER", ROLE_ADMIN);

                    //   Hier sind deine PUT-Regeln korrekt eingefügt!
                    //   Debugging: Logge, ob die Sicherheitsregel greift
                    logger.info("🛠 Setze Sicherheitsregel für Rückgabe-Anfrage...");
                    auth.requestMatchers(HttpMethod.PUT, "/api/orders/{orderId}/return_request").authenticated();

                    //  User darf eigene Bestellungen abrufen:
                    auth.requestMatchers(HttpMethod.GET, "/api/orders/{userEmail}").authenticated();
                    auth.requestMatchers(HttpMethod.POST, "/api/cart/**").authenticated();

                    //  Alle anderen Endpunkte benötigen Authentifizierung:
                    auth.anyRequest().authenticated();
                })
                .httpBasic(httpBasic -> httpBasic.disable()) // `httpBasic()` korrekt deaktivieren
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        logger.info(" Sicherheitsregeln geladen!");
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
