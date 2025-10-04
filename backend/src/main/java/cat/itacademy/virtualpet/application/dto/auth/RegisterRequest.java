package cat.itacademy.virtualpet.application.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * Payload de entrada para REGISTRO de usuario.
 * Se valida en capa web antes de llegar a la lógica de negocio.
 */
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@ToString(exclude = "password") // evita loguear la password en texto claro
public class RegisterRequest {

    // Nombre de usuario para mostrar/identificar (no se usa para login)
    @NotBlank(message = "username es obligatorio")
    @Size(min = 3, max = 50, message = "username debe tener entre 3 y 50 caracteres")
    private String username;

    // Login será únicamente por email (normalizarás a minúsculas en el servicio)
    @NotBlank(message = "email es obligatorio")
    @Email(message = "email no tiene formato válido")
    @Size(max = 120, message = "email no puede superar 120 caracteres")
    private String email;

    // Contraseña en texto claro SOLO aquí (se hashea con BCrypt en el servicio)
    @NotBlank(message = "password es obligatoria")
    @Size(min = 6, max = 100, message = "password debe tener entre 6 y 100 caracteres")
    private String password;
}

