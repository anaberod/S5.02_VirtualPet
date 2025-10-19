package cat.itacademy.virtualpet.web.error;


public class UsernameAlreadyTakenException extends RuntimeException {


    private static final String DEFAULT_MESSAGE = "Username already taken";

    public UsernameAlreadyTakenException() {
        super(DEFAULT_MESSAGE);
    }


    public UsernameAlreadyTakenException(String username) {
        super(DEFAULT_MESSAGE + (username != null ? (": " + username) : ""));
    }
}
