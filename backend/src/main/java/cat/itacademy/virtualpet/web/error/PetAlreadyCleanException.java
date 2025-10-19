package cat.itacademy.virtualpet.web.error;


public class PetAlreadyCleanException extends RuntimeException {
    public PetAlreadyCleanException() {
        super("Your pet is already clean 🧼✨");
    }
}
