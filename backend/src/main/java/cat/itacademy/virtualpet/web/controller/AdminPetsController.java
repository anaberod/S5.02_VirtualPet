package cat.itacademy.virtualpet.web.controller;

import cat.itacademy.virtualpet.application.dto.pet.PetActionResponse;
import cat.itacademy.virtualpet.application.dto.pet.PetResponse;
import cat.itacademy.virtualpet.application.service.pet.PetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * ADMIN controller for global pet management.
 * Allows viewing, filtering, deleting and acting on any pet.
 */
@Slf4j
@RestController
@RequestMapping("/admin/pets")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin Pets", description = "Endpoints for ADMIN users to list, manage and interact with any pet.")
public class AdminPetsController {

    private final PetService petService;

    public AdminPetsController(PetService petService) {
        this.petService = petService;
    }

    // ===================== LIST ALL (PAGINATED + FILTER) =====================

    @Operation(
            summary = "List all pets (ADMIN only, paginated)",
            description = """
                    Paginated list of all pets. Optional filter by ownerId.
                    Example:
                    - /admin/pets?page=0&size=5&sort=createdAt,desc
                    - /admin/pets?ownerId=2&page=0&size=10
                    """
    )
    @GetMapping
    public ResponseEntity<Page<PetResponse>> listAllPets(
            Authentication authentication,
            @Parameter(description = "Filter pets by owner ID", example = "2")
            @RequestParam(required = false) Long ownerId,
            @ParameterObject Pageable pageable
    ) {
        String adminEmail = authentication.getName();
        log.info("ADMIN {} LIST pets ownerId={} page={} size={} sort={}",
                adminEmail, ownerId,
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

        Page<PetResponse> pets = petService.adminListPets(ownerId, pageable, adminEmail);
        return ResponseEntity.ok(pets);
    }

    // ===================== GET BY ID =====================

    @Operation(summary = "Get a pet by ID (ADMIN override)")
    @GetMapping("/{id}")
    public ResponseEntity<PetResponse> getPetById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String adminEmail = authentication.getName();
        log.info("ADMIN {} GET pet {}", adminEmail, id);

        PetResponse pet = petService.getPetById(id, adminEmail);
        return ResponseEntity.ok(pet);
    }

    // ===================== DELETE PET =====================

    @Operation(summary = "Delete a pet (ADMIN override)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePet(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String adminEmail = authentication.getName();
        log.info("ADMIN {} DELETE pet {}", adminEmail, id);

        petService.deletePet(id, adminEmail);
        return ResponseEntity.noContent().build();
    }

    // ===================== ACTIONS =====================

    @Operation(summary = "Feed a pet (ADMIN override)")
    @PostMapping("/{id}/actions/feed")
    public ResponseEntity<PetActionResponse> feedPet(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String adminEmail = authentication.getName();
        log.info("ADMIN {} FEED pet {}", adminEmail, id);

        PetActionResponse response = petService.feed(id, adminEmail);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Wash a pet (ADMIN override)")
    @PostMapping("/{id}/actions/wash")
    public ResponseEntity<PetActionResponse> washPet(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String adminEmail = authentication.getName();
        log.info("ADMIN {} WASH pet {}", adminEmail, id);

        PetActionResponse response = petService.wash(id, adminEmail);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Play with a pet (ADMIN override)")
    @PostMapping("/{id}/actions/play")
    public ResponseEntity<PetActionResponse> playPet(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String adminEmail = authentication.getName();
        log.info("ADMIN {} PLAY pet {}", adminEmail, id);

        PetActionResponse response = petService.play(id, adminEmail);
        return ResponseEntity.ok(response);
    }
}
