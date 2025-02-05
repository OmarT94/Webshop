package java_work.de.backend.service;


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



    // `JwtAuthFilter` als Bean registrieren (Circular Dependency verhindern)
    @Bean
    public JwtAuthFilter jwtAuthFilter(ApplicationContext applicationContext , JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        return new JwtAuthFilter(jwtUtil, userDetailsService, applicationContext);
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter ) throws Exception {
        http
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    config.setAllowedOrigins(List.of("http://localhost:5173"));
                    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                    config.setAllowedHeaders(List.of("*"));
                    config.setAllowCredentials(true);
                    return config;
                }))
                .csrf(AbstractHttpConfigurer::disable) // CSRF-Schutz deaktivieren (neue API)
                // CSRF-Schutz ausschalten
                .authorizeHttpRequests(auth -> auth
                        // Authentifizierung für Auth-Routen erlauben
                        // Lesen von Produkten (GET) ist jedem erlaubt
                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                        .requestMatchers("/api/auth/register", "/api/auth/login").permitAll() //  Registrierung und Login für alle freigeben
                        // Admin-Endpunkte
                       // .requestMatchers("/api/products/**").permitAll()   all users can add/delete/update
                        .requestMatchers("/api/admin/**").hasAnyAuthority("ROLE_ADMIN")
                        // Nur Admin darf Produkte erstellen, bearbeiten, löschen
                        .requestMatchers(HttpMethod.POST, "/api/products/**").hasAnyAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/products/**").hasAnyAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasAnyAuthority("ROLE_ADMIN")

                        //  Admin darf alle Bestellungen verwalten:
                        .requestMatchers(HttpMethod.GET, "/api/orders").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/orders/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/orders/{orderId}/cancel").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")

                        //  User kann eigene Bestellungen abrufen:
                        .requestMatchers(HttpMethod.GET, "/api/orders/{userEmail}").authenticated()

                        // Alle anderen Endpunkte benötigen eine Authentifizierung
                        .anyRequest().authenticated()

                )
                .httpBasic(httpBasic -> httpBasic.disable())// `httpBasic()` korrekt deaktivieren
                .addFilterBefore(jwtAuthFilter,UsernamePasswordAuthenticationFilter.class);



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
