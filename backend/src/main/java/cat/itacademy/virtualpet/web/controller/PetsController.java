package cat.itacademy.virtualpet.web.controller;

import cat.itacademy.virtualpet.application.dto.pet.*;
import cat.itacademy.virtualpet.application.service.pet.PetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing virtual pets.
 * All endpoints require JWT authentication.
 */
@RestController
@RequestMapping("/pets")
@SecurityRequirement(name = "bearerAuth") // Swagger: activa el candado
public class PetsController {

    private final PetService petService;

    public PetsController(PetService petService) {
        this.petService = petService;
    }

    // =============== CRUD ===============

    @Operation(summary = "Create a new pet (only for authenticated users)")
    @PostMapping
    public ResponseEntity<PetResponse> createPet(
            @Valid @RequestBody PetCreateRequest request,
            Authentication authentication) {

        String email = authentication.getName(); // sub del JWT = email
        PetResponse response = petService.createPet(request, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get all pets (own pets if user, all if admin)")
    @GetMapping
    public ResponseEntity<List<PetResponse>> getAllPets(Authentication authentication) {
        String email = authentication.getName();
        List<PetResponse> pets = petService.getAllPets(email);
        return ResponseEntity.ok(pets);
    }

    @Operation(summary = "Get a pet by ID (owner or admin only)")
    @GetMapping("/{id}")
    public ResponseEntity<PetResponse> getPetById(
            @PathVariable Long id,
            Authentication authentication) {

        String email = authentication.getName();
        PetResponse pet = petService.getPetById(id, email);
        return ResponseEntity.ok(pet);
    }

    @Operation(summary = "Update a pet (only by owner or admin)")
    @PutMapping("/{id}")
    public ResponseEntity<PetResponse> updatePet(
            @PathVariable Long id,
            @Valid @RequestBody PetUpdateRequest request,
            Authentication authentication) {

        String email = authentication.getName();
        PetResponse updated = petService.updatePet(id, request, email);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Delete a pet (only by owner or admin)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePet(
            @PathVariable Long id,
            Authentication authentication) {

        String email = authentication.getName();
        petService.deletePet(id, email);
        return ResponseEntity.noContent().build();
    }

    // =============== ACTIONS ===============

    @Operation(summary = "Feed a pet (hunger -50, hygiene -5, +1 action)")
    @PostMapping("/{id}/actions/feed")
    public ResponseEntity<PetActionResponse> feedPet(
            @PathVariable Long id,
            Authentication authentication) {

        String email = authentication.getName();
        PetActionResponse response = petService.feed(id, email);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Wash a pet (hygiene +30, hunger +5, +1 action)")
    @PostMapping("/{id}/actions/wash")
    public ResponseEntity<PetActionResponse> washPet(
            @PathVariable Long id,
            Authentication authentication) {

        String email = authentication.getName();
        PetActionResponse response = petService.wash(id, email);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Play with a pet (fun +40, hunger +10, +1 action)")
    @PostMapping("/{id}/actions/play")
    public ResponseEntity<PetActionResponse> playWithPet(
            @PathVariable Long id,
            Authentication authentication) {

        String email = authentication.getName();
        PetActionResponse response = petService.play(id, email);
        return ResponseEntity.ok(response);
    }
}
