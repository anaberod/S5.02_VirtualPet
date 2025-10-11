package cat.itacademy.virtualpet.web.error;

/**
 * Se lanza cuando un usuario intenta registrarse con un nombre de usuario ya existente.
 */
public class UsernameAlreadyTakenException extends RuntimeException {

    /** Mensaje por defecto. */
    private static final String DEFAULT_MESSAGE = "Username already taken";

    public UsernameAlreadyTakenException() {
        super(DEFAULT_MESSAGE);
    }

    /** Opcional: incluir el username en el mensaje (útil para logs). */
    public UsernameAlreadyTakenException(String username) {
        super(DEFAULT_MESSAGE + (username != null ? (": " + username) : ""));
    }
}
