package cat.itacademy.virtualpet.application.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Set;

@Data
@Schema(description = "Información pública del usuario (sin password).")
public class UserResponse {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "anaUser")
    private String username;

    @Schema(example = "ana@example.com")
    private String email;

    @Schema(example = "[\"ROLE_USER\"]")
    private Set<String> roles;
}
