package cat.itacademy.virtualpet.domain.user; // Repositorio del dominio "user"

import org.springframework.data.jpa.repository.JpaRepository; // CRUD, paginación, etc. generado por Spring Data
import org.springframework.stereotype.Repository;             // Estereotipo (opcional en interfaces de Spring Data)
import java.util.Optional;                                   // Contenedor seguro para valores ausentes/presentes

/**
 * Repositorio de acceso a datos para la entidad User.
 * Spring Data JPA genera la implementación automáticamente en runtime.
 * Clave primaria de User = Long.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Buscar por username (útil para validación de registro / pantallas admin)
    Optional<User> findByUsername(String username);

    // NUEVO: Buscar por email (login será solo por email)
    Optional<User> findByEmail(String email);

    // ¿Existe ya este username? (para devolver 409 en registro)
    boolean existsByUsername(String username);

    // ¿Existe ya este email? (para devolver 409 en registro)
    boolean existsByEmail(String email);
}
