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

/**
 * Controller para la administración de usuarios (solo ROLE_ADMIN).
 * Permite listar usuarios, ver detalles, listar mascotas y eliminar usuarios o mascotas.
 */
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
        log.info("ADMIN {} requested full user list", adminEmail);
        List<UserResponse> users = userService.getAllUsers(adminEmail);
        return ResponseEntity.ok(users);
    }

    // ===================== VER DETALLE DE UN USUARIO =====================

    @Operation(summary = "Get user details by ID (ADMIN only)")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable Long id,
            Authentication authentication) {

        String adminEmail = authentication.getName();
        log.info("ADMIN {} requested user {}", adminEmail, id);
        UserResponse user = userService.getUserById(id, adminEmail);
        return ResponseEntity.ok(user);
    }

    // ===================== LISTAR MASCOTAS DE UN USUARIO =====================

    @Operation(summary = "List all pets of a user (ADMIN only)")
    @GetMapping("/{id}/pets")
    public ResponseEntity<List<PetResponse>> getUserPets(
            @PathVariable Long id,
            Authentication authentication) {

        String adminEmail = authentication.getName();
        log.info("ADMIN {} requested pets of user {}", adminEmail, id);
        List<PetResponse> pets = userService.getUserPets(id, adminEmail);
        return ResponseEntity.ok(pets);
    }

    // ===================== ELIMINAR UN USUARIO COMPLETO =====================

    @Operation(summary = "Delete a user and all their pets (ADMIN only)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id,
            Authentication authentication) {

        String adminEmail = authentication.getName();
        log.info("ADMIN {} deleted user {}", adminEmail, id);
        userService.deleteUser(id, adminEmail);
        return ResponseEntity.noContent().build();
    }

    // ===================== ELIMINAR UNA MASCOTA CONCRETA =====================

    @Operation(summary = "Delete a pet of a specific user (ADMIN only)")
    @DeleteMapping("/{userId}/pets/{petId}")
    public ResponseEntity<Map<String, Object>> deletePetOfUser(
            @PathVariable Long userId,
            @PathVariable Long petId,
            Authentication authentication) {

        String adminEmail = authentication.getName();
        log.info("ADMIN {} deleted pet {} of user {}", adminEmail, petId, userId);

        userService.deleteUserPet(userId, petId, adminEmail);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Pet deleted successfully");
        response.put("petId", petId);

        return ResponseEntity.ok(response);
    }


}
