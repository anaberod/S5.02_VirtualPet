package cat.itacademy.virtualpet.web.error;

/**
 * Thrown when the user tries to wash a pet that is already fully clean.
 */
public class PetAlreadyCleanException extends RuntimeException {
    public PetAlreadyCleanException() {
        super("Your pet is already clean 🧼✨");
    }
}
