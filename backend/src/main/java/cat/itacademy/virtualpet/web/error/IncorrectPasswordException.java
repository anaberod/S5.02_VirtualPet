package cat.itacademy.virtualpet.web.error;

/** Se lanza cuando la contraseña no coincide con la almacenada. */
public class IncorrectPasswordException extends RuntimeException {
    public IncorrectPasswordException() {
        super("Incorrect password");
    }
}
