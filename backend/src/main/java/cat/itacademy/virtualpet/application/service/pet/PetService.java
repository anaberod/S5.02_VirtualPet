package cat.itacademy.virtualpet.application.service.pet;

import cat.itacademy.virtualpet.application.dto.pet.*;
import java.util.List;

/**
 * Business logic interface for managing pets.
 */
public interface PetService {

    PetResponse createPet(PetCreateRequest request, String userEmail);

    List<PetResponse> getAllPets(String userEmail);

    PetResponse getPetById(Long id, String userEmail);

    PetResponse updatePet(Long id, PetUpdateRequest request, String userEmail);

    void deletePet(Long id, String userEmail);

    // Actions
    PetActionResponse feed(Long id, String userEmail);
    PetActionResponse wash(Long id, String userEmail);
    PetActionResponse play(Long id, String userEmail);
}
