package cat.itacademy.virtualpet.config; // Mantener dentro del package base para que Spring la detecte

/* ============================== IMPORTS ============================== */
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import cat.itacademy.virtualpet.infrastructure.security.JwtAuthenticationFilter;

/**
 * Configuración de seguridad de la API.
 * - CORS activo para el front (localhost:5173).
 * - CSRF desactivado (API REST + JWT).
 * - STATELESS (sin sesiones en servidor).
 * - Rutas públicas: /auth/**, Swagger (/v3/api-docs/**, /swagger-ui/**, /swagger-ui.html) y OPTIONS.
 * - /admin/** solo accesible a ROLE_ADMIN.
 * - 401 cuando no hay auth; 403 cuando faltan permisos.
 * - Inserta el filtro JWT antes del UsernamePasswordAuthenticationFilter.
 */
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                /* ---- CORS ---- */
                .cors(c -> {})

                /* ---- CSRF ---- */
                .csrf(csrf -> csrf.disable())

                /* ---- STATELESS ---- */
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                /* ---- AUTORIZACIÓN DE RUTAS ---- */
                .authorizeHttpRequests(auth -> auth
                        // Preflight del navegador
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Swagger / OpenAPI públicos
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        // Auth público
                        .requestMatchers("/auth/**").permitAll()
                        // Rutas de error
                        .dispatcherTypeMatchers(DispatcherType.ERROR, DispatcherType.FORWARD).permitAll()
                        .requestMatchers("/error").permitAll()
                        // --- RUTAS ADMIN ---
                        .requestMatchers("/admin/**").hasRole("ADMIN")  // solo ROLE_ADMIN puede acceder
                        // --- RESTO ---
                        .anyRequest().authenticated()
                )

                /* ---- RESPUESTAS POR DEFECTO ---- */
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) ->
                                res.sendError(HttpServletResponse.SC_UNAUTHORIZED)) // 401 → sin token
                        .accessDeniedHandler((req, res, e) ->
                                res.sendError(HttpServletResponse.SC_FORBIDDEN))    // 403 → sin permiso
                )

                /* ---- SIN FORM LOGIN / BASIC ---- */
                .formLogin(f -> f.disable())
                .httpBasic(b -> b.disable())

                /* ---- FILTRO JWT ---- */
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configuración CORS (para front local en puerto 5173).
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://127.0.0.1:5173"
        ));
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        cfg.setAllowCredentials(false);
        cfg.setMaxAge(3600L); // cachear preflight 1h

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

    /**
     * Password encoder (BCrypt) para hashear y validar contraseñas.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
