package cat.itacademy.virtualpet.web.error;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;




import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // --- 400: Validaciones de @Valid en @RequestBody ---
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex,
                                                                HttpServletRequest req) {
        Map<String, Object> body = base(HttpStatus.BAD_REQUEST, "Validation failed", req.getRequestURI());

        List<Map<String, Object>> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toFieldErrorMap)
                .toList();

        body.put("errors", errors);
        return ResponseEntity.badRequest().body(body);
    }

    // --- 401: Email inválido (no existe) ---
    @ExceptionHandler(InvalidEmailException.class)
    public ResponseEntity<Map<String, Object>> invalidEmail(InvalidEmailException ex,
                                                            HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), req.getRequestURI());
    }

    // --- 401: Password incorrecta ---
    @ExceptionHandler(IncorrectPasswordException.class)
    public ResponseEntity<Map<String, Object>> incorrectPassword(IncorrectPasswordException ex,
                                                                 HttpServletRequest req) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), req.getRequestURI());
    }

    // --- 409: Email ya registrado (excepción propia de registro) ---
    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    public ResponseEntity<Map<String, Object>> emailAlreadyRegistered(EmailAlreadyRegisteredException ex,
                                                                      HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), req.getRequestURI());
    }

    // --- 409: Violación de integridad (índices únicos, etc.) ---
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> dataIntegrity(DataIntegrityViolationException ex,
                                                             HttpServletRequest req) {
        // Intento de mensaje más amigable si detectamos que es el email único
        String mostSpecific = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : "";
        String msg = (mostSpecific != null && mostSpecific.toLowerCase().contains("email"))
                ? "Email already registered"
                : "Data integrity violation";
        return build(HttpStatus.CONFLICT, msg, req.getRequestURI());
    }

    @ExceptionHandler(UsernameAlreadyTakenException.class)
    public ResponseEntity<Map<String, Object>> usernameTaken(UsernameAlreadyTakenException ex,
                                                             HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(PetDeceasedException.class)
    public ResponseEntity<Map<String, Object>> petDeceased(PetDeceasedException ex,
                                                           HttpServletRequest req) {
        // 410 Gone porque el recurso (mascota viva) ya no está disponible
        return build(HttpStatus.GONE, ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(PetNotHungryException.class)
    public ResponseEntity<Map<String, Object>> petNotHungry(PetNotHungryException ex,
                                                            HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(PetAlreadyCleanException.class)
    public ResponseEntity<Map<String, Object>> petAlreadyClean(PetAlreadyCleanException ex,
                                                               HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(PetTooHappyException.class)
    public ResponseEntity<Map<String, Object>> petTooHappy(PetTooHappyException ex,
                                                           HttpServletRequest req) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), req.getRequestURI());
    }




    // --- 500: Fallback genérico ---
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> generic(Exception ex, HttpServletRequest req) {
        // En producción podrías loggear ex con un logger
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
