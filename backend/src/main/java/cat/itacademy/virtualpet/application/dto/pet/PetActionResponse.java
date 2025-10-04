package cat.itacademy.virtualpet.application.dto.pet;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response after performing an action (feed, wash, play).
 * For simplicity, same structure as PetResponse.
 */
@Schema(description = "Response returned after performing an action on a pet.")
public class PetActionResponse extends PetResponse {
}
