package cat.itacademy.virtualpet.web.error;


public class PetTooHappyException extends RuntimeException {
    public PetTooHappyException() {
        super("Your pet is already too happy and tired to play 🎾💤");
    }
}
