package cat.itacademy.virtualpet.web.controller;

import cat.itacademy.virtualpet.application.dto.pet.*;
import cat.itacademy.virtualpet.application.service.pet.PetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Slf4j
@RestController
@RequestMapping("/pets")
@SecurityRequirement(name = "bearerAuth") // Swagger: activa el candado
public class PetsController {

    private final PetService petService;

    public PetsController(PetService petService) {
        this.petService = petService;
    }

    // =============== CREATE ===============
    @Operation(summary = "Create a new pet (only for authenticated users)")
    @PostMapping
    public ResponseEntity<PetResponse> createPet(
            @Valid @RequestBody PetCreateRequest request,
            Authentication authentication) {

        String email = authentication.getName();
        log.info("USER {} requested CREATE PET | breed={} name={}", email, request.getBreed(), request.getName());

        try {
            PetResponse response = petService.createPet(request, email);
            log.info("USER {} successfully CREATED PET id={}", email, response.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception ex) {
            log.warn("USER {} failed to CREATE PET | reason={}", email, ex.getMessage());
            throw ex;
        }
    }

    // =============== READ ALL ===============
    @Operation(summary = "Get all pets (own pets if user, all if admin)")
    @GetMapping
    public ResponseEntity<List<PetResponse>> getAllPets(Authentication authentication) {
        String email = authentication.getName();
        log.info("USER {} requested PET LIST", email);

        try {
            List<PetResponse> pets = petService.getAllPets(email);
            log.info("USER {} retrieved {} pets", email, pets.size());
            log.debug("First 3 pets preview: {}", pets.stream().limit(3).toList());
            return ResponseEntity.ok(pets);
        } catch (Exception ex) {
            log.warn("USER {} failed to LIST PETS | reason={}", email, ex.getMessage());
            throw ex;
        }
    }

    // =============== READ BY ID ===============
    @Operation(summary = "Get a pet by ID (owner or admin only)")
    @GetMapping("/{id}")
    public ResponseEntity<PetResponse> getPetById(
            @PathVariable Long id,
            Authentication authentication) {

        String email = authentication.getName();
        log.info("USER {} requested PET {}", email, id);

        try {
            PetResponse pet = petService.getPetById(id, email);
            log.info("USER {} retrieved PET {} successfully", email, id);
            log.debug("PET {} details: {}", id, pet);
            return ResponseEntity.ok(pet);
        } catch (Exception ex) {
            log.warn("USER {} failed to GET PET {} | reason={}", email, id, ex.getMessage());
            throw ex;
        }
    }

    // =============== UPDATE ===============
    @Operation(summary = "Update a pet (only by owner or admin)")
    @PutMapping("/{id}")
    public ResponseEntity<PetResponse> updatePet(
            @PathVariable Long id,
            @Valid @RequestBody PetUpdateRequest request,
            Authentication authentication) {

        String email = authentication.getName();
        log.info("USER {} attempting to UPDATE PET {}", email, id);

        try {
            PetResponse updated = petService.updatePet(id, request, email);
            log.info("USER {} successfully UPDATED PET {}", email, id);
            log.debug("Updated PET {} details: {}", id, updated);
            return ResponseEntity.ok(updated);
        } catch (Exception ex) {
            log.warn("USER {} failed to UPDATE PET {} | reason={}", email, id, ex.getMessage());
            throw ex;
        }
    }

    // =============== DELETE ===============
    @Operation(summary = "Delete a pet (only by owner or admin)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePet(
            @PathVariable Long id,
            Authentication authentication) {

        String email = authentication.getName();
        log.info("USER {} attempting to DELETE PET {}", email, id);

        try {
            petService.deletePet(id, email);
            log.info("USER {} successfully DELETED PET {}", email, id);
            return ResponseEntity.noContent().build();
        } catch (Exception ex) {
            log.warn("USER {} failed to DELETE PET {} | reason={}", email, id, ex.getMessage());
            throw ex;
        }
    }

    // =============== ACTIONS ===============
    @Operation(summary = "Feed a pet (hunger -50, hygiene -5, +1 action)")
    @PostMapping("/{id}/actions/feed")
    public ResponseEntity<PetActionResponse> feedPet(
            @PathVariable Long id,
            Authentication authentication) {

        String email = authentication.getName();
        log.info("USER {} FEED PET {}", email, id);

        try {
            PetActionResponse response = petService.feed(id, email);
            log.info("USER {} successfully FED PET {}", email, id);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            log.warn("USER {} failed to FEED PET {} | reason={}", email, id, ex.getMessage());
            throw ex;
        }
    }

    @Operation(summary = "Wash a pet (hygiene +30, hunger +5, +1 action)")
    @PostMapping("/{id}/actions/wash")
    public ResponseEntity<PetActionResponse> washPet(
            @PathVariable Long id,
            Authentication authentication) {

        String email = authentication.getName();
        log.info("USER {} WASH PET {}", email, id);

        try {
            PetActionResponse response = petService.wash(id, email);
            log.info("USER {} successfully WASHED PET {}", email, id);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            log.warn("USER {} failed to WASH PET {} | reason={}", email, id, ex.getMessage());
            throw ex;
        }
    }

    @Operation(summary = "Play with a pet (fun +40, hunger +10, +1 action)")
    @PostMapping("/{id}/actions/play")
    public ResponseEntity<PetActionResponse> playWithPet(
            @PathVariable Long id,
            Authentication authentication) {

        String email = authentication.getName();
        log.info("USER {} PLAY PET {}", email, id);

        try {
            PetActionResponse response = petService.play(id, email);
            log.info("USER {} successfully PLAYED with PET {}", email, id);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            log.warn("USER {} failed to PLAY PET {} | reason={}", email, id, ex.getMessage());
            throw ex;
        }
    }
}
