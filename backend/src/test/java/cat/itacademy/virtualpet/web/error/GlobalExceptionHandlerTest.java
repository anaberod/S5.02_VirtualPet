package cat.itacademy.virtualpet.web.error;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private HttpServletRequest mockReq(String uri) {
        HttpServletRequest req = Mockito.mock(HttpServletRequest.class);
        when(req.getRequestURI()).thenReturn(uri);
        return req;
    }

    @Test
    @DisplayName("@Valid -> 400 con lista de errores")
    void handleValidation_returns400_withErrors() throws Exception {
        var target = new Object();
        var br = new BeanPropertyBindingResult(target, "userDto");
        br.addError(new FieldError("userDto", "email", "must be a well-formed email address"));
        br.addError(new FieldError("userDto", "password", "size must be between 8 and 64"));

        var param = Mockito.mock(MethodParameter.class);
        var ex = new MethodArgumentNotValidException(param, br);

        var response = handler.handleValidation(ex, mockReq("/auth/register"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("status")).isEqualTo(400);
        assertThat(body.get("error")).isEqualTo("Bad Request");
        assertThat(body.get("message")).isEqualTo("Validation failed");
        assertThat(body.get("path")).isEqualTo("/auth/register");
        assertThat((String) body.get("timestamp")).contains("T");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> errors = (List<Map<String, Object>>) body.get("errors");
        assertThat(errors).hasSize(2);
        assertThat(errors.get(0).get("field")).isIn("email", "password");
        assertThat(errors.get(0).get("message")).isNotNull();
    }

    @Test
    @DisplayName("InvalidEmailException -> 401 Unauthorized (mensaje por defecto)")
    void invalidEmail_returns401() {
        var ex = new InvalidEmailException();
        var response = handler.invalidEmail(ex, mockReq("/auth/login"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        Map<String, Object> body = response.getBody();
        assertThat(body.get("message")).isEqualTo("Invalid email");
        assertThat(body.get("status")).isEqualTo(401);
        assertThat(body.get("path")).isEqualTo("/auth/login");
    }

    @Test
    @DisplayName("IncorrectPasswordException -> 401 Unauthorized (mensaje por defecto)")
    void incorrectPassword_returns401() {
        var ex = new IncorrectPasswordException();
        var response = handler.incorrectPassword(ex, mockReq("/auth/login"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().get("message")).isEqualTo("Incorrect password");
        assertThat(response.getBody().get("status")).isEqualTo(401);
    }

    @Test
    @DisplayName("EmailAlreadyRegisteredException -> 409 Conflict (mensaje por defecto)")
    void emailAlreadyRegistered_returns409() {
        var ex = new EmailAlreadyRegisteredException();
        var response = handler.emailAlreadyRegistered(ex, mockReq("/auth/register"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().get("message")).isEqualTo("Email already registered");
        assertThat(response.getBody().get("status")).isEqualTo(409);
    }

    @Test
    @DisplayName("UsernameAlreadyTakenException -> 409 Conflict (mensaje por defecto)")
    void usernameTaken_returns409() {
        var ex = new UsernameAlreadyTakenException();
        var response = handler.usernameTaken(ex, mockReq("/auth/register"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().get("message")).isEqualTo("Username already taken");
        assertThat(response.getBody().get("status")).isEqualTo(409);
    }

    @Test
    @DisplayName("DataIntegrityViolation con 'email' en la causa -> 409 y mensaje amigable")
    void dataIntegrity_email_returns409_withFriendlyMessage() {
        var cause = new RuntimeException("duplicate key value violates unique constraint on email");
        var ex = new DataIntegrityViolationException("fk/unique error", cause);

        var response = handler.dataIntegrity(ex, mockReq("/auth/register"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().get("message")).isEqualTo("Email already registered");
        assertThat(response.getBody().get("path")).isEqualTo("/auth/register");
    }

    @Test
    @DisplayName("DataIntegrityViolation sin 'email' -> 409 con mensaje genérico")
    void dataIntegrity_withoutEmail_returns409_genericMessage() {
        var ex = new DataIntegrityViolationException("some constraint failed", new RuntimeException("unique idx on username"));
        var response = handler.dataIntegrity(ex, mockReq("/auth/register"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().get("message")).isEqualTo("Data integrity violation");
        assertThat(response.getBody().get("status")).isEqualTo(409);
    }

    @Test
    @DisplayName("PetDeceasedException -> 410 Gone (mensaje tal cual)")
    void petDeceased_returns410() {
        var ex = new PetDeceasedException("Your pet is deceased");
        var response = handler.petDeceased(ex, mockReq("/pets/5/actions/feed"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.GONE);
        assertThat(response.getBody().get("message")).isEqualTo("Your pet is deceased");
        assertThat(response.getBody().get("status")).isEqualTo(410);
    }

    @Test
    @DisplayName("PetNotHungryException -> 409 Conflict (mensaje por defecto)")
    void petNotHungry_returns409() {
        var ex = new PetNotHungryException();
        var response = handler.petNotHungry(ex, mockReq("/pets/5/actions/feed"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().get("message"))
                .isEqualTo("Your pet is not hungry right now 🐶🍗");
    }

    @Test
    @DisplayName("PetAlreadyCleanException -> 409 Conflict (mensaje por defecto)")
    void petAlreadyClean_returns409() {
        var ex = new PetAlreadyCleanException();
        var response = handler.petAlreadyClean(ex, mockReq("/pets/5/actions/wash"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().get("message"))
                .isEqualTo("Your pet is already clean 🧼✨");
    }

    @Test
    @DisplayName("PetTooHappyException -> 409 Conflict (mensaje por defecto)")
    void petTooHappy_returns409() {
        var ex = new PetTooHappyException();
        var response = handler.petTooHappy(ex, mockReq("/pets/5/actions/play"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().get("message"))
                .isEqualTo("Your pet is already too happy and tired to play 🎾💤");
    }

    @Test
    @DisplayName("ResponseStatusException (404) -> respeta el status y mensaje")
    void responseStatusException_respectsStatusAndMessage() {
        var ex = new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet not found");
        var response = handler.handleResponseStatus(ex, mockReq("/admin/pets/123"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().get("message")).isEqualTo("Pet not found");
        assertThat(response.getBody().get("status")).isEqualTo(404);
        assertThat(response.getBody().get("path")).isEqualTo("/admin/pets/123");
    }

    @Test
    @DisplayName("Excepción genérica -> 500 Internal Server Error")
    void generic_returns500() {
        var response = handler.generic(new Exception("boom"), mockReq("/whatever"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().get("message")).isEqualTo("Internal Server Error");
        assertThat(response.getBody().get("path")).isEqualTo("/whatever");
        assertThat(response.getBody().get("status")).isEqualTo(500);
    }
}
