package cat.itacademy.virtualpet.web.controller;

import cat.itacademy.virtualpet.application.dto.auth.AuthResponse;
import cat.itacademy.virtualpet.application.dto.auth.LoginRequest;
import cat.itacademy.virtualpet.application.dto.auth.RegisterRequest;
import cat.itacademy.virtualpet.application.service.auth.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador HTTP de autenticación.
 * Expone:
 *  - POST /auth/register : registra usuario y devuelve JWT.
 *  - POST /auth/login    : login SOLO por email + password, devuelve JWT.
 *
 * Notas:
 *  - La validación (@Valid) usa las anotaciones de los DTOs.
 *  - Los errores (409, 401, 400/422) los lanza el AuthService como ResponseStatusException.
 *  - /auth/** ya está permitido en tu SecurityConfig.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Registro de usuario.
     * Body: { username, email, password }
     * Respuesta: { token } (JWT)
     * Status: 201 CREATED
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest body) {
        AuthResponse resp = authService.register(body);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    /**
     * Login SOLO por email.
     * Body: { email, password }
     * Respuesta: { token } (JWT)
     * Status: 200 OK
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest body) {
        AuthResponse resp = authService.login(body);
        return ResponseEntity.ok(resp);
    }
}
