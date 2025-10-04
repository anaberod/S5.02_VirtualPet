package cat.itacademy.virtualpet.application.dto.auth;

import lombok.*;

/**
 * Respuesta estándar de autenticación: se devuelve el JWT.
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AuthResponse {
    // Token JWT firmado por el backend (se envía en Authorization: Bearer <token>)
    private String token;
}
