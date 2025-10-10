package cat.itacademy.virtualpet.infrastructure.security;

import cat.itacademy.virtualpet.domain.user.User;
import cat.itacademy.virtualpet.domain.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Filtro JWT:
 * - Ignora preflight y rutas públicas mediante shouldNotFilter.
 * - Valida token, carga usuario y establece Authentication si todo es correcto.
 * - Si el usuario no existe o el token no es válido, no autentica (Security devolverá 401/403).
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    private static final AntPathMatcher PM = new AntPathMatcher();

    // Rutas públicas (ajústalas si necesitas más)
    private static final String[] PUBLIC_PATTERNS = new String[] {
            "/auth/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/actuator/health"
    };

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) return true; // preflight CORS
        for (String pattern : PUBLIC_PATTERNS) {
            if (PM.match(pattern, uri)) return true;
        }
        return false;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Si ya hay autenticación, continúa
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);

        try {
            // 1) Extraer email (valida firma/exp. internamente; si falla, lanza excepción)
            String email = jwtService.extractEmail(token);
            if (email == null) {
                filterChain.doFilter(request, response);
                return;
            }
            email = email.trim().toLowerCase();

            // 2) Cargar usuario
            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null) {
                // Usuario borrado o inexistente → no autenticar
                filterChain.doFilter(request, response);
                return;
            }

            // 3) Validar token contra el usuario
            if (!jwtService.isTokenValid(token, user)) {
                filterChain.doFilter(request, response);
                return;
            }

            // 4) Construir Authorities con prefijo ROLE_
            Set<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toSet());

            // 5) Establecer Authentication (principal = email)
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(email, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (Exception ignored) {
            // Cualquier fallo → no autenticar; Security decidirá (401/403)
        }

        filterChain.doFilter(request, response);
    }
}
