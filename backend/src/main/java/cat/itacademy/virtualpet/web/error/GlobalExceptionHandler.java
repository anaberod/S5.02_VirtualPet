package cat.itacademy.virtualpet.web.error;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // --- 400: Validaciones de @Valid ---
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex,
                                                                HttpServletRequest req) {
        var uri = req.getRequestURI();
        var errors = ex.getBindingResult().getFieldErrors();
        log.warn("400 Validation failed on {} ({} field errors)", uri, errors.size());
        if (log.isDebugEnabled()) {
            errors.forEach(fe -> log.debug(" - field='{}' rejected='{}' msg='{}'",
                    fe.getField(), fe.getRejectedValue(), fe.getDefaultMessage()));
        }

        Map<String, Object> body = base(HttpStatus.BAD_REQUEST, "Validation failed", uri);
        List<Map<String, Object>> errorList = errors.stream().map(this::toFieldErrorMap).toList();
        body.put("errors", errorList);
        return ResponseEntity.badRequest().body(body);
    }

    // --- 401 ---
    @ExceptionHandler(InvalidEmailException.class)
    public ResponseEntity<Map<String, Object>> invalidEmail(InvalidEmailException ex, HttpServletRequest req) {
        log.warn("401 Invalid email on {} -> {}", req.getRequestURI(), ex.getMessage());
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(IncorrectPasswordException.class)
    public ResponseEntity<Map<String, Object>> incorrectPassword(IncorrectPasswordException ex, HttpServletRequest req) {
        log.warn("401 Incorrect password on {} -> {}", req.getRequestURI(), ex.getMessage());
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), req.getRequestURI());
    }

    // --- 409 ---
    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    public ResponseEntity<Map<String, Object>> emailAlreadyRegistered(EmailAlreadyRegisteredException ex,
                                                                      HttpServletRequest req) {
        log.warn("409 Email already registered on {} -> {}", req.getRequestURI(), ex.getMessage());
        return build(HttpStatus.CONFLICT, ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> dataIntegrity(DataIntegrityViolationException ex,
                                                             HttpServletRequest req) {
        String mostSpecific = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : "";
        String msg = (mostSpecific != null && mostSpecific.toLowerCase().contains("email"))
                ? "Email already registered"
                : "Data integrity violation";
        log.warn("409 DataIntegrity on {} -> {} (cause='{}')", req.getRequestURI(), msg, mostSpecific);
        if (log.isDebugEnabled()) log.debug("DataIntegrity stack:", ex);
        return build(HttpStatus.CONFLICT, msg, req.getRequestURI());
    }

    @ExceptionHandler(UsernameAlreadyTakenException.class)
    public ResponseEntity<Map<String, Object>> usernameTaken(UsernameAlreadyTakenException ex, HttpServletRequest req) {
        log.warn("409 Username taken on {} -> {}", req.getRequestURI(), ex.getMessage());
        return build(HttpStatus.CONFLICT, ex.getMessage(), req.getRequestURI());
    }

    // --- 410 ---
    @ExceptionHandler(PetDeceasedException.class)
    public ResponseEntity<Map<String, Object>> petDeceased(PetDeceasedException ex, HttpServletRequest req) {
        log.warn("410 Pet deceased on {} -> {}", req.getRequestURI(), ex.getMessage());
        return build(HttpStatus.GONE, ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(PetNotHungryException.class)
    public ResponseEntity<Map<String, Object>> petNotHungry(PetNotHungryException ex, HttpServletRequest req) {
        log.warn("409 Pet not hungry on {} -> {}", req.getRequestURI(), ex.getMessage());
        return build(HttpStatus.CONFLICT, ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(PetAlreadyCleanException.class)
    public ResponseEntity<Map<String, Object>> petAlreadyClean(PetAlreadyCleanException ex, HttpServletRequest req) {
        log.warn("409 Pet already clean on {} -> {}", req.getRequestURI(), ex.getMessage());
        return build(HttpStatus.CONFLICT, ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(PetTooHappyException.class)
    public ResponseEntity<Map<String, Object>> petTooHappy(PetTooHappyException ex, HttpServletRequest req) {
        log.warn("409 Pet too happy on {} -> {}", req.getRequestURI(), ex.getMessage());
        return build(HttpStatus.CONFLICT, ex.getMessage(), req.getRequestURI());
    }


    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException ex,
                                                                    HttpServletRequest req) {

        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        String message = ex.getReason() != null ? ex.getReason() : status.getReasonPhrase();
        log.warn("{} {} on {} -> {}", status.value(), status.getReasonPhrase(), req.getRequestURI(), message);
        return build(status, message, req.getRequestURI());
    }

    // --- 500: Fallback genérico ---
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> generic(Exception ex, HttpServletRequest req) {
        log.error("500 Internal error on {} -> {}", req.getRequestURI(), ex.getMessage(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", req.getRequestURI());
    }

    // ----------------- helpers -----------------

    private Map<String, Object> toFieldErrorMap(FieldError fe) {
        Map<String, Object> m = new HashMap<>();
        m.put("field", fe.getField());
        m.put("message", fe.getDefaultMessage());
        m.put("rejectedValue", fe.getRejectedValue());
        return m;
    }

    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message, String path) {
        return ResponseEntity.status(status).body(base(status, message, path));
    }

    private Map<String, Object> base(HttpStatus status, String message, String path) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", path);
        return body;
    }
}
