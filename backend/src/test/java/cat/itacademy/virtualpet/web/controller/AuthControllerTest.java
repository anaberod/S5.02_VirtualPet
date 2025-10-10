package cat.itacademy.virtualpet.web.controller;

import cat.itacademy.virtualpet.BackendApplication;
import cat.itacademy.virtualpet.domain.user.User;
import cat.itacademy.virtualpet.domain.user.UserRepository;
import cat.itacademy.virtualpet.support.TestcontainersMySQL;
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

@SpringBootTest(classes = BackendApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest extends TestcontainersMySQL {

    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired PasswordEncoder encoder;

    @BeforeEach
    void cleanDB() { users.deleteAll(); }

    @Test
    @DisplayName("POST /auth/register → 2xx crea usuario nuevo (persistencia en BD)")
    void register_createsUser_persisted() throws Exception {
        final String email = "anabel@example.com";
        final String username = "anabel";

        String payload = """
            {
              "username": "anabel",
              "email": "anabel@example.com",
              "password": "StrongerPass123!"
            }
        """;

        MvcResult res = mvc.perform(
                post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload)
        ).andReturn();

        int status = res.getResponse().getStatus();
        assertThat(status).as("status debe ser 2xx").isBetween(200, 299);

        Optional<User> u = users.findByEmail(email);
        assertThat(u).as("el usuario debe quedar persistido").isPresent();
        assertThat(u.get().getUsername()).isEqualTo(username);
    }

    @Test
    @DisplayName("POST /auth/register → 409 si email/username ya existen")
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

        MvcResult res = mvc.perform(
                post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        ).andReturn();

        assertThat(res.getResponse().getStatus()).isEqualTo(409);
    }

    @Test
    @DisplayName("POST /auth/login → 2xx con credenciales correctas (email + password)")
    void login_returnsSuccess() throws Exception {
        // Inserta usuario con la misma estrategia de hash que usa la app
        User user = new User();
        user.setUsername("anabel");
        user.setEmail("anabel@example.com");
        user.setPasswordHash(encoder.encode("StrongPass123")); // <- importante
        users.save(user);

        String json = """
            {
              "email": "anabel@example.com",
              "password": "StrongPass123"
            }
        """;

        MvcResult res = mvc.perform(
                post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
        ).andReturn();

        assertThat(res.getResponse().getStatus())
                .as("login correcto debe responder 2xx")
                .isBetween(200, 299);
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
