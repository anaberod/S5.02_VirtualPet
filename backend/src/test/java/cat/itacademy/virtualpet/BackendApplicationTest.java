package cat.itacademy.virtualpet;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test") // 👈 usa application-test.yml (tiene jwt.secret y demás)
class BackendApplicationTest {

    @Test
    @DisplayName("✅ Spring context loads successfully")
    void contextLoads() {
        // Si el contexto arranca sin excepciones, el test pasa.
    }
}
