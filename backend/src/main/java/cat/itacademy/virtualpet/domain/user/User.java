package cat.itacademy.virtualpet.domain.user;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;



@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_username", columnNames = "username"),
                @UniqueConstraint(name = "uk_users_email", columnNames = "email")
        }
)
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "passwordHash")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;


    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 120)
    private String email;

    @JsonIgnore
    @Column(name = "password_hash", nullable = false, length = 120)

    private String passwordHash;


    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "user_roles",
            joinColumns = @JoinColumn(
                    name = "user_id",
                    foreignKey = @ForeignKey(name = "fk_user_roles_user")
            )
    )
    @Column(name = "role", nullable = false, length = 32)
    @Builder.Default
    private Set<String> roles = new HashSet<>();


    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;




    public void addRole(String role) {
        if (roles == null) roles = new HashSet<>();
        roles.add(role);
    }


    @PrePersist
    @PreUpdate
    private void normalizeFields() {
        if (username != null) {
            username = username.trim();
        }
        if (email != null) {
            email = email.trim().toLowerCase();
        }
    }
}
