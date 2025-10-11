package cat.itacademy.virtualpet.web.error;

/**
 * Thrown when the user tries to feed a pet whose hunger is already at minimum.
 */
public class PetNotHungryException extends RuntimeException {
    public PetNotHungryException() {
        super("Your pet is not hungry right now 🐶🍗");
    }
}
