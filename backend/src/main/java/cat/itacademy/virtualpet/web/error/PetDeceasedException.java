package cat.itacademy.virtualpet.web.error;

/**
 * Excepción lanzada cuando se intenta realizar una acción sobre
 * una mascota que ya ha fallecido.
 *
 * Será gestionada por el GlobalExceptionHandler y devolverá HTTP 410 Gone.
 */
public class PetDeceasedException extends RuntimeException {

    public PetDeceasedException() {
        super("Your pet has passed away 💔");
    }

    public PetDeceasedException(String message) {
        super(message);
    }
}
