package cat.itacademy.virtualpet.infrastructure.security;

import cat.itacademy.virtualpet.domain.user.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretBase64;

    @Value("${jwt.expirationMinutes}")
    private long expirationMinutes;



    public String generateToken(User user) {
        Instant now = Instant.now();
        Instant exp = now.plus(expirationMinutes, ChronoUnit.MINUTES);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("roles", user.getRoles());

        String email = user.getEmail();

        log.debug("JWT → generating token for email='{}' expIn={}min", email, expirationMinutes);

        try {
            String token = Jwts.builder()
                    .setSubject(email)
                    .setIssuedAt(Date.from(now))
                    .setExpiration(Date.from(exp))
                    .addClaims(claims)
                    .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                    .compact();

            log.info("JWT → token generated for userId={} email={}", user.getId(), email);
            return token;

        } catch (Exception e) {
            log.error("JWT → error generating token for {}: {}", email, e.getMessage());
            throw e;
        }
    }

    public String extractEmail(String token) {
        try {
            String email = extractAllClaims(token).getSubject();
            log.trace("JWT → extracted subject='{}'", email);
            return email;
        } catch (ExpiredJwtException e) {
            log.warn("JWT → expired token for subject={}", e.getClaims().getSubject());
            throw e;
        } catch (JwtException e) {
            log.error("JWT → invalid token: {}", e.getMessage());
            throw e;
        }
    }

    public boolean isTokenValid(String token, User user) {
        try {
            Claims claims = extractAllClaims(token);
            String subjectEmail = claims.getSubject();
            Date expiration = claims.getExpiration();

            boolean sameUser = subjectEmail != null
                    && user.getEmail() != null
                    && subjectEmail.equalsIgnoreCase(user.getEmail());
            boolean notExpired = expiration != null && expiration.after(new Date());

            boolean valid = sameUser && notExpired;

            log.debug("JWT → validation for email='{}': sameUser={} notExpired={} => valid={}",
                    user.getEmail(), sameUser, notExpired, valid);

            return valid;

        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT → validation failed for {}: {}", user.getEmail(), e.getMessage());
            return false;
        }
    }



    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretBase64);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
