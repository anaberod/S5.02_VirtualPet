package cat.itacademy.virtualpet.application.dto.pet;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Request body for updating an existing pet (e.g., rename).
 */
@Schema(description = "Request data for updating an existing pet.")
public class PetUpdateRequest {

    @NotBlank(message = "Pet name cannot be blank")
    @Size(max = 30, message = "Pet name cannot exceed 30 characters")
    @Schema(description = "New name of the pet", example = "Lucky", maxLength = 30)
    private String name;

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
