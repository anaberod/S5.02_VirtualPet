package cat.itacademy.virtualpet.web.controller;

import cat.itacademy.virtualpet.application.dto.pet.PetResponse;
import cat.itacademy.virtualpet.application.dto.user.UserResponse;
import cat.itacademy.virtualpet.application.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@RestController
@RequestMapping("/admin/users")
@SecurityRequirement(name = "bearerAuth") // Swagger: requiere JWT
public class AdminUsersController {

    private final UserService userService;

    public AdminUsersController(UserService userService) {
        this.userService = userService;
    }

    // ===================== LISTAR TODOS LOS USUARIOS =====================

    @Operation(summary = "List all users (ADMIN only)")
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers(Authentication authentication) {
        String adminEmail = authentication.getName();
        log.info("ADMIN {} requested FULL USER LIST", adminEmail);

        try {
            List<UserResponse> users = userService.getAllUsers(adminEmail);
            log.info("ADMIN {} retrieved {} users", adminEmail, users.size());
            log.debug("First 3 users preview: {}", users.stream().limit(3).toList());
            return ResponseEntity.ok(users);
        } catch (Exception ex) {
            log.warn("ADMIN {} failed to list users | reason={}", adminEmail, ex.getMessage());
            throw ex;
        }
    }

    // ===================== VER DETALLE DE UN USUARIO =====================

    @Operation(summary = "Get user details by ID (ADMIN only)")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable Long id,
            Authentication authentication) {

        String adminEmail = authentication.getName();
        log.info("ADMIN {} requested USER {}", adminEmail, id);

        try {
            UserResponse user = userService.getUserById(id, adminEmail);
            log.info("ADMIN {} retrieved USER {} successfully", adminEmail, id);
            log.debug("USER {} payload: {}", id, user);
            return ResponseEntity.ok(user);
        } catch (Exception ex) {
            log.warn("ADMIN {} failed to get USER {} | reason={}", adminEmail, id, ex.getMessage());
            throw ex;
        }
    }

    // ===================== LISTAR MASCOTAS DE UN USUARIO =====================

    @Operation(summary = "List all pets of a user (ADMIN only)")
    @GetMapping("/{id}/pets")
    public ResponseEntity<List<PetResponse>> getUserPets(
            @PathVariable Long id,
            Authentication authentication) {

        String adminEmail = authentication.getName();
        log.info("ADMIN {} requested PETS of USER {}", adminEmail, id);

        try {
            List<PetResponse> pets = userService.getUserPets(id, adminEmail);
            log.info("ADMIN {} retrieved {} pets for USER {}", adminEmail, pets.size(), id);
            log.debug("First 3 pets preview (user {}): {}", id, pets.stream().limit(3).toList());
            return ResponseEntity.ok(pets);
        } catch (Exception ex) {
            log.warn("ADMIN {} failed to list PETS of USER {} | reason={}", adminEmail, id, ex.getMessage());
            throw ex;
        }
    }

    // ===================== ELIMINAR UN USUARIO COMPLETO =====================

    @Operation(summary = "Delete a user and all their pets (ADMIN only)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id,
            Authentication authentication) {

        String adminEmail = authentication.getName();
        log.info("ADMIN {} attempting to DELETE USER {}", adminEmail, id);

        try {
            userService.deleteUser(id, adminEmail);
            log.info("ADMIN {} successfully DELETED USER {}", adminEmail, id);
            return ResponseEntity.noContent().build();
        } catch (Exception ex) {
            log.warn("ADMIN {} failed to DELETE USER {} | reason={}", adminEmail, id, ex.getMessage());
            throw ex;
        }
    }

    // ===================== ELIMINAR UNA MASCOTA CONCRETA =====================

    @Operation(summary = "Delete a pet of a specific user (ADMIN only)")
    @DeleteMapping("/{userId}/pets/{petId}")
    public ResponseEntity<Map<String, Object>> deletePetOfUser(
            @PathVariable Long userId,
            @PathVariable Long petId,
            Authentication authentication) {

        String adminEmail = authentication.getName();
        log.info("ADMIN {} attempting to DELETE PET {} of USER {}", adminEmail, petId, userId);

        try {
            userService.deleteUserPet(userId, petId, adminEmail);
            log.info("ADMIN {} successfully DELETED PET {} of USER {}", adminEmail, petId, userId);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Pet deleted successfully");
            response.put("petId", petId);

            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            log.warn("ADMIN {} failed to DELETE PET {} of USER {} | reason={}",
                    adminEmail, petId, userId, ex.getMessage());
            throw ex;
        }
    }
}
