package cat.itacademy.virtualpet.web.controller;

import cat.itacademy.virtualpet.domain.pet.Pet;
import cat.itacademy.virtualpet.domain.pet.PetRepository;
import cat.itacademy.virtualpet.domain.pet.enums.Breed;
import cat.itacademy.virtualpet.domain.pet.enums.LifeStage;
import cat.itacademy.virtualpet.domain.user.User;
import cat.itacademy.virtualpet.domain.user.UserRepository;
import cat.itacademy.virtualpet.infrastructure.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 🔧 Integration test for AdminUsersController.
 * Ejecuta contexto real: Security + JWT + BD (perfil test).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminUsersControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtService jwtService;
    @Autowired private UserRepository userRepository;
    @Autowired private PetRepository petRepository;

    private String adminToken;
    private User admin;
    private User user;
    private Pet userPet;

    @BeforeEach
    void setup() {
        // Estado limpio
        petRepository.deleteAll();
        userRepository.deleteAll();

        // --- Admin con ROLE_ADMIN ---
        admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@example.com");
        admin.setPasswordHash("secret");
        admin.setRoles(Set.of("ROLE_ADMIN"));
        admin = userRepository.save(admin);

        // --- Usuario normal ---
        user = new User();
        user.setUsername("alice");
        user.setEmail("alice@example.com");
        user.setPasswordHash("pwd");
        user.setRoles(Set.of("ROLE_USER"));
        user = userRepository.save(user);

        // --- Mascota del usuario (campos NOT NULL rellenos) ---
        userPet = new Pet();
        userPet.setName("Kira");
        userPet.setBreed(Breed.LABRADOR);      // NOT NULL
        userPet.setLifeStage(LifeStage.BABY);  // NOT NULL
        userPet.setActionCount(0);             // NOT NULL
        userPet.setDead(false);                // NOT NULL
        userPet.setHunger(30);
        userPet.setHygiene(90);
        userPet.setFun(70);
        userPet.setOwner(user);
        userPet = petRepository.save(userPet);

        // --- JWT del admin ---
        adminToken = "Bearer " + jwtService.generateToken(admin);
    }

    // ================= LIST USERS =================

    @Test
    @DisplayName("GET /admin/users → 200 y lista de usuarios")
    void getAllUsers_ok() throws Exception {
        mockMvc.perform(
                        get("/admin/users")
                                .header("Authorization", adminToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[1].id").exists());
    }

    // ================= GET USER BY ID =================

    @Test
    @DisplayName("GET /admin/users/{id} → 200 con el usuario")
    void getUserById_ok() throws Exception {
        mockMvc.perform(
                        get("/admin/users/{id}", user.getId())
                                .header("Authorization", adminToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    @Test
    @DisplayName("GET /admin/users/{id} → 404 si no existe")
    void getUserById_notFound() throws Exception {
        mockMvc.perform(
                        get("/admin/users/{id}", Long.MAX_VALUE)
                                .header("Authorization", adminToken)
                )
                .andExpect(status().isNotFound());
    }

    // ================= GET USER PETS =================

    @Test
    @DisplayName("GET /admin/users/{id}/pets → 200 con mascotas del usuario")
    void getUserPets_ok() throws Exception {
        mockMvc.perform(
                        get("/admin/users/{id}/pets", user.getId())
                                .header("Authorization", adminToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(userPet.getId()))
                .andExpect(jsonPath("$[0].name").value("Kira"));
    }

    // ================= DELETE PET OF USER =================

    @Test
    @DisplayName("DELETE /admin/users/{userId}/pets/{petId} → 200 OK (con body)")
    void deletePetOfUser_ok() throws Exception {
        mockMvc.perform(
                        delete("/admin/users/{userId}/pets/{petId}", user.getId(), userPet.getId())
                                .header("Authorization", adminToken)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.petId").value(userPet.getId()));

        // Verifica que ya no existe la mascota
        assertThat(petRepository.findById(userPet.getId())).isEmpty();
    }

    // ================= DELETE USER (and pets) =================

    @Test
    @DisplayName("DELETE /admin/users/{id} → 204 y elimina sus mascotas")
    void deleteUser_ok() throws Exception {
        mockMvc.perform(
                        delete("/admin/users/{id}", user.getId())
                                .header("Authorization", adminToken)
                                .with(csrf())
                )
                .andExpect(status().isNoContent());

        assertThat(userRepository.findById(user.getId())).isEmpty();
        assertThat(petRepository.findAllByOwnerId(user.getId())).isEmpty();
    }
}
