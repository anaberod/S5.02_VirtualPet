package cat.itacademy.virtualpet.web.error;

/**
 * Se lanza cuando se intenta registrar un email que ya existe en el sistema.
 */
public class EmailAlreadyRegisteredException extends RuntimeException {

    /** Mensaje por defecto. */
    private static final String DEFAULT_MESSAGE = "Email already registered";

    public EmailAlreadyRegisteredException() {
        super(DEFAULT_MESSAGE);
    }

    /** Opcional: incluye el email en el mensaje (útil para logs). */
    public EmailAlreadyRegisteredException(String email) {
        super(DEFAULT_MESSAGE + (email != null ? (": " + email) : ""));
    }
}
