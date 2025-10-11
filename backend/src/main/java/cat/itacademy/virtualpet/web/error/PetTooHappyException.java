package cat.itacademy.virtualpet.web.error;

/**
 * Thrown when the user tries to play with a pet whose fun is already at maximum.
 */
public class PetTooHappyException extends RuntimeException {
    public PetTooHappyException() {
        super("Your pet is already too happy and tired to play 🎾💤");
    }
}
