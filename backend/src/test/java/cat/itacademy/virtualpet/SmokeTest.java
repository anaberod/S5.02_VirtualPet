package cat.itacademy.virtualpet;

import cat.itacademy.virtualpet.support.TestcontainersMySQL;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/** Carga el contexto completo con el perfil de test. */
@SpringBootTest(
        classes = BackendApplication.class,              // <-- tu clase @SpringBootApplication
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")                              // <-- usa application-test.yml
class SmokeTest extends TestcontainersMySQL {

    @Test
    void contextLoads() {
        assertThat(true).isTrue();
    }
}
