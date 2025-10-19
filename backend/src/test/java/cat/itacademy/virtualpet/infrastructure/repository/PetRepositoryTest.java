package cat.itacademy.virtualpet.infrastructure.repository;

import cat.itacademy.virtualpet.domain.pet.PetRepository;
import cat.itacademy.virtualpet.domain.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;


import cat.itacademy.virtualpet.domain.user.User;
import cat.itacademy.virtualpet.domain.pet.Pet;
import cat.itacademy.virtualpet.domain.pet.enums.Breed;
import cat.itacademy.virtualpet.domain.pet.enums.LifeStage;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PetRepositoryTest {

    @Autowired
    UserRepository userRepository;
    @Autowired
    PetRepository petRepository;

    @Test
    @DisplayName("Guardar y leer Pet con su owner")
    void saveAndRead() {
        // --- owner ---
        User owner = new User();
        owner.setEmail("owner@example.com");
        owner.setUsername("owner");
        owner.setPasswordHash("$2a$10$dummy");
        owner.setRoles(Set.of("ROLE_USER"));
        owner = userRepository.save(owner);

        // --- pet ---
        Pet pet = new Pet();
        pet.setName("Kira");
        pet.setBreed(Breed.DALMATIAN);
        pet.setHunger(20);
        pet.setHygiene(80);
        pet.setFun(60);
        pet.setActionCount(3);            // <- en tu entidad es actionCount
        pet.setLifeStage(LifeStage.ADULT);
        pet.setOwner(owner);

        Pet saved = petRepository.save(pet);
        Long id = saved.getId();

        Pet found = petRepository.findById(id).orElseThrow();

        assertThat(found.getId()).isNotNull();
        assertThat(found.getOwner().getId()).isEqualTo(owner.getId());
        assertThat(found.getBreed()).isEqualTo(Breed.DALMATIAN);
        assertThat(found.getActionCount()).isEqualTo(3);
        assertThat(found.getLifeStage()).isEqualTo(LifeStage.ADULT);
    }
}
