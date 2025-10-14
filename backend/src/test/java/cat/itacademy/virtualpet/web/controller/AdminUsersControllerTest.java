package cat.itacademy.virtualpet.web.controller;

import cat.itacademy.virtualpet.application.dto.pet.PetResponse;
import cat.itacademy.virtualpet.application.dto.user.UserResponse;
import cat.itacademy.virtualpet.application.service.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver; // por si tuvieras pageable en el futuro
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests standalone de AdminUsersController.
 * No levanta Spring context. Inyecta Authentication con .principal(...).
 */
class AdminUsersControllerStandaloneTest {

    private static final String ADMIN_EMAIL = "admin@example.com";

    private UserService userService;
    private MockMvc mockMvc;

    private Authentication adminAuth() {
        return new UsernamePasswordAuthenticationToken(
                ADMIN_EMAIL,
                "N/A",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    @BeforeEach
    void setup() {
        userService = mock(UserService.class);
        AdminUsersController controller = new AdminUsersController(userService);

        // Si en algún endpoint usas Pageable en el futuro, este resolver evita errores de argumentos.
        PageableHandlerMethodArgumentResolver pageableResolver =
                new PageableHandlerMethodArgumentResolver();

        this.mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setCustomArgumentResolvers(pageableResolver)
                .build();
    }

    // ============ GET /admin/users (200) ============
    @Test
    @DisplayName("GET /admin/users -> 200 y devuelve lista")
    void getAllUsers_ok() throws Exception {
        List<UserResponse> users = List.of(user(1L, "alice@pets.com"), user(2L, "bob@pets.com"));
        given(userService.getAllUsers(ADMIN_EMAIL)).willReturn(users);

        mockMvc.perform(get("/admin/users").principal(adminAuth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    // ============ GET /admin/users/{id} (200) ============
    @Test
    @DisplayName("GET /admin/users/7 -> 200 con user")
    void getUserById_ok() throws Exception {
        given(userService.getUserById(7L, ADMIN_EMAIL)).willReturn(user(7L, "seven@pets.com"));

        mockMvc.perform(get("/admin/users/7").principal(adminAuth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7));
    }

    // ============ GET /admin/users/{id} (404) ============
    @Test
    @DisplayName("GET /admin/users/999 -> 404 si no existe")
    void getUserById_notFound() throws Exception {
        given(userService.getUserById(999L, ADMIN_EMAIL))
                .willThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        mockMvc.perform(get("/admin/users/999").principal(adminAuth()))
                .andExpect(status().isNotFound());
    }

    // ============ GET /admin/users/{id}/pets (200) ============
    @Test
    @DisplayName("GET /admin/users/5/pets -> 200 y devuelve lista de mascotas")
    void getUserPets_ok() throws Exception {
        List<PetResponse> pets = List.of(pet(10L), pet(11L));
        given(userService.getUserPets(5L, ADMIN_EMAIL)).willReturn(pets);

        mockMvc.perform(get("/admin/users/5/pets").principal(adminAuth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    // ============ DELETE /admin/users/{id} (204) ============
    @Test
    @DisplayName("DELETE /admin/users/3 -> 204 sin contenido")
    void deleteUser_noContent() throws Exception {
        doNothing().when(userService).deleteUser(3L, ADMIN_EMAIL);

        mockMvc.perform(delete("/admin/users/3").principal(adminAuth()))
                .andExpect(status().isNoContent());
    }

    // ============ DELETE /admin/users/{userId}/pets/{petId} (200) ============
    @Test
    @DisplayName("DELETE /admin/users/4/pets/22 -> 200 con body OK")
    void deletePetOfUser_ok() throws Exception {
        doNothing().when(userService).deleteUserPet(4L, 22L, ADMIN_EMAIL);

        mockMvc.perform(delete("/admin/users/4/pets/22").principal(adminAuth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("Pet deleted successfully"))
                .andExpect(jsonPath("$.petId").value(22));
    }

    // --------- helpers ---------
    private UserResponse user(Long id, String email) {
        UserResponse u = new UserResponse();
        // Suponiendo que tu DTO tiene setters para estos campos.
        // Si tu UserResponse tiene otros nombres, ajústalos aquí.
        try {
            // reflect para no depender de la forma exacta; si existen, los usamos
            UserResponse.class.getMethod("setId", Long.class).invoke(u, id);
        } catch (Exception ignored) {}
        try {
            UserResponse.class.getMethod("setEmail", String.class).invoke(u, email);
        } catch (Exception ignored) {}
        try {
            UserResponse.class.getMethod("setName", String.class).invoke(u, "User-" + id);
        } catch (Exception ignored) {}
        return u;
    }

    private PetResponse pet(Long id) {
        PetResponse p = new PetResponse();
        p.setId(id);
        p.setName("Pet-" + id);
        // Evitamos depender de enums concretos
        p.setBreed(null);
        p.setLifeStage(null);
        p.setHunger(50);
        p.setHygiene(60);
        p.setFun(70);
        p.setActionCount(0);
        p.setOwnerId(1L);
        p.setCreatedAt(Instant.now());
        p.setDead(false);
        p.setDeathAt(null);
        return p;
    }
}
