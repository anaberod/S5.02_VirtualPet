package cat.itacademy.virtualpet.application.service.pet;

import cat.itacademy.virtualpet.application.dto.pet.*;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Business logic interface for managing pets.
 */
public interface PetService {

    PetResponse createPet(PetCreateRequest request, String userEmail);

    List<PetResponse> getAllPets(String userEmail);

    PetResponse getPetById(Long id, String userEmail);

    PetResponse updatePet(Long id, PetUpdateRequest request, String userEmail);
    Page<PetResponse> adminListPets(Long ownerId, Pageable pageable, String adminEmail);

    void deletePet(Long id, String userEmail);

    // Actions
    PetActionResponse feed(Long id, String userEmail);
    PetActionResponse wash(Long id, String userEmail);
    PetActionResponse play(Long id, String userEmail);
}
