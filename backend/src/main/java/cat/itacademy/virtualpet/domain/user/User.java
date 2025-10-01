package cat.itacademy.virtualpet.domain.user; // Paquete donde vive la entidad User (organiza el código por dominio)

// -------------------- IMPORTS --------------------
import jakarta.persistence.*;                 // Anotaciones JPA: @Entity, @Id, @Table, @Column, etc.
import lombok.*;                              // Anotaciones Lombok para generar getters/setters/constructores/builder/toString
import org.hibernate.annotations.CreationTimestamp; // Marca un campo fecha para autocompletarse en la inserción

import java.time.Instant;                     // Tipo de fecha/hora en UTC
import java.util.HashSet;                     // Implementación concreta para Set
import java.util.Set;                         // Interfaz Set (conjunto sin duplicados)

// -------------------- ENTIDAD JPA --------------------
@Entity                                       // Indica que esta clase es una entidad JPA (se mapea a una tabla)
@Table(                                        // Configura detalles de la tabla en BD
        name = "users",                       // Nombre de la tabla (evitamos "user" por ser palabra reservada en algunos SGBD)
        uniqueConstraints = {                 // Restricciones de unicidad a nivel BD (evitan duplicados reales)
                @UniqueConstraint(
                        name = "uk_users_username",
                        columnNames = "username" // username debe ser único
                ),
                @UniqueConstraint(
                        name = "uk_users_email",
                        columnNames = "email"    // email debe ser único
                )
        }
)
@Getter @Setter                               // Lombok: genera getters y setters para todos los campos
@NoArgsConstructor                             // Lombok: constructor vacío (requisito de JPA)
@AllArgsConstructor                             // Lombok: constructor con todos los campos
@Builder                                       // Lombok: patrón builder para crear instancias de forma fluida
@ToString(exclude = "passwordHash")            // Lombok: toString sin mostrar el hash de la contraseña (no filtrar secretos)
public class User {                            // Declaración de la entidad

    @Id                                        // Clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Generación auto-incremental (IDENTITY en MySQL)
    private Long id;                            // Identificador único del usuario

    @Column(nullable = false, length = 50)      // Columna NOT NULL, longitud máxima 50
    private String username;                    // Nombre de usuario (login)

    @Column(nullable = false, length = 120)     // ⬅️ Email OBLIGATORIO (NOT NULL), longitud máxima 120
    private String email;                       // Email del usuario (único por constraint de la tabla)

    @Column(name = "password_hash", nullable = false, length = 120) // Columna NOT NULL con nombre explícito
    private String passwordHash;                // Hash de contraseña (BCrypt); nunca guardes texto plano

    @ElementCollection(fetch = FetchType.EAGER) // Colección de valores simples (no otra entidad); se carga siempre con el usuario
    @CollectionTable(
            name = "user_roles",               // Tabla intermedia donde se guardan los roles
            joinColumns = @JoinColumn(
                    name = "user_id",
                    foreignKey = @ForeignKey(name = "fk_user_roles_user") // Nombre explícito para la FK
            )
    )
    @Column(name = "role", nullable = false, length = 32) // Columna con el nombre del rol
    @Builder.Default                               // Si construyes con builder y no pones roles, será un Set vacío por defecto
    private Set<String> roles = new HashSet<>();   // Conjunto de roles (p. ej. ROLE_USER, ROLE_ADMIN)

    @CreationTimestamp                             // Hibernate asigna automáticamente la fecha/hora al crear el registro
    @Column(name = "created_at", nullable = false, updatable = false) // No se puede actualizar después de creado
    private Instant createdAt;                     // Momento de creación en UTC

    public void addRole(String role) {             // Método de ayuda para añadir roles de forma segura
        if (roles == null) roles = new HashSet<>();// Defensa ante posibles nulls (raro, pero seguro)
        roles.add(role);                           // Inserta el rol (no se duplica porque Set evita duplicados)
    }
}
