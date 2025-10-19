package cat.itacademy.virtualpet.application.dto.pet;

import cat.itacademy.virtualpet.domain.pet.enums.Breed;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;


@Schema(description = "Request data for creating a new pet.")
public class PetCreateRequest {

    @NotBlank(message = "Pet name cannot be blank")
    @Size(max = 30, message = "Pet name cannot exceed 30 characters")
    @Schema(description = "Pet name", example = "Buddy", maxLength = 30)
    private String name;

    @NotNull(message = "Breed is required")
    @Schema(description = "Pet breed", example = "LABRADOR", allowableValues = {"DALMATIAN", "LABRADOR", "GOLDEN_RETRIEVER"})
    private Breed breed;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Breed getBreed() {
        return breed;
    }

    public void setBreed(Breed breed) {
        this.breed = breed;
    }
}
