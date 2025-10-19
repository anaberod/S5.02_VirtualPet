package cat.itacademy.virtualpet;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class BackendApplicationTest {

    @Test
    @DisplayName("✅ Spring context loads successfully")
    void contextLoads() {

    }
}
