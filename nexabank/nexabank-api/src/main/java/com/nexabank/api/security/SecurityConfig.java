package com.nexabank.api.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración de Spring Security.
 *
 * <p>Rutas públicas:
 * <ul>
 *   <li>GET /actuator/health — health check</li>
 *   <li>POST /api/v1/auth/** — autenticación</li>
 * </ul>
 *
 * <p>Rutas privadas (requieren token):
 * <ul>
 *   <li>/api/v1/users/**</li>
 *   <li>/api/v1/accounts/**</li>
 *   <li>/api/v1/transactions/**</li>
 *   <li>/api/v1/transfers/**</li>
 *   <li>/api/v1/beneficiaries/**</li>
 *   <li>/api/v1/algorithms/**</li>
 *   <li>/api/v1/benchmarks/**</li>
 * </ul>
 *
 * <p>API stateless: CSRF deshabilitado, sin sesiones.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final FirebaseAuthenticationFilter firebaseAuthFilter;

    public SecurityConfig(FirebaseAuthenticationFilter firebaseAuthFilter) {
        this.firebaseAuthFilter = firebaseAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF deshabilitado — API stateless
                .csrf(AbstractHttpConfigurer::disable)

                // Sin sesiones HTTP
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Reglas de autorización
                .authorizeHttpRequests(auth -> auth
                        // Rutas públicas
                        .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                        .requestMatchers("/api/v1/auth/**").permitAll()

                        // Todo lo demás requiere autenticación
                        .anyRequest().authenticated()
                )

                // Registrar el filtro de Firebase antes del filtro estándar
                .addFilterBefore(firebaseAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
