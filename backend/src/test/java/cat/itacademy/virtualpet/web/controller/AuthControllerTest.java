package cat.itacademy.virtualpet.web.controller;

import cat.itacademy.virtualpet.BackendApplication;
import cat.itacademy.virtualpet.domain.user.User;
import cat.itacademy.virtualpet.domain.user.UserRepository;
import cat.itacademy.virtualpet.domain.pet.PetRepository;          // 👈
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = BackendApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired PetRepository pets;                 // 👈
    @Autowired PasswordEncoder encoder;

    @BeforeEach
    void cleanDB() {
        // Importante: primero hijos, luego padres (evita violación FK)
        pets.deleteAll();
        users.deleteAll();
    }

    @Test
    @DisplayName("POST /auth/register → 2xx, devuelve token y persiste usuario con contraseña cifrada")
    void register_createsUser_andReturnsToken_andStoresBCrypt() throws Exception {
        final String email = "anabel@example.com";
        final String username = "anabel";
        final String rawPass = "StrongerPass123!";

        String payload = """
            {
              "username": "anabel",
              "email": "anabel@example.com",
              "password": "StrongerPass123!"
            }
        """;

        mvc.perform(
                        post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(payload)
                )
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").isNotEmpty());

        Optional<User> u = users.findByEmail(email);
        assertThat(u).isPresent();
        assertThat(u.get().getUsername()).isEqualTo(username);
        assertThat(u.get().getPasswordHash()).isNotBlank();
        assertThat(u.get().getPasswordHash()).isNotEqualTo(rawPass);
        assertThat(encoder.matches(rawPass, u.get().getPasswordHash())).isTrue();
    }

    @Test
    @DisplayName("POST /auth/register → 409 si email/username ya existen (y body de error estándar)")
    void register_conflictIfExists() throws Exception {
        User existing = new User();
        existing.setUsername("ya_existe");
        existing.setEmail("ya_existe@example.com");
        existing.setPasswordHash("$2a$10$dummy");
        users.save(existing);

        String json = """
            {
              "username": "ya_existe",
              "email": "ya_existe@example.com",
              "password": "AnotherPass123"
            }
        """;

        mvc.perform(
                        post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("POST /auth/login → 2xx con credenciales correctas y token en respuesta")
    void login_returnsSuccessWithToken() throws Exception {
        User user = new User();
        user.setUsername("anabel");
        user.setEmail("anabel@example.com");
        user.setPasswordHash(encoder.encode("StrongPass123"));
        users.save(user);

        String json = """
            {
              "email": "anabel@example.com",
              "password": "StrongPass123"
            }
        """;

        mvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json)
                )
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("POST /auth/login → 400/401 con credenciales inválidas")
    void login_invalidCredentials() throws Exception {
        String json = """
            {
              "email": "nope@example.com",
              "password": "wrong"
            }
        """;

        MvcResult res = mvc.perform(
                post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        ).andReturn();

        int status = res.getResponse().getStatus();
        assertThat(status).isIn(400, 401);
    }
}
