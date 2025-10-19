package cat.itacademy.virtualpet.infrastructure.security;

import cat.itacademy.virtualpet.domain.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.security.Key;
import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {

        "jwt.secret=MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTI=",
        "jwt.expirationMinutes=10"
})
class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    @Value("${jwt.secret}")
    private String secretBase64;

    private Key key() {

        byte[] bytes = Decoders.BASE64.decode(secretBase64);
        return Keys.hmacShaKeyFor(bytes);
    }

    @Test
    @DisplayName("generateToken: subject=email y firma válida")
    void generateToken_containsSubjectAndValidSignature() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setUsername("user");

        String token = jwtService.generateToken(user);
        assertThat(token).isNotBlank();

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();

        assertThat(claims.getSubject()).isEqualTo(user.getEmail());
    }

    @Test
    @DisplayName("generateToken: exp > now (no está expirado)")
    void token_hasExpirationInFuture() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setUsername("user");

        String token = jwtService.generateToken(user);

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();

        Date exp = claims.getExpiration();
        assertThat(exp).isNotNull();
        assertThat(exp.toInstant()).isAfter(Instant.now());
    }

    @Test
    @DisplayName("token manipulado: verificación de firma falla")
    void tamperedToken_shouldFailSignatureVerification() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setUsername("user");

        String token = jwtService.generateToken(user);
        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertThatThrownBy(() -> Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(tampered))
                .isInstanceOf(RuntimeException.class);
    }
}
