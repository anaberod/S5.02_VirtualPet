package cat.itacademy.virtualpet.web.controller;

import cat.itacademy.virtualpet.BackendApplication;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 🔧 Integration test para PetsController (endpoints de usuario):
 * - Contexto completo (Security + JWT real + BD del perfil test)
 * - Sin SecurityContext manual ni mocks de JwtService
 */
@SpringBootTest(classes = BackendApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PetsControllerTest  {

    @Autowired MockMvc mvc;
    @Autowired JwtService jwtService;
    @Autowired UserRepository users;
    @Autowired PetRepository pets;

    private User owner;
    private User other;
    private Pet ownerPet;
    private Pet otherPet;
    private String ownerBearer;

    @BeforeEach
    void setup() {
        pets.deleteAll();
        users.deleteAll();

        // --- Users ---
        owner = new User();
        owner.setUsername("user");
        owner.setEmail("user@example.com");
        owner.setPasswordHash("$2a$10$dummy");
        owner.setRoles(Set.of("ROLE_USER"));
        owner = users.save(owner);

        other = new User();
        other.setUsername("other");
        other.setEmail("other@example.com");
        other.setPasswordHash("$2a$10$dummy");
        other.setRoles(Set.of("ROLE_USER"));
        other = users.save(other);

        // --- Pets (NOT NULLs cubiertos) ---
        ownerPet = new Pet();
        ownerPet.setName("Kira");
        ownerPet.setBreed(Breed.LABRADOR);
        ownerPet.setLifeStage(LifeStage.BABY);
        ownerPet.setActionCount(1);
        ownerPet.setDead(false);
        ownerPet.setHunger(45);
        ownerPet.setHygiene(70);
        ownerPet.setFun(30);
        ownerPet.setOwner(owner);
        ownerPet = pets.save(ownerPet);

        otherPet = new Pet();
        otherPet.setName("Max");
        otherPet.setBreed(Breed.DALMATIAN);
        otherPet.setLifeStage(LifeStage.BABY);
        otherPet.setActionCount(0);
        otherPet.setDead(false);
        otherPet.setHunger(20);
        otherPet.setHygiene(80);
        otherPet.setFun(80);
        otherPet.setOwner(other);
        otherPet = pets.save(otherPet);

        // --- JWT real del owner ---
        ownerBearer = "Bearer " + jwtService.generateToken(owner);
    }

    // ========== AUTH / LISTADO ==========

    @Test
    @DisplayName("GET /pets → 401 si no envías token")
    void listPets_unauthorized_withoutToken() throws Exception {
        mvc.perform(get("/pets"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /pets → 200 y devuelve SOLO las mascotas del usuario autenticado")
    void listPets_onlyOwners() throws Exception {
        mvc.perform(get("/pets").header("Authorization", ownerBearer))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(ownerPet.getId()))
                .andExpect(jsonPath("$[0].name").value("Kira"))
                .andExpect(jsonPath("$[0].breed").value("LABRADOR"));
    }

    // ========== ACCIONES HAPPY PATH (valores exactos + persistencia) ==========

    @Test
    @DisplayName("POST /pets/{id}/actions/feed → reduce hunger -70 (min 0), hygiene -5, fun -10, +1 acción")
    void feed_happyPath() throws Exception {
        // Estado inicial de referencia
        assertThat(ownerPet.getHunger()).isEqualTo(45);
        assertThat(ownerPet.getHygiene()).isEqualTo(70);
        assertThat(ownerPet.getFun()).isEqualTo(30);
        int prevActions = ownerPet.getActionCount();

        mvc.perform(post("/pets/{id}/actions/feed", ownerPet.getId())
                        .header("Authorization", ownerBearer)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ownerPet.getId()))
                .andExpect(jsonPath("$.hunger").value(0))    // 45 - 70 -> 0 (min 0)
                .andExpect(jsonPath("$.hygiene").value(65)) // 70 - 5
                .andExpect(jsonPath("$.fun").value(20));    // 30 - 10

        // Persistencia
        Pet reloaded = pets.findById(ownerPet.getId()).orElseThrow();
        assertThat(reloaded.getHunger()).isZero();
        assertThat(reloaded.getHygiene()).isEqualTo(65);
        assertThat(reloaded.getFun()).isEqualTo(20);
        assertThat(reloaded.getActionCount()).isEqualTo(prevActions + 1);
        assertThat(reloaded.getLifeStage()).isEqualTo(LifeStage.BABY);
    }

    @Test
    @DisplayName("POST /pets/{id}/actions/wash → hygiene +30 (max 100), hunger +10, fun -20, +1 acción")
    void wash_happyPath() throws Exception {
        // Dejar hygiene alta pero no al 100
        ownerPet.setHygiene(75);
        ownerPet = pets.save(ownerPet);
        int prevActions = ownerPet.getActionCount();

        mvc.perform(post("/pets/{id}/actions/wash", ownerPet.getId())
                        .header("Authorization", ownerBearer)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hygiene").value(100)) // 75 + 30 -> 100 top
                .andExpect(jsonPath("$.hunger").value(55))   // 45 + 10
                .andExpect(jsonPath("$.fun").value(10));     // 30 - 20

        Pet reloaded = pets.findById(ownerPet.getId()).orElseThrow();
        assertThat(reloaded.getHygiene()).isEqualTo(100);
        assertThat(reloaded.getHunger()).isEqualTo(55);
        assertThat(reloaded.getFun()).isEqualTo(10);
        assertThat(reloaded.getActionCount()).isEqualTo(prevActions + 1);
    }

    @Test
    @DisplayName("POST /pets/{id}/actions/play → fun +40 (max 100), hunger +15, +1 acción")
    void play_happyPath() throws Exception {
        int prevActions = ownerPet.getActionCount();

        mvc.perform(post("/pets/{id}/actions/play", ownerPet.getId())
                        .header("Authorization", ownerBearer)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fun").value(70))   // 30 + 40
                .andExpect(jsonPath("$.hunger").value(60)); // 45 + 15

        Pet reloaded = pets.findById(ownerPet.getId()).orElseThrow();
        assertThat(reloaded.getFun()).isEqualTo(70);
        assertThat(reloaded.getHunger()).isEqualTo(60);
        assertThat(reloaded.getActionCount()).isEqualTo(prevActions + 1);
    }

    // ========== ERRORES DE NEGOCIO (409 / 410) ==========

    @Test
    @DisplayName("feed → 409 si hunger ya es 0 (PetNotHungryException)")
    void feed_conflictWhenNotHungry() throws Exception {
        ownerPet.setHunger(0);
        pets.save(ownerPet);

        mvc.perform(post("/pets/{id}/actions/feed", ownerPet.getId())
                        .header("Authorization", ownerBearer)
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("wash → 409 si hygiene ya es 100 (PetAlreadyCleanException)")
    void wash_conflictWhenAlreadyClean() throws Exception {
        ownerPet.setHygiene(100);
        pets.save(ownerPet);

        mvc.perform(post("/pets/{id}/actions/wash", ownerPet.getId())
                        .header("Authorization", ownerBearer)
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("play → 409 si fun ya es 100 (PetTooHappyException)")
    void play_conflictWhenTooHappy() throws Exception {
        ownerPet.setFun(100);
        pets.save(ownerPet);

        mvc.perform(post("/pets/{id}/actions/play", ownerPet.getId())
                        .header("Authorization", ownerBearer)
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @DisplayName("acciones → 410 si la mascota está fallecida (PetDeceasedException)")
    void action_goneWhenDeceased() throws Exception {
        ownerPet.setDead(true);
        ownerPet.setLifeStage(LifeStage.PASSED);
        ownerPet.setDeathAt(Instant.now());
        pets.save(ownerPet);

        mvc.perform(post("/pets/{id}/actions/feed", ownerPet.getId())
                        .header("Authorization", ownerBearer)
                        .with(csrf()))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.status").value(410));
    }

    // ========== 404 / 403 ==========

    @Test
    @DisplayName("GET /pets/{id} → 404 si no existe la mascota")
    void getPetById_notFound() throws Exception {
        mvc.perform(get("/pets/{id}", Long.MAX_VALUE)
                        .header("Authorization", ownerBearer))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST acciones sobre mascota de otro usuario → 403")
    void actions_forbiddenWhenPetBelongsToAnotherUser() throws Exception {
        mvc.perform(post("/pets/{id}/actions/feed", otherPet.getId())
                        .header("Authorization", ownerBearer)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    // ========== EVOLUCIÓN DE ETAPA ==========

    @Test
    @DisplayName("lifeStage BABY→ADULT al pasar de 4→5 acciones")
    void lifeStage_babyToAdult() throws Exception {
        ownerPet.setActionCount(4);
        ownerPet.setLifeStage(LifeStage.BABY);
        pets.save(ownerPet);

        mvc.perform(post("/pets/{id}/actions/play", ownerPet.getId())
                        .header("Authorization", ownerBearer)
                        .with(csrf()))
                .andExpect(status().isOk());

        Pet reloaded = pets.findById(ownerPet.getId()).orElseThrow();
        assertThat(reloaded.getActionCount()).isEqualTo(5);
        assertThat(reloaded.getLifeStage()).isEqualTo(LifeStage.ADULT);
    }

    @Test
    @DisplayName("lifeStage ADULT→SENIOR al pasar de 9→10 acciones")
    void lifeStage_adultToSenior() throws Exception {
        ownerPet.setActionCount(9);
        ownerPet.setLifeStage(LifeStage.ADULT);
        pets.save(ownerPet);

        mvc.perform(post("/pets/{id}/actions.play".replace('.', '/'), ownerPet.getId())
                        .header("Authorization", ownerBearer)
                        .with(csrf()))
                .andExpect(status().isOk());

        Pet reloaded = pets.findById(ownerPet.getId()).orElseThrow();
        assertThat(reloaded.getActionCount()).isEqualTo(10);
        assertThat(reloaded.getLifeStage()).isEqualTo(LifeStage.SENIOR);
    }
}
