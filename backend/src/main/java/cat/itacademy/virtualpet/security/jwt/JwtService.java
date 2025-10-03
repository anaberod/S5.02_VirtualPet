package cat.itacademy.virtualpet.security.jwt; // Paquete de seguridad/JWT

/* ============================= IMPORTS ============================= */
import cat.itacademy.virtualpet.domain.user.User;              // Nuestra entidad User (para sacar email/roles al crear el token)
import io.jsonwebtoken.*;                                      // JJWT (builder, parser, Claims, SignatureAlgorithm, JwtException)
import io.jsonwebtoken.io.Decoders;                            // Para decodificar Base64 de la clave
import io.jsonwebtoken.security.Keys;                          // Para construir la Key HMAC a partir de bytes
import org.springframework.beans.factory.annotation.Value;     // Inyectar propiedades de application.yml
import org.springframework.stereotype.Service;                 // Marcar el servicio como bean de Spring

import java.security.Key;                                      // Tipo de la clave HMAC
import java.time.Instant;                                      // Fechas en UTC
import java.time.temporal.ChronoUnit;                          // Para sumar minutos de expiración
import java.util.Date;                                         // JJWT usa java.util.Date para iat/exp
import java.util.HashMap;                                      // Para claims personalizados
import java.util.Map;                                          // Interface Map (claims)

/**
 * Servicio de utilidades JWT:
 * - Generar token (HS256) con subject = email y claims útiles (userId, username, roles, iat, exp).
 * - Extraer email del token.
 * - Validar firma y expiración (y que el token corresponde al usuario).
 *
 * Requisitos:
 * - En application.yml: jwt.secret (Base64 256 bits) y jwt.expirationMinutes.
 * - El login en tu app es por EMAIL: por eso usamos subject = email.
 */
@Service
public class JwtService {

    /* ======= Propiedades inyectadas desde application.yml ======= */

    @Value("${jwt.secret}")                 // Clave Base64 (256 bits mínimo) -> p.ej. desde variable de entorno JWT_SECRET
    private String secretBase64;

    @Value("${jwt.expirationMinutes}")      // Minutos hasta la expiración del token (ej. 120)
    private long expirationMinutes;

    /* ============================= API PÚBLICA ============================= */

    /**
     * Genera un JWT firmado (HS256) para el usuario dado.
     * Subject del token = email (login es por email).
     * Incluye claims: userId, username, roles, iat, exp.
     */
    public String generateToken(User user) {
        // Instante actual (UTC)
        Instant now = Instant.now();

        // Instante de expiración = ahora + minutos configurados
        Instant exp = now.plus(expirationMinutes, ChronoUnit.MINUTES);

        // Claims personalizados que añadiremos al token
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());           // Id interno estable
        claims.put("username", user.getUsername());   // Para mostrar en el front si quieres
        claims.put("roles", user.getRoles());         // Lista/Set de roles (ROLE_USER, ROLE_ADMIN)

        // Construcción del token:
        // - subject = email del user (coherente con login por email)
        // - iat/exp = fechas de emisión/expiración
        // - claims extra (arriba)
        // - firma HS256 con la clave HMAC derivada de secretBase64
        return Jwts.builder()
                .setSubject(user.getEmail())                  // SUBJECT = email
                .setIssuedAt(Date.from(now))                  // IAT (issued at)
                .setExpiration(Date.from(exp))                // EXP (expiration)
                .addClaims(claims)                            // Claims personalizados
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // Firma HS256
                .compact();                                   // Serializa a String
    }

    /**
     * Extrae el email (subject) del token.
     * Lanza JwtException si la firma es inválida o el token está corrupto.
     */
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Valida el token: firma correcta, no expirado y corresponde al usuario (mismo email).
     * Devuelve true si es válido; false en caso contrario.
     */
    public boolean isTokenValid(String token, User user) {
        try {
            Claims claims = extractAllClaims(token);
            String subjectEmail = claims.getSubject();
            Date expiration = claims.getExpiration();

            // Debe ser para el mismo usuario (por email) y no estar expirado
            boolean sameUser = subjectEmail != null
                    && user.getEmail() != null
                    && subjectEmail.equalsIgnoreCase(user.getEmail());
            boolean notExpired = expiration != null && expiration.after(new Date());

            return sameUser && notExpired;
        } catch (JwtException | IllegalArgumentException e) {
            // JwtException cubre: firma inválida, token malformado, expirado (al parsear), etc.
            // IllegalArgumentException si el token es null/vacío.
            return false;
        }
    }

    /* ============================= MÉTODOS PRIVADOS ============================= */

    /**
     * Parsea y valida el token (firma y formato) y devuelve los claims.
     * Usa la misma clave de firma que en generateToken().
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())   // Clave HMAC para verificar firma
                .build()
                .parseClaimsJws(token)            // Valida firma y decodifica claims
                .getBody();
    }

    /**
     * Construye la Key HMAC a partir del secret Base64 del application.yml.
     * - Si la clave no tiene al menos 256 bits reales, JJWT lanzará WeakKeyException.
     */
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretBase64); // Decodifica Base64 a bytes
        return Keys.hmacShaKeyFor(keyBytes);                    // Crea la clave HMAC para HS256
    }
}
