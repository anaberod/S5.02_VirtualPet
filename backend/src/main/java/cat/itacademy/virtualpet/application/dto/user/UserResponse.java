package cat.itacademy.virtualpet.application.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.Instant;
import java.util.Set;


@Data
@Schema(description = "Información pública del usuario (sin contraseña).")
public class UserResponse {

    @Schema(description = "Identificador del usuario", example = "1")
    private Long id;

    @Schema(description = "Nombre de usuario visible", example = "anaUser")
    private String username;

    @Schema(description = "Correo electrónico del usuario", example = "ana@example.com")
    private String email;

    @Schema(description = "Roles asignados al usuario", example = "[\"ROLE_USER\"]")
    private Set<String> roles;

    @Schema(description = "Fecha de creación de la cuenta", example = "2025-10-07T10:32:00Z")
    private Instant createdAt;
}
