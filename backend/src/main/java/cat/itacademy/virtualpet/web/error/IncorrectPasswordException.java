package cat.itacademy.virtualpet.web.error;


public class IncorrectPasswordException extends RuntimeException {
    public IncorrectPasswordException() {
        super("Incorrect password");
    }
}
