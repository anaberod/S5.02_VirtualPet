package cat.itacademy.virtualpet.infrastructure.security;

import cat.itacademy.virtualpet.domain.user.User;
import cat.itacademy.virtualpet.domain.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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


@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    private static final AntPathMatcher PM = new AntPathMatcher();


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

        String uri = request.getRequestURI();


        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            log.debug("JWT FILTER skipped for '{}' → already authenticated", uri);
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.trace("JWT FILTER no Authorization header found for '{}'", uri);
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);
        log.debug("JWT FILTER detected Bearer token ({} chars) for '{}'", token.length(), uri);

        try {

            String email = jwtService.extractEmail(token);
            if (email == null) {
                log.warn("JWT FILTER invalid token: subject is null for '{}'", uri);
                filterChain.doFilter(request, response);
                return;
            }
            email = email.trim().toLowerCase();
            log.trace("JWT FILTER extracted email='{}'", email);


            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null) {
                log.warn("JWT FILTER user not found for email='{}'", email);
                filterChain.doFilter(request, response);
                return;
            }


            if (!jwtService.isTokenValid(token, user)) {
                log.warn("JWT FILTER invalid token for user='{}'", email);
                filterChain.doFilter(request, response);
                return;
            }


            Set<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toSet());


            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(email, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(auth);

            log.debug("JWT FILTER authenticated user='{}' with roles={}", email, authorities);

        } catch (Exception ex) {
            log.error("JWT FILTER error while processing token for '{}': {}", uri, ex.getMessage());

        }

        filterChain.doFilter(request, response);
    }
}
