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

        log.info("Register requested | email={} username={}", body.getEmail(), body.getUsername());

        try {

            log.debug("Register payload validated for email={}", body.getEmail());


            AuthResponse resp = authService.register(body);


            log.info("Register success | email={} token_issued=true", body.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(resp);

        } catch (Exception ex) {

            log.warn("Register failed | email={} reason={}", body.getEmail(), ex.getMessage());
            throw ex;
        }
    }

    // =================== LOGIN ===================
    @PostMapping(
            value = "/login",
            consumes = "application/json",
            produces = "application/json"
    )
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest body) {

        log.info("Login requested | email={}", body.getEmail());

        try {

            AuthResponse resp = authService.login(body);


            log.info("Login success | email={}", body.getEmail());

            log.debug("JWT issued successfully for email={}", body.getEmail());

            return ResponseEntity.ok(resp);

        } catch (Exception ex) {

            log.warn("Login failed | email={} reason={}", body.getEmail(), ex.getMessage());
            throw ex;
        }
    }
}
