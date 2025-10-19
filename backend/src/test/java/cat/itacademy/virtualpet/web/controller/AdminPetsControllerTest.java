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

import java.time.Instant;
import java.util.Set;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 🔧 Integration test for AdminPetsController.
 * Runs with the full Spring Boot context, Security + JWT + DB reales (perfil test).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminPetsControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtService jwtService;
    @Autowired private UserRepository userRepository;
    @Autowired private PetRepository petRepository;

    private String adminToken;
    private User admin;
    private Pet pet;

    @BeforeEach
    void setup() {
        // Estado limpio para cada test
        petRepository.deleteAll();
        userRepository.deleteAll();

        // --- Admin real con ROLE_ADMIN ---
        admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@example.com");
        admin.setPasswordHash("secret"); // no valida password en estos tests
        admin.setRoles(Set.of("ROLE_ADMIN"));
        admin = userRepository.save(admin);

        // --- Mascota válida (rellenando NOT NULL) ---
        pet = new Pet();
        pet.setName("Rex");
        pet.setBreed(Breed.LABRADOR);        // NOT NULL
        pet.setLifeStage(LifeStage.BABY);    // NOT NULL
        pet.setActionCount(0);               // NOT NULL
        pet.setDead(false);                  // NOT NULL
        pet.setHunger(40);
        pet.setHygiene(80);
        pet.setFun(70);
        pet.setOwner(admin);
        // Si tu entidad usa @CreationTimestamp no hace falta; si no, descomenta:
        // pet.setCreatedAt(Instant.now());
        pet = petRepository.save(pet);

        // --- JWT válido para el admin ---
        adminToken = "Bearer " + jwtService.generateToken(admin);
    }

    @Test
    @DisplayName("GET /admin/pets devuelve 200 y lista mascotas")
    void listAllPets_ok() throws Exception {
        mockMvc.perform(
                        get("/admin/pets")
                                .header("Authorization", adminToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(pet.getId()))
                .andExpect(jsonPath("$.content[0].name").value("Rex"));
    }

    @Test
    @DisplayName("GET /admin/pets/{id} devuelve 200 con la mascota")
    void getPetById_ok() throws Exception {
        mockMvc.perform(
                        get("/admin/pets/{id}", pet.getId())
                                .header("Authorization", adminToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(pet.getId()))
                .andExpect(jsonPath("$.name").value("Rex"));
    }

    @Test
    @DisplayName("GET /admin/pets/{id} devuelve 404 si no existe")
    void getPetById_notFound() throws Exception {
        mockMvc.perform(
                        get("/admin/pets/{id}", Long.MAX_VALUE)
                                .header("Authorization", adminToken)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /admin/pets/{id} elimina mascota → 204")
    void deletePet_ok() throws Exception {
        mockMvc.perform(
                        delete("/admin/pets/{id}", pet.getId())
                                .header("Authorization", adminToken)
                                .with(csrf()) // evita 403 por CSRF en POST/DELETE si está activo
                )
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("POST /admin/pets/{id}/actions/feed → 200 OK")
    void feedPet_ok() throws Exception {
        mockMvc.perform(
                        post("/admin/pets/{id}/actions/feed", pet.getId())
                                .header("Authorization", adminToken)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(pet.getId()));
    }

    @Test
    @DisplayName("POST /admin/pets/{id}/actions/wash → 200 OK")
    void washPet_ok() throws Exception {
        mockMvc.perform(
                        post("/admin/pets/{id}/actions/wash", pet.getId())
                                .header("Authorization", adminToken)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(pet.getId()));
    }

    @Test
    @DisplayName("POST /admin/pets/{id}/actions/play → 200 OK")
    void playPet_ok() throws Exception {
        mockMvc.perform(
                        post("/admin/pets/{id}/actions/play", pet.getId())
                                .header("Authorization", adminToken)
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(pet.getId()));
    }
}
