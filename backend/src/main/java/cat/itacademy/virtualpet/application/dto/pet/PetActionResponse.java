package cat.itacademy.virtualpet.application.dto.pet;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/**
 * Response after performing an action (feed, wash, play).
 * Inherits all fields from PetResponse.
 */
@Schema(description = "Response returned after performing an action on a pet.")
public class PetActionResponse extends PetResponse {

    // Mensaje para eventos especiales (ej. muerte)
    private String message;

    // Avisos para el frontend (ej.: "hunger_high", "hygiene_low", "fun_low")
    private List<String> warnings;

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public List<String> getWarnings() { return warnings; }
    public void setWarnings(List<String> warnings) { this.warnings = warnings; }
}
