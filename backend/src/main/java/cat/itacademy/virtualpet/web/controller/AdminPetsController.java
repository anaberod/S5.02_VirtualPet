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


    @Operation(summary = "List all pets (ADMIN only, paginated)")
    @GetMapping
    public ResponseEntity<Page<PetResponse>> listAllPets(
            Authentication authentication,
            @Parameter(description = "Filter pets by owner ID", example = "2")
            @RequestParam(required = false) Long ownerId,
            @ParameterObject Pageable pageable
    ) {
        String adminEmail = authentication.getName();
        log.info("ADMIN {} requested PET LIST (ownerId={})", adminEmail, ownerId);
        log.debug("Pagination params | page={} size={} sort={}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

        try {
            Page<PetResponse> pets = petService.adminListPets(ownerId, pageable, adminEmail);
            log.info("ADMIN {} successfully retrieved {} pets", adminEmail, pets.getTotalElements());
            return ResponseEntity.ok(pets);
        } catch (Exception ex) {
            log.warn("ADMIN {} failed to list pets | reason={}", adminEmail, ex.getMessage());
            throw ex;
        }
    }


    @Operation(summary = "Get a pet by ID (ADMIN override)")
    @GetMapping("/{id}")
    public ResponseEntity<PetResponse> getPetById(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String adminEmail = authentication.getName();
        log.info("ADMIN {} requested PET {}", adminEmail, id);

        try {
            PetResponse pet = petService.getPetById(id, adminEmail);
            log.info("ADMIN {} retrieved PET {} successfully", adminEmail, id);
            return ResponseEntity.ok(pet);
        } catch (Exception ex) {
            log.warn("ADMIN {} failed to get PET {} | reason={}", adminEmail, id, ex.getMessage());
            throw ex;
        }
    }


    @Operation(summary = "Delete a pet (ADMIN override)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePet(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String adminEmail = authentication.getName();
        log.info("ADMIN {} attempting to DELETE PET {}", adminEmail, id);

        try {
            petService.deletePet(id, adminEmail);
            log.info("ADMIN {} successfully deleted PET {}", adminEmail, id);
            return ResponseEntity.noContent().build();
        } catch (Exception ex) {
            log.warn("ADMIN {} failed to delete PET {} | reason={}", adminEmail, id, ex.getMessage());
            throw ex;
        }
    }

    // ===================== ACTIONS =====================
    @Operation(summary = "Feed a pet (ADMIN override)")
    @PostMapping("/{id}/actions/feed")
    public ResponseEntity<PetActionResponse> feedPet(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String adminEmail = authentication.getName();
        log.info("ADMIN {} FEED PET {}", adminEmail, id);

        try {
            PetActionResponse response = petService.feed(id, adminEmail);
            log.info("ADMIN {} successfully FED PET {}", adminEmail, id);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            log.warn("ADMIN {} failed to FEED PET {} | reason={}", adminEmail, id, ex.getMessage());
            throw ex;
        }
    }

    @Operation(summary = "Wash a pet (ADMIN override)")
    @PostMapping("/{id}/actions/wash")
    public ResponseEntity<PetActionResponse> washPet(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String adminEmail = authentication.getName();
        log.info("ADMIN {} WASH PET {}", adminEmail, id);

        try {
            PetActionResponse response = petService.wash(id, adminEmail);
            log.info("ADMIN {} successfully WASHED PET {}", adminEmail, id);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            log.warn("ADMIN {} failed to WASH PET {} | reason={}", adminEmail, id, ex.getMessage());
            throw ex;
        }
    }

    @Operation(summary = "Play with a pet (ADMIN override)")
    @PostMapping("/{id}/actions/play")
    public ResponseEntity<PetActionResponse> playPet(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String adminEmail = authentication.getName();
        log.info("ADMIN {} PLAY PET {}", adminEmail, id);

        try {
            PetActionResponse response = petService.play(id, adminEmail);
            log.info("ADMIN {} successfully PLAYED with PET {}", adminEmail, id);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            log.warn("ADMIN {} failed to PLAY PET {} | reason={}", adminEmail, id, ex.getMessage());
            throw ex;
        }
    }
}
