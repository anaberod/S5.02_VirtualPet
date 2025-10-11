package cat.itacademy.virtualpet.web.error;

/** Se lanza cuando el email no existe en el sistema. */
public class InvalidEmailException extends RuntimeException {
    public InvalidEmailException() {
        super("Invalid email");
    }
}
