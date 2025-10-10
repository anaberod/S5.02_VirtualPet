package cat.itacademy.virtualpet.support;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.stream.Collectors;
import java.util.Arrays;

/**
 * Genera JWT HS256 para tests, compatible con JwtService:
 * - Usa secret en Base64 (igual que el backend).
 * - Claims: sub (email), roles (array), iat, exp.
 */
public final class JwtTestUtil {
    private JwtTestUtil() {}

    public static String generateHs256(
            String subjectEmail,
            String[] roles,
            String issuerIgnored,      // no se usa en tu JwtService, se mantiene por firma
            String secretBase64,       // debe ser el mismo Base64 que en application(-test).yml
            long ttlSeconds
    ) {
        // Decodifica la clave Base64 como hace JwtService.getSigningKey()
        byte[] secretBytes = Base64.getDecoder().decode(secretBase64);

        String headerJson = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";

        long now = Instant.now().getEpochSecond();
        long exp = now + ttlSeconds;

        String rolesJsonArray = "[" +
                Arrays.stream(roles).map(r -> "\"" + r + "\"").collect(Collectors.joining(",")) +
                "]";

        String payloadJson = "{"
                + "\"sub\":\"" + subjectEmail + "\","
                + "\"iat\":" + now + ","
                + "\"exp\":" + exp + ","
                + "\"roles\":" + rolesJsonArray
                + "}";

        String header = base64Url(headerJson.getBytes(StandardCharsets.UTF_8));
        String payload = base64Url(payloadJson.getBytes(StandardCharsets.UTF_8));
        String signature = hmacSha256(header + "." + payload, secretBytes);

        return header + "." + payload + "." + signature;
    }

    private static String hmacSha256(String data, byte[] secretBytes) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretBytes, "HmacSHA256"));
            return base64Url(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Cannot sign JWT", e);
        }
    }

    private static String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
