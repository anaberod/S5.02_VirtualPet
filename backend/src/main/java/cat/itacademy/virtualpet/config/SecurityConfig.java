package cat.itacademy.virtualpet.config; // Debe estar bajo el package base para que Spring la detecte

/* ============================== IMPORTS ============================== */
import jakarta.servlet.DispatcherType;                         // Distinguir ERROR/FORWARD dispatch
import jakarta.servlet.http.HttpServletResponse;               // Enviar 401/403 personalizados
import lombok.RequiredArgsConstructor;                         // Inyección por constructor (Lombok)
import org.springframework.context.annotation.Bean;            // Declarar beans
import org.springframework.context.annotation.Configuration;   // Clase de configuración
import org.springframework.http.HttpMethod;                    // Métodos HTTP
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity; // @PreAuthorize...
import org.springframework.security.config.annotation.web.builders.HttpSecurity;                 // Reglas de seguridad
import org.springframework.security.config.http.SessionCreationPolicy;                           // STATELESS
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;                         // BCrypt
import org.springframework.security.crypto.password.PasswordEncoder;                             // PasswordEncoder
import org.springframework.security.web.SecurityFilterChain;                                     // Cadena de filtros
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;     // Para insertar el filtro JWT antes
import org.springframework.web.cors.CorsConfiguration;                                           // CORS
import org.springframework.web.cors.CorsConfigurationSource;                                     // Fuente CORS
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;                             // CORS por rutas
import java.util.List;

// 👉 Tu filtro JWT (Paso 6)
import cat.itacademy.virtualpet.security.jwt.JwtAuthenticationFilter;

/**
 * Configuración de seguridad de la API.
 * - CORS activo para el front (localhost:5173).
 * - CSRF desactivado (API REST + JWT).
 * - STATELESS (sin sesiones en servidor).
 * - Rutas públicas: /auth/**, Swagger (/v3/api-docs/**, /swagger-ui/**, /swagger-ui.html) y OPTIONS.
 * - 401 cuando no hay auth; 403 cuando faltan permisos.
 * - Permite /error y dispatch ERROR/FORWARD para obtener 404 correctos.
 * - Inserta el JwtAuthenticationFilter antes del UsernamePasswordAuthenticationFilter.
 */
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // Inyectamos el filtro que valida Authorization: Bearer <token>
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Define la cadena de filtros y las reglas de autorización.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                /* ---- CORS ---- */
                .cors(c -> {})

                /* ---- CSRF ----
                   En APIs stateless con JWT, se desactiva CSRF para evitar 403 en POST/PUT/DELETE. */
                .csrf(csrf -> csrf.disable())

                /* ---- STATELESS ---- */
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                /* ---- AUTORIZACIÓN DE RUTAS ---- */
                .authorizeHttpRequests(auth -> auth
                        // Preflight del navegador
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Swagger / OpenAPI públicos (para probar desde UI)
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        // Auth público
                        .requestMatchers("/auth/**").permitAll()
                        // /error y dispatch de errores → públicos para que endpoints inexistentes devuelvan 404 (no 403)
                        .dispatcherTypeMatchers(DispatcherType.ERROR, DispatcherType.FORWARD).permitAll()
                        .requestMatchers("/error").permitAll()
                        // Todo lo demás requiere autenticación (JWT)
                        .anyRequest().authenticated()
                )

                /* ---- RESPUESTAS POR DEFECTO ----
                   401 si no estás autenticado; 403 si te falta permiso con token válido. */
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) ->
                                res.sendError(HttpServletResponse.SC_UNAUTHORIZED)) // 401
                        .accessDeniedHandler((req, res, e) ->
                                res.sendError(HttpServletResponse.SC_FORBIDDEN))    // 403
                )

                /* ---- SIN FORM LOGIN / BASIC ---- */
                .formLogin(f -> f.disable())
                .httpBasic(b -> b.disable())

                /* ---- INSERTAR FILTRO JWT ----
                   Debe ir antes del UsernamePasswordAuthenticationFilter para que
                   el SecurityContext tenga la Authentication basada en el token. */
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configuración CORS (dev).
     * - Orígenes permitidos: localhost/127.0.0.1:5173 (Vite).
     * - Métodos: GET/POST/PUT/DELETE/PATCH/OPTIONS.
     * - Cabeceras: Authorization (Bearer), Content-Type.
     * - Sin credenciales (no usamos cookies).
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
