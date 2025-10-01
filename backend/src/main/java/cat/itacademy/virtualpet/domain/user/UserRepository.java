package cat.itacademy.virtualpet.domain.user; // Ubica el repositorio dentro del dominio "user"

/* ----------- IMPORTS ----------- */
import org.springframework.data.jpa.repository.JpaRepository; // Interfaz base de Spring Data JPA que aporta CRUD, paginación y más
import org.springframework.stereotype.Repository;             // Anotación para registrar este repositorio como bean en el contexto de Spring
import java.util.Optional;                                  // Contenedor para valores que pueden o no estar presentes (evita null)

/**
 * Repositorio de acceso a datos para la entidad User.
 *
 * - NO se implementa a mano: Spring Data JPA genera en runtime la implementación
 *   a partir de la firma de los métodos y del nombre de la entidad/campos.
 * - El segundo parámetro de JpaRepository es el tipo de la clave primaria (Long en User).
 */
@Repository // Marca la interfaz para que Spring la detecte en el escaneo de componentes (estereotipo de persistencia)
public interface UserRepository extends JpaRepository<User, Long> { // Hereda todos los métodos CRUD de JpaRepository<User, Long>

    // Deriva automáticamente una consulta "SELECT u FROM User u WHERE u.username = :username"
    // Devuelve Optional porque puede no existir ningún usuario con ese username.
    Optional<User> findByUsername(String username);

    // Devuelve true si existe alguna fila en "users" con ese username (consulta optimizada de existencia).
    boolean existsByUsername(String username);

    // Devuelve true si existe alguna fila en "users" con ese email (recuerda que en tu modelo es NOT NULL y único).
    boolean existsByEmail(String email);
}
