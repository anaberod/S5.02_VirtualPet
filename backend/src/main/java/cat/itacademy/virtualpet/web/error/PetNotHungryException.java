package cat.itacademy.virtualpet.web.error;


public class PetNotHungryException extends RuntimeException {
    public PetNotHungryException() {
        super("Your pet is not hungry right now 🐶🍗");
    }
}
