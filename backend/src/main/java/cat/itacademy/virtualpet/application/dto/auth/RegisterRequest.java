package cat.itacademy.virtualpet.application.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;


@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
@ToString(exclude = "password")
public class RegisterRequest {


    @NotBlank(message = "username es obligatorio")
    @Size(min = 3, max = 50, message = "username debe tener entre 3 y 50 caracteres")
    private String username;


    @NotBlank(message = "email es obligatorio")
    @Email(message = "email no tiene formato válido")
    @Size(max = 120, message = "email no puede superar 120 caracteres")
    private String email;


    @NotBlank(message = "password es obligatoria")
    @Size(min = 6, max = 100, message = "password debe tener entre 6 y 100 caracteres")
    private String password;
}

