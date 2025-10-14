package cat.itacademy.virtualpet.web.mapper;


import cat.itacademy.virtualpet.application.dto.pet.PetActionResponse;
import cat.itacademy.virtualpet.application.dto.pet.PetResponse;
import cat.itacademy.virtualpet.application.mapper.PetMapper;
import cat.itacademy.virtualpet.domain.pet.enums.Breed;
import cat.itacademy.virtualpet.domain.pet.enums.LifeStage;
import cat.itacademy.virtualpet.domain.pet.Pet;
import cat.itacademy.virtualpet.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PetMapperTest {

    private PetMapper mapper;

    private Pet petBase;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(PetMapper.class);

        User owner = new User();
        owner.setId(100L);
        owner.setEmail("user@example.com");
        owner.setUsername("user");
        owner.setRoles(new HashSet<>(Set.of("ROLE_USER")));

        petBase = new Pet();
        petBase.setId(1L);
        petBase.setName("Toby");
        petBase.setBreed(Breed.LABRADOR);
        petBase.setOwner(owner);
        petBase.setHunger(40);
        petBase.setHygiene(60);
        petBase.setFun(50);
        petBase.setActionCount(3);
        petBase.setLifeStage(LifeStage.BABY);
        petBase.setDead(false);
        petBase.setDeathAt(null);
        petBase.setCreatedAt(Instant.now());
    }

    @Test
    @DisplayName("toResponse: mapea campos básicos y estado correctamente")
    void toResponse_mapsBasicFields() {
        PetResponse dto = mapper.toResponse(petBase);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(petBase.getId());
        assertThat(dto.getName()).isEqualTo("Toby");
        assertThat(dto.getBreed()).isEqualTo(Breed.LABRADOR);

        // Stats
        assertThat(dto.getHunger()).isEqualTo(40);
        assertThat(dto.getHygiene()).isEqualTo(60);
        assertThat(dto.getFun()).isEqualTo(50);

        // Life stage
        assertThat(dto.getLifeStage()).isEqualTo(LifeStage.BABY);
    }

    @Test
    @DisplayName("toActionResponse: mapea igual que response (message/warnings se añaden fuera del mapper)")
    void toActionResponse_mapsStatsAndStage() {
        PetActionResponse dto = mapper.toActionResponse(petBase);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Toby");
        assertThat(dto.getBreed()).isEqualTo(Breed.LABRADOR);

        assertThat(dto.getHunger()).isEqualTo(40);
        assertThat(dto.getHygiene()).isEqualTo(60);
        assertThat(dto.getFun()).isEqualTo(50);
        assertThat(dto.getLifeStage()).isEqualTo(LifeStage.BABY);

        // El mensaje y los warnings los rellena el servicio (buildResponseWithWarnings),
        // el mapper no se encarga de eso:
        // assertThat(dto.getMessage()).isNull();  // descomenta si el campo existe y por defecto es null
        // assertThat(dto.getWarnings()).isNull(); // idem
    }

    @Test
    @DisplayName("toResponse: soporta PASSED y deathAt no nulo")
    void toResponse_mapsPassedAndDeathAt() {
        Pet dead = clonePet(petBase);
        dead.setLifeStage(LifeStage.PASSED);
        dead.setDead(true);
        dead.setDeathAt(Instant.now());

        PetResponse dto = mapper.toResponse(dead);

        assertThat(dto.getLifeStage()).isEqualTo(LifeStage.PASSED);

    }

    @Test
    @DisplayName("toResponse/toActionResponse: toleran valores límite y nulls no críticos")
    void mapping_handlesEdgeValues() {
        Pet edge = clonePet(petBase);
        edge.setHunger(0);
        edge.setHygiene(100);
        edge.setFun(0);

        PetResponse r1 = mapper.toResponse(edge);
        PetActionResponse r2 = mapper.toActionResponse(edge);

        assertThat(r1.getHunger()).isZero();
        assertThat(r1.getHygiene()).isEqualTo(100);
        assertThat(r1.getFun()).isZero();

        assertThat(r2.getHunger()).isZero();
        assertThat(r2.getHygiene()).isEqualTo(100);
        assertThat(r2.getFun()).isZero();
    }

    private Pet clonePet(Pet p) {
        Pet x = new Pet();
        x.setId(p.getId());
        x.setName(p.getName());
        x.setBreed(p.getBreed());
        x.setOwner(p.getOwner());
        x.setHunger(p.getHunger());
        x.setHygiene(p.getHygiene());
        x.setFun(p.getFun());
        x.setActionCount(p.getActionCount());
        x.setLifeStage(p.getLifeStage());
        x.setDead(p.isDead());
        x.setDeathAt(p.getDeathAt());
        x.setCreatedAt(p.getCreatedAt());
        return x;
    }
}
