package cat.itacademy.virtualpet.application.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Payload de entrada para LOGIN.
 * Importante: aquí solo se acepta email + password (no username).
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@ToString(exclude = "password") // nunca loguear password
public class LoginRequest {

    @NotBlank(message = "email es obligatorio")
    @Email(message = "email no tiene formato válido")
    @Size(max = 120, message = "email no puede superar 120 caracteres")
    private String email;

    @NotBlank(message = "password es obligatoria")
    @Size(min = 6, max = 100, message = "password debe tener entre 6 y 100 caracteres")
    private String password;
}
