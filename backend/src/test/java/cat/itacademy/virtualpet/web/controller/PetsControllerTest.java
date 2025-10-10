package cat.itacademy.virtualpet.web.controller;

import cat.itacademy.virtualpet.BackendApplication;
import cat.itacademy.virtualpet.domain.pet.Pet;
import cat.itacademy.virtualpet.domain.pet.PetRepository;
import cat.itacademy.virtualpet.domain.pet.enums.Breed;
import cat.itacademy.virtualpet.domain.pet.enums.LifeStage;
import cat.itacademy.virtualpet.domain.user.User;
import cat.itacademy.virtualpet.domain.user.UserRepository;
import cat.itacademy.virtualpet.support.TestcontainersMySQL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Set;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = BackendApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PetsControllerTest extends TestcontainersMySQL {

    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired PetRepository pets;

    // ✅ Simula el filtro y servicio JWT
    @MockBean cat.itacademy.virtualpet.infrastructure.security.JwtService jwtService;

    String userEmail = "user@example.com";
    Long petId;

    @BeforeEach
    void setup() {
        pets.deleteAll();
        users.deleteAll();

        // --- Crear usuario ---
        User user = new User();
        user.setEmail(userEmail.toLowerCase());
        user.setUsername("user");
        user.setPasswordHash("$2a$10$dummy");
        user.setRoles(Set.of("ROLE_USER"));
        user = users.save(user);

        // --- Crear mascota ---
        Pet pet = new Pet();
        pet.setName("Kira");
        pet.setBreed(Breed.LABRADOR);
        pet.setHunger(45);
        pet.setHygiene(70);
        pet.setFun(30);
        pet.setActionCount(1);
        pet.setLifeStage(LifeStage.BABY);
        pet.setOwner(user);
        pet = pets.save(pet);
        petId = pet.getId();

        // --- Mockear JwtService ---
        when(jwtService.extractEmail(anyString())).thenReturn(userEmail);
        when(jwtService.isTokenValid(anyString(), org.mockito.ArgumentMatchers.any(User.class))).thenReturn(true);

        // --- Simular autenticación ---
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userEmail, null,
                        Set.of(new SimpleGrantedAuthority("ROLE_USER")))
        );
    }

    // ---------------- TESTS ----------------

    @Test
    @DisplayName("GET /pets → 401 sin token (sin contexto de seguridad)")
    void listPets_unauthorized() throws Exception {
        SecurityContextHolder.clearContext();
        mvc.perform(get("/pets"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /pets → 200 con token USER (solo sus mascotas)")
    void listPets_authorized() throws Exception {
        mvc.perform(get("/pets")
                        .header("Authorization", "Bearer faketoken"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].name").value("Kira"))
                .andExpect(jsonPath("$[0].breed").value("LABRADOR"))
                .andExpect(jsonPath("$[0].actionCount").isNumber())
                .andExpect(jsonPath("$[0].lifeStage").exists());
    }

    @Test
    @DisplayName("POST /pets/{id}/actions/feed|wash|play → aplican reglas y límites 0–100")
    void actions_shouldRespectRules() throws Exception {
        // Feed
        mvc.perform(post("/pets/{id}/actions/feed", petId)
                        .header("Authorization", "Bearer faketoken"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hunger").value(greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.hunger").value(lessThanOrEqualTo(100)));

        // Wash
        mvc.perform(post("/pets/{id}/actions/wash", petId)
                        .header("Authorization", "Bearer faketoken"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hygiene").value(lessThanOrEqualTo(100)));

        // Play
        mvc.perform(post("/pets/{id}/actions/play", petId)
                        .header("Authorization", "Bearer faketoken"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fun").value(lessThanOrEqualTo(100)));
    }

}
