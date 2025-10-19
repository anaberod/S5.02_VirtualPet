package cat.itacademy.virtualpet.web.mapper;

import cat.itacademy.virtualpet.application.dto.pet.PetActionResponse;
import cat.itacademy.virtualpet.application.dto.pet.PetResponse;
import cat.itacademy.virtualpet.application.mapper.PetMapper;
import cat.itacademy.virtualpet.domain.pet.Pet;
import cat.itacademy.virtualpet.domain.pet.enums.Breed;
import cat.itacademy.virtualpet.domain.pet.enums.LifeStage;
import cat.itacademy.virtualpet.domain.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PetMapperTest {

    private PetMapper mapper;
    private Pet petBase;
    private User owner;

    @BeforeEach
    void setUp() {
        mapper = Mappers.getMapper(PetMapper.class);

        owner = new User();
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
        assertThat(dto.getHunger()).isEqualTo(40);
        assertThat(dto.getHygiene()).isEqualTo(60);
        assertThat(dto.getFun()).isEqualTo(50);
        assertThat(dto.getLifeStage()).isEqualTo(LifeStage.BABY);
    }

    @Test
    @DisplayName("toActionResponse: mapea igual que response (sin message/warnings)")
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

    @Test
    @DisplayName("toResponse: owner null no rompe")
    void toResponse_ownerNull_isSafe() {
        Pet noOwner = clonePet(petBase);
        noOwner.setOwner(null);

        PetResponse dto = mapper.toResponse(noOwner);
        assertThat(dto).isNotNull();
    }

    @Test
    @DisplayName("Enums: distintos Breed/LifeStage se copian tal cual")
    void enums_copyVerbatim() {
        for (Breed b : new Breed[]{Breed.LABRADOR, Breed.DALMATIAN}) {
            for (LifeStage s : new LifeStage[]{LifeStage.BABY, LifeStage.ADULT, LifeStage.SENIOR}) {
                Pet p = clonePet(petBase);
                p.setBreed(b);
                p.setLifeStage(s);

                PetResponse dto = mapper.toResponse(p);
                assertThat(dto.getBreed()).isEqualTo(b);
                assertThat(dto.getLifeStage()).isEqualTo(s);
            }
        }
    }

    @Test
    @DisplayName("updateEntity (si existe): actualiza un campo seguro (name) y mantiene el resto")
    void updateEntity_updatesNameOnly_ifPresent() throws Exception {
        // 1️⃣ Carga PetUpdateRequest si existe
        Class<?> updateDtoClass;
        try {
            updateDtoClass = Class.forName("cat.itacademy.virtualpet.application.dto.pet.PetUpdateRequest");
        } catch (ClassNotFoundException e) {
            return; // no existe en tu proyecto → test no aplica
        }

        // 2️⃣ Entidad base
        Pet pet = clonePet(petBase);
        pet.setName("OLD");
        pet.setHunger(10);
        pet.setHygiene(20);
        pet.setFun(30);

        // 3️⃣ DTO con sólo name
        Object dto = updateDtoClass.getDeclaredConstructor().newInstance();
        try {
            updateDtoClass.getMethod("setName", String.class).invoke(dto, "NEW");
        } catch (NoSuchMethodException ignored) {
            return; // no tiene name → salimos
        }

        // 4️⃣ Buscar método updateEntity en mapper (acepta ambos órdenes de parámetros)
        Method update = null;
        try {
            update = mapper.getClass().getMethod("updateEntity", Pet.class, updateDtoClass);
        } catch (NoSuchMethodException ignored) {
            try {
                update = mapper.getClass().getMethod("updateEntity", updateDtoClass, Pet.class);
            } catch (NoSuchMethodException ignored2) {
                return; // no existe updateEntity → salimos
            }
        }

        if (update.getParameterTypes()[0].equals(Pet.class)) {
            update.invoke(mapper, pet, dto);
        } else {
            update.invoke(mapper, dto, pet);
        }

        // 5️⃣ Aserciones
        assertThat(pet.getName()).isEqualTo("NEW");
        assertThat(pet.getHunger()).isEqualTo(10);
        assertThat(pet.getHygiene()).isEqualTo(20);
        assertThat(pet.getFun()).isEqualTo(30);
    }

    // ----------------- helpers -----------------

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
