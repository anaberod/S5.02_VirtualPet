package cat.itacademy.virtualpet.web.controller;

import cat.itacademy.virtualpet.application.dto.auth.AuthResponse;
import cat.itacademy.virtualpet.application.dto.auth.LoginRequest;
import cat.itacademy.virtualpet.application.dto.auth.RegisterRequest;
import cat.itacademy.virtualpet.application.service.auth.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // =================== REGISTER ===================
    @PostMapping(
            value = "/register",
            consumes = "application/json",
            produces = "application/json"
    )
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest body) {
        // 🟢 INFO: inicio de la operación
        log.info("Register requested | email={} username={}", body.getEmail(), body.getUsername());

        try {
            // ⚙️ DEBUG: validación de payload
            log.debug("Register payload validated for email={}", body.getEmail());

            // 💾 Lógica de negocio
            AuthResponse resp = authService.register(body);

            // 🟢 INFO: registro exitoso
            log.info("Register success | email={} token_issued=true", body.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(resp);

        } catch (Exception ex) {
            // ⚠️ WARN: error controlado o inesperado
            log.warn("Register failed | email={} reason={}", body.getEmail(), ex.getMessage());
            throw ex; // deja que el GlobalExceptionHandler devuelva el error correspondiente
        }
    }

    // =================== LOGIN ===================
    @PostMapping(
            value = "/login",
            consumes = "application/json",
            produces = "application/json"
    )
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest body) {
        // 🟢 INFO: intento de login
        log.info("Login requested | email={}", body.getEmail());

        try {
            // 💾 Lógica de negocio
            AuthResponse resp = authService.login(body);

            // 🟢 INFO: login correcto
            log.info("Login success | email={}", body.getEmail());
            // ⚙️ DEBUG: confirmación del JWT emitido
            log.debug("JWT issued successfully for email={}", body.getEmail());

            return ResponseEntity.ok(resp);

        } catch (Exception ex) {
            // ⚠️ WARN: fallo de login (credenciales incorrectas o usuario inexistente)
            log.warn("Login failed | email={} reason={}", body.getEmail(), ex.getMessage());
            throw ex;
        }
    }
}
