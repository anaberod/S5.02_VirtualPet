package cat.itacademy.virtualpet.web.error;


public class InvalidEmailException extends RuntimeException {
    public InvalidEmailException() {
        super("Invalid email");
    }
}
