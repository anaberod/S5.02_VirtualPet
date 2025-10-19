package cat.itacademy.virtualpet.web.error;


public class EmailAlreadyRegisteredException extends RuntimeException {


    private static final String DEFAULT_MESSAGE = "Email already registered";

    public EmailAlreadyRegisteredException() {
        super(DEFAULT_MESSAGE);
    }


    public EmailAlreadyRegisteredException(String email) {
        super(DEFAULT_MESSAGE + (email != null ? (": " + email) : ""));
    }
}
