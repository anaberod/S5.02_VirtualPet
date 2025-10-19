package cat.itacademy.virtualpet.domain.pet;

import cat.itacademy.virtualpet.domain.pet.enums.Breed;
import cat.itacademy.virtualpet.domain.pet.enums.LifeStage;
import cat.itacademy.virtualpet.domain.user.User;
import jakarta.persistence.*;
import java.time.Instant;


@Entity
@Table(name = "pets")
public class Pet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Breed breed;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LifeStage lifeStage = LifeStage.BABY;

    @Column(nullable = false)
    private int hunger = 50;

    @Column(nullable = false)
    private int hygiene = 70;

    @Column(nullable = false)
    private int fun = 60;

    @Column(name = "action_count", nullable = false)
    private int actionCount = 0;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();


    @Column(nullable = false)
    private boolean dead = false;

    @Column(name = "death_at")
    private Instant deathAt;

    // Many pets belong to one user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;



    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Breed getBreed() { return breed; }
    public void setBreed(Breed breed) { this.breed = breed; }

    public LifeStage getLifeStage() { return lifeStage; }
    public void setLifeStage(LifeStage lifeStage) { this.lifeStage = lifeStage; }

    public int getHunger() { return hunger; }
    public void setHunger(int hunger) { this.hunger = hunger; }

    public int getHygiene() { return hygiene; }
    public void setHygiene(int hygiene) { this.hygiene = hygiene; }

    public int getFun() { return fun; }
    public void setFun(int fun) { this.fun = fun; }

    public int getActionCount() { return actionCount; }
    public void setActionCount(int actionCount) { this.actionCount = actionCount; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public boolean isDead() { return dead; }
    public void setDead(boolean dead) { this.dead = dead; }

    public Instant getDeathAt() { return deathAt; }
    public void setDeathAt(Instant deathAt) { this.deathAt = deathAt; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }
}
