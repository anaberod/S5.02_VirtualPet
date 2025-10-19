package cat.itacademy.virtualpet.web.error;


public class PetDeceasedException extends RuntimeException {

    public PetDeceasedException() {
        super("Your pet has passed away 💔");
    }

    public PetDeceasedException(String message) {
        super(message);
    }
}
