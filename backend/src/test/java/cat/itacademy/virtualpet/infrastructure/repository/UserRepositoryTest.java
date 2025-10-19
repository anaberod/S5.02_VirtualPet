package cat.itacademy.virtualpet.infrastructure.repository;

import cat.itacademy.virtualpet.domain.user.User;
import cat.itacademy.virtualpet.domain.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // usa el MySQL del testcontainer
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Guardar y buscar por email")
    void save_and_findByEmail() {
        User u = new User();
        u.setUsername("ana");
        u.setEmail("ana@example.com");

        try { u.getClass().getMethod("setPassword", String.class).invoke(u, "{noop}secret"); }
        catch (Exception ignore) { try { u.getClass().getMethod("setPasswordHash", String.class).invoke(u, "{noop}secret"); } catch (Exception e) { /* nada */ } }

        try { u.getClass().getMethod("setRoles", Set.class).invoke(u, Set.of("ROLE_USER")); } catch (Exception ignore) {}

        userRepository.save(u);

        assertThat(userRepository.findByEmail("ana@example.com"))
                .isPresent()
                .get()
                .extracting(User::getUsername)
                .isEqualTo("ana");
    }

    @Test
    @DisplayName("existsByUsername / existsByEmail")
    void exists_checks() {
        User u = new User();
        u.setUsername("pepe");
        u.setEmail("pepe@example.com");
        try { u.getClass().getMethod("setPassword", String.class).invoke(u, "{noop}x"); }
        catch (Exception ignore) { try { u.getClass().getMethod("setPasswordHash", String.class).invoke(u, "{noop}x"); } catch (Exception e) { /* nada */ } }
        userRepository.save(u);

        assertThat(userRepository.existsByUsername("pepe")).isTrue();
        assertThat(userRepository.existsByEmail("pepe@example.com")).isTrue();
        assertThat(userRepository.existsByUsername("otro")).isFalse();
        assertThat(userRepository.existsByEmail("otro@example.com")).isFalse();
    }
}
