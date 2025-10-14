package cat.itacademy.virtualpet.web.controller;

import cat.itacademy.virtualpet.application.dto.pet.PetActionResponse;
import cat.itacademy.virtualpet.application.dto.pet.PetResponse;
import cat.itacademy.virtualpet.application.service.pet.PetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests del AdminPetsController en modo standalone (sin ApplicationContext).
 * Inyectamos el Authentication con .principal(...) en cada request.
 */
class AdminPetsControllerStandaloneTest {

    private static final String ADMIN_EMAIL = "admin@example.com";

    private PetService petService;
    private MockMvc mockMvc;

    private Authentication adminAuth() {
        return new UsernamePasswordAuthenticationToken(
                ADMIN_EMAIL, "N/A",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    @BeforeEach
    void setup() {
        petService = mock(PetService.class);
        AdminPetsController controller = new AdminPetsController(petService);

        // Sólo necesitamos el resolver de Pageable
        PageableHandlerMethodArgumentResolver pageableResolver =
                new PageableHandlerMethodArgumentResolver();

        this.mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setCustomArgumentResolvers(pageableResolver)
                .build();
    }

    // ---------------- LIST (200) ----------------
    @Test
    @DisplayName("GET /admin/pets -> 200 y devuelve página")
    void listAllPets_ok() throws Exception {
        Page<PetResponse> page = new PageImpl<>(
                List.of(pet(1L), pet(2L)), PageRequest.of(0, 2), 2);

        given(petService.adminListPets(
                ArgumentMatchers.isNull(),   // ownerId
                ArgumentMatchers.any(),      // pageable
                ArgumentMatchers.eq(ADMIN_EMAIL))
        ).willReturn(page);

        mockMvc.perform(
                        get("/admin/pets")
                                .param("page", "0")
                                .param("size", "2")
                                .principal(adminAuth())   // << inyecta Authentication
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    // ------------- GET BY ID (200 / 404) -------------
    @Test
    @DisplayName("GET /admin/pets/7 -> 200 con pet")
    void getPetById_ok() throws Exception {
        given(petService.getPetById(7L, ADMIN_EMAIL)).willReturn(pet(7L));

        mockMvc.perform(get("/admin/pets/7").principal(adminAuth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7));
    }

    @Test
    @DisplayName("GET /admin/pets/999 -> 404 si no existe")
    void getPetById_notFound() throws Exception {
        given(petService.getPetById(999L, ADMIN_EMAIL))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet not found"));

        mockMvc.perform(get("/admin/pets/999").principal(adminAuth()))
                .andExpect(status().isNotFound());
    }

    // ---------------- DELETE (204) ----------------
    @Test
    @DisplayName("DELETE /admin/pets/3 -> 204 sin contenido")
    void deletePet_noContent() throws Exception {
        doNothing().when(petService).deletePet(3L, ADMIN_EMAIL);

        mockMvc.perform(delete("/admin/pets/3").principal(adminAuth()))
                .andExpect(status().isNoContent());
    }

    // ---------------- ACTIONS (200) ----------------
    @Test
    @DisplayName("POST /admin/pets/5/actions/feed -> 200 con body")
    void feed_ok() throws Exception {
        given(petService.feed(5L, ADMIN_EMAIL)).willReturn(action(5L, "Fed"));

        mockMvc.perform(post("/admin/pets/5/actions/feed").principal(adminAuth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.message").value("Fed"));
    }

    @Test
    @DisplayName("POST /admin/pets/8/actions/wash -> 200 con body")
    void wash_ok() throws Exception {
        given(petService.wash(8L, ADMIN_EMAIL)).willReturn(action(8L, "Washed"));

        mockMvc.perform(post("/admin/pets/8/actions/wash").principal(adminAuth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(8))
                .andExpect(jsonPath("$.message").value("Washed"));
    }

    @Test
    @DisplayName("POST /admin/pets/4/actions/play -> 200 con body")
    void play_ok() throws Exception {
        given(petService.play(4L, ADMIN_EMAIL)).willReturn(action(4L, "Played"));

        mockMvc.perform(post("/admin/pets/4/actions/play").principal(adminAuth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.message").value("Played"));
    }

    // ---------------- helpers ----------------
    private PetResponse pet(long id) {
        PetResponse r = new PetResponse();
        r.setId(id);
        r.setName("Firulais-" + id);
        r.setBreed(null);        // evitamos depender de tus enums concretos
        r.setLifeStage(null);
        r.setHunger(50);
        r.setHygiene(60);
        r.setFun(70);
        r.setActionCount(3);
        r.setOwnerId(1L);
        r.setCreatedAt(Instant.now());
        r.setDead(false);
        r.setDeathAt(null);
        return r;
    }

    private PetActionResponse action(long id, String msg) {
        PetActionResponse a = new PetActionResponse();
        a.setId(id);
        a.setName("Firulais-" + id);
        a.setBreed(null);
        a.setLifeStage(null);
        a.setHunger(40);
        a.setHygiene(80);
        a.setFun(90);
        a.setActionCount(4);
        a.setOwnerId(1L);
        a.setCreatedAt(Instant.now());
        a.setDead(false);
        a.setDeathAt(null);
        a.setMessage(msg);
        a.setWarnings(List.of());
        return a;
    }
}
