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
 * Rutas públicas (permitidas en SecurityConfig):
 *  - POST /auth/register : registra usuario y devuelve JWT.
 *  - POST /auth/login    : login SOLO por email + password, devuelve JWT.
 *
 * Los errores (401/409/400) se lanzan como excepciones propias y
 * se formatean en GlobalExceptionHandler.
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping(
            value = "/register",
            consumes = "application/json",
            produces = "application/json"
    )
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest body) {
        AuthResponse resp = authService.register(body);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @PostMapping(
            value = "/login",
            consumes = "application/json",
            produces = "application/json"
    )
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest body) {
        AuthResponse resp = authService.login(body);
        return ResponseEntity.ok(resp);
    }
}
