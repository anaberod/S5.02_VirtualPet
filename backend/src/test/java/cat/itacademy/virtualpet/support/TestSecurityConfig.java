package cat.itacademy.virtualpet.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;

/**
 * Configuración de seguridad de test:
 * - Simula un AuthenticationManager simple que siempre valida el token mockeado.
 * - Garantiza que Spring Security no bloquee los endpoints en tests de integración.
 */
@TestConfiguration
public class TestSecurityConfig {

    @Bean
    public AuthenticationManager authenticationManager() {
        return authentication -> {
            // Simula autenticación válida (ROLE_USER)
            return new UsernamePasswordAuthenticationToken(
                    authentication.getPrincipal(),
                    null,
                    Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"))
            );
        };
    }
}
