package cat.itacademy.virtualpet.application.dto.pet;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response after performing an action (feed, wash, play).
 * Inherits all fields from PetResponse.
 */
@Schema(description = "Response returned after performing an action on a pet.")
public class PetActionResponse extends PetResponse {

    // 🆕 Optional: message to indicate special events (e.g. death)
    private String message;

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
