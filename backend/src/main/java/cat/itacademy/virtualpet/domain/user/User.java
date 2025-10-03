package cat.itacademy.virtualpet.domain.user; // Paquete del dominio "user"

/* ========================= IMPORTS ========================= */
import com.fasterxml.jackson.annotation.JsonIgnore;              // Evitar exponer passwordHash si alguna vez se serializa la entidad
import jakarta.persistence.*;                                   // JPA: @Entity, @Id, @Table, @Column, etc.
import lombok.*;                                                // Lombok: getters/setters, builder, etc.
import org.hibernate.annotations.CreationTimestamp;            // Fecha de creación automática

import java.time.Instant;                                       // Timestamps en UTC
import java.util.HashSet;                                       // Implementación para Set
import java.util.Set;                                           // Interfaz Set

/* ========================= ENTIDAD ========================= */
/**
 * Entidad JPA que representa a un usuario de la aplicación.
 * - Unicidad en username y email a nivel de BD.
 * - Contraseña almacenada como hash (BCrypt).
 * - Roles en tabla secundaria (user_roles) como colección de Strings.
 */
@Entity
@Table(
        name = "users", // Evitamos "user" por ser palabra reservada en algunos SGBD
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_username", columnNames = "username"),
                @UniqueConstraint(name = "uk_users_email", columnNames = "email")
        }
)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "passwordHash") // Nunca incluimos el hash en logs por seguridad
@EqualsAndHashCode(onlyExplicitlyIncluded = true) // equals/hashCode solo con los campos anotados
public class User {

    /* ------------------- Identificador ------------------- */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT en MySQL/MariaDB
    @EqualsAndHashCode.Include                          // Identidad por id
    private Long id;

    /* ------------------- Datos de login ------------------- */
    @Column(nullable = false, length = 50)              // NOT NULL, máx 50
    private String username;                            // Debe ser único (constraint de la tabla)

    @Column(nullable = false, length = 120)             // NOT NULL, máx 120
    private String email;                               // Debe ser único (constraint de la tabla)

    @JsonIgnore                                         // Por si alguien serializa la entidad: NO exponer hashes
    @Column(name = "password_hash", nullable = false, length = 120)
    // BCrypt genera ~60 chars; dejamos 120 por si se cambia de algoritmo en el futuro
    private String passwordHash;

    /* ------------------- Roles ------------------- */
    @ElementCollection(fetch = FetchType.EAGER)         // Cargamos los roles junto al usuario (simple y seguro para auth)
    @CollectionTable(
            name = "user_roles",
            joinColumns = @JoinColumn(
                    name = "user_id",
                    foreignKey = @ForeignKey(name = "fk_user_roles_user")
            )
    )
    @Column(name = "role", nullable = false, length = 32) // Ej.: ROLE_USER, ROLE_ADMIN
    @Builder.Default
    private Set<String> roles = new HashSet<>();

    /* ------------------- Metadatos ------------------- */
    @CreationTimestamp                                  // Se autocompleta al insertar
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /* ------------------- Ayudas de dominio ------------------- */

    /** Añade un rol si no existía (no duplica al ser Set). */
    public void addRole(String role) {
        if (roles == null) roles = new HashSet<>();
        roles.add(role);
    }

    /** Normaliza datos antes de insertar (defensa contra espacios y mayúsculas en email). */
    @PrePersist
    @PreUpdate
    private void normalizeFields() {
        if (username != null) {
            username = username.trim();
        }
        if (email != null) {
            email = email.trim().toLowerCase(); // emails en minúsculas para evitar “duplicados” lógicos
        }
    }
}
