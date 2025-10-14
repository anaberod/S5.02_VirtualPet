package cat.itacademy.virtualpet.web.error;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

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
        // given: un BindingResult con un par de errores
        var target = new Object();
        var br = new BeanPropertyBindingResult(target, "userDto");
        br.addError(new FieldError("userDto", "email", "must be a well-formed email address"));
        br.addError(new FieldError("userDto", "password", "size must be between 8 and 64"));

        var param = Mockito.mock(MethodParameter.class); // no lo usamos, pero el ctor lo pide
        var ex = new MethodArgumentNotValidException(param, br);

        // when
        var response = handler.handleValidation(ex, mockReq("/auth/register"));

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("status")).isEqualTo(400);
        assertThat(body.get("error")).isEqualTo("Bad Request");
        assertThat(body.get("message")).isEqualTo("Validation failed");
        assertThat(body.get("path")).isEqualTo("/auth/register");

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> errors = (List<Map<String, Object>>) body.get("errors");
        assertThat(errors).hasSize(2);
        assertThat(errors.get(0).get("field")).isIn("email", "password");
        assertThat(errors.get(0).get("message")).isNotNull();
    }

    @Test
    @DisplayName("InvalidEmailException -> 401 Unauthorized (mensaje por defecto)")
    void invalidEmail_returns401() {
        var ex = new InvalidEmailException(); // <-- sin mensaje custom
        var response = handler.invalidEmail(ex, mockReq("/auth/login"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        Map<String, Object> body = response.getBody();
        assertThat(body.get("message")).isEqualTo("Invalid email");
        assertThat(body.get("path")).isEqualTo("/auth/login");
    }

    @Test
    @DisplayName("IncorrectPasswordException -> 401 Unauthorized (mensaje por defecto)")
    void incorrectPassword_returns401() {
        var ex = new IncorrectPasswordException(); // <-- sin mensaje custom
        var response = handler.incorrectPassword(ex, mockReq("/auth/login"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().get("message")).isEqualTo("Incorrect password");
    }

    @Test
    @DisplayName("EmailAlreadyRegisteredException -> 409 Conflict (mensaje por defecto)")
    void emailAlreadyRegistered_returns409() {
        var ex = new EmailAlreadyRegisteredException(); // <-- sin argumento
        var response = handler.emailAlreadyRegistered(ex, mockReq("/auth/register"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().get("message")).isEqualTo("Email already registered");
    }

    @Test
    @DisplayName("UsernameAlreadyTakenException -> 409 Conflict (mensaje por defecto)")
    void usernameTaken_returns409() {
        // Asumimos que tu excepción por defecto dice exactamente "Username already taken"
        var ex = new UsernameAlreadyTakenException(); // <-- sin argumento
        var response = handler.usernameTaken(ex, mockReq("/auth/register"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().get("message")).isEqualTo("Username already taken");
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
    @DisplayName("PetDeceasedException -> 410 Gone (mensaje tal cual)")
    void petDeceased_returns410() {
        var ex = new PetDeceasedException("Your pet is deceased");
        var response = handler.petDeceased(ex, mockReq("/pets/5/actions/feed"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.GONE);
        assertThat(response.getBody().get("message")).isEqualTo("Your pet is deceased");
    }

    @Test
    @DisplayName("PetNotHungryException -> 409 Conflict (mensaje con emoji por defecto)")
    void petNotHungry_returns409() {
        var ex = new PetNotHungryException(); // <-- mensaje con emojis
        var response = handler.petNotHungry(ex, mockReq("/pets/5/actions/feed"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().get("message"))
                .isEqualTo("Your pet is not hungry right now 🐶🍗");
    }

    @Test
    @DisplayName("PetAlreadyCleanException -> 409 Conflict (mensaje con emoji por defecto)")
    void petAlreadyClean_returns409() {
        var ex = new PetAlreadyCleanException(); // <-- mensaje con emojis
        var response = handler.petAlreadyClean(ex, mockReq("/pets/5/actions/wash"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().get("message"))
                .isEqualTo("Your pet is already clean 🧼✨");
    }

    @Test
    @DisplayName("PetTooHappyException -> 409 Conflict (mensaje con emoji por defecto)")
    void petTooHappy_returns409() {
        var ex = new PetTooHappyException(); // <-- mensaje con emojis
        var response = handler.petTooHappy(ex, mockReq("/pets/5/actions/play"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().get("message"))
                .isEqualTo("Your pet is already too happy and tired to play 🎾💤");
    }

    @Test
    @DisplayName("Excepción genérica -> 500 Internal Server Error")
    void generic_returns500() {
        var response = handler.generic(new Exception("boom"), mockReq("/whatever"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().get("message")).isEqualTo("Internal Server Error");
        assertThat(response.getBody().get("path")).isEqualTo("/whatever");
    }
}
