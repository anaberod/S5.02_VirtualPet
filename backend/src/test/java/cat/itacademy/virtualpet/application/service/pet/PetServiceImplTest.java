package cat.itacademy.virtualpet.application.service.pet;

import cat.itacademy.virtualpet.application.dto.pet.PetActionResponse;
import cat.itacademy.virtualpet.application.dto.pet.PetResponse;
import cat.itacademy.virtualpet.domain.pet.enums.Breed;
import cat.itacademy.virtualpet.domain.pet.enums.LifeStage;
import cat.itacademy.virtualpet.domain.pet.Pet;
import cat.itacademy.virtualpet.domain.user.User;
import cat.itacademy.virtualpet.web.error.PetAlreadyCleanException;
import cat.itacademy.virtualpet.web.error.PetDeceasedException;
import cat.itacademy.virtualpet.web.error.PetNotHungryException;
import cat.itacademy.virtualpet.web.error.PetTooHappyException;
import cat.itacademy.virtualpet.domain.pet.PetRepository;
import cat.itacademy.virtualpet.domain.user.UserRepository;
import cat.itacademy.virtualpet.application.mapper.PetMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.data.domain.*;

import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PetServiceImplTest {

    @Mock private PetRepository petRepository;
    @Mock private UserRepository userRepository;
    @Mock private PetMapper petMapper;

    @InjectMocks private PetServiceImpl petService;

    private static final String OWNER_EMAIL = "user@example.com";
    private static final String ADMIN_EMAIL = "admin@example.com";

    private User owner;
    private User admin;
    private Pet pet;

    @BeforeEach
    void setUp() {
        // Owner
        owner = new User();
        owner.setId(100L);
        owner.setEmail(OWNER_EMAIL);
        owner.setUsername("user");
        owner.setRoles(new HashSet<>(Set.of("ROLE_USER")));

        // Admin
        admin = new User();
        admin.setId(1L);
        admin.setEmail(ADMIN_EMAIL);
        admin.setUsername("admin");
        admin.setRoles(new HashSet<>(Set.of("ROLE_ADMIN")));

        // Pet base
        pet = new Pet();
        pet.setId(1L);
        pet.setName("Toby");
        pet.setBreed(Breed.LABRADOR);
        pet.setOwner(owner);
        pet.setHunger(40);
        pet.setHygiene(60);
        pet.setFun(50);
        pet.setActionCount(0);
        pet.setLifeStage(LifeStage.BABY);
        pet.setDead(false);
        pet.setDeathAt(null);

        // Stubs comunes
        when(userRepository.findByEmail(OWNER_EMAIL)).thenReturn(Optional.of(owner));
        when(userRepository.findByEmail(ADMIN_EMAIL)).thenReturn(Optional.of(admin));
        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));
        when(petRepository.save(any(Pet.class))).thenAnswer(inv -> inv.getArgument(0));

        // Mapper lenient (evita stubbing innecesario si algún test no lo usa)
        lenient().when(petMapper.toActionResponse(any(Pet.class))).thenAnswer(inv -> {
            Pet p = inv.getArgument(0);
            PetActionResponse r = new PetActionResponse();
            r.setId(p.getId());
            r.setName(p.getName());
            r.setBreed(p.getBreed());
            r.setHunger(p.getHunger());
            r.setHygiene(p.getHygiene());
            r.setFun(p.getFun());
            r.setLifeStage(p.getLifeStage());
            return r;
        });
        lenient().when(petMapper.toResponse(any(Pet.class))).thenAnswer(inv -> {
            Pet p = inv.getArgument(0);
            PetResponse r = new PetResponse();
            r.setId(p.getId());
            r.setName(p.getName());
            r.setBreed(p.getBreed());
            r.setHunger(p.getHunger());
            r.setHygiene(p.getHygiene());
            r.setFun(p.getFun());
            r.setLifeStage(p.getLifeStage());
            return r;
        });
    }



    @Test
    @DisplayName("feed: hunger -50 (min 0), hygiene -5, +1 acción")
    void feed_happyPath() {
        PetActionResponse res = petService.feed(1L, OWNER_EMAIL);

        assertThat(res.getHunger()).isZero();
        assertThat(res.getHygiene()).isEqualTo(55);
        assertThat(res.getLifeStage()).isEqualTo(LifeStage.BABY);

        Pet saved = captureLastSavedPet();
        assertThat(saved.getHunger()).isZero();
        assertThat(saved.getHygiene()).isEqualTo(55);
        assertThat(saved.getActionCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("feed: lanza PetNotHungryException si hunger = 0")
    void feed_throwsWhenNotHungry() {
        pet.setHunger(0);
        assertThatThrownBy(() -> petService.feed(1L, OWNER_EMAIL))
                .isInstanceOf(PetNotHungryException.class);
        verify(petRepository, never()).save(any());
    }



    @Test
    @DisplayName("wash: hygiene +30 (max 100), hunger +10 (según impl), +1 acción")
    void wash_happyPath() {
        pet.setHygiene(75);

        PetActionResponse res = petService.wash(1L, OWNER_EMAIL);

        assertThat(res.getHygiene()).isEqualTo(100);
        assertThat(res.getHunger()).isEqualTo(50); // 40 + 10 (tu impl)
        Pet saved = captureLastSavedPet();
        assertThat(saved.getHygiene()).isEqualTo(100);
        assertThat(saved.getHunger()).isEqualTo(50);
        assertThat(saved.getActionCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("wash: lanza PetAlreadyCleanException si hygiene = 100")
    void wash_throwsWhenAlreadyClean() {
        pet.setHygiene(100);
        assertThatThrownBy(() -> petService.wash(1L, OWNER_EMAIL))
                .isInstanceOf(PetAlreadyCleanException.class);
        verify(petRepository, never()).save(any());
    }



    @Test
    @DisplayName("play: fun +40 (max 100), hunger +15 (según impl), +1 acción")
    void play_happyPath() {
        PetActionResponse res = petService.play(1L, OWNER_EMAIL);

        assertThat(res.getFun()).isEqualTo(90);
        assertThat(res.getHunger()).isEqualTo(55); // 40 + 15 (tu impl)
        Pet saved = captureLastSavedPet();
        assertThat(saved.getFun()).isEqualTo(90);
        assertThat(saved.getHunger()).isEqualTo(55);
        assertThat(saved.getActionCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("play: lanza PetTooHappyException si fun = 100")
    void play_throwsWhenTooHappy() {
        pet.setFun(100);
        assertThatThrownBy(() -> petService.play(1L, OWNER_EMAIL))
                .isInstanceOf(PetTooHappyException.class);
        verify(petRepository, never()).save(any());
    }



    @Test
    @DisplayName("muere por hambre: al jugar con hunger ≥ 85 sube a 100 y pasa a PASSED")
    void death_byHungerAtPlay() {
        pet.setHunger(90);

        PetActionResponse res = petService.play(1L, OWNER_EMAIL);

        assertThat(res.getLifeStage()).isEqualTo(LifeStage.PASSED);
        Pet saved = captureLastSavedPet();
        assertThat(saved.isDead()).isTrue();
        assertThat(saved.getHunger()).isEqualTo(100);
        assertThat(saved.getDeathAt()).isNotNull();
    }

    @Test
    @DisplayName("muere por higiene=0 y fun=0 en una acción")
    void death_byZeroHygieneAndFun() {
        pet.setHygiene(0);
        pet.setFun(0);

        PetActionResponse res = petService.feed(1L, OWNER_EMAIL);

        assertThat(res.getLifeStage()).isEqualTo(LifeStage.PASSED);
        Pet saved = captureLastSavedPet();
        assertThat(saved.isDead()).isTrue();
        assertThat(saved.getDeathAt()).isNotNull();
    }

    @Test
    @DisplayName("si ya está PASSED, cualquier acción lanza PetDeceasedException")
    void actions_throwWhenAlreadyPassed() {
        pet.setLifeStage(LifeStage.PASSED);
        pet.setDead(true);
        pet.setDeathAt(Instant.now());

        assertThatThrownBy(() -> petService.feed(1L, OWNER_EMAIL))
                .isInstanceOf(PetDeceasedException.class);
        assertThatThrownBy(() -> petService.wash(1L, OWNER_EMAIL))
                .isInstanceOf(PetDeceasedException.class);
        assertThatThrownBy(() -> petService.play(1L, OWNER_EMAIL))
                .isInstanceOf(PetDeceasedException.class);
        verify(petRepository, never()).save(any());
    }



    @Test
    @DisplayName("lifeStage: BABY→ADULT al pasar de 4 a 5 acciones")
    void lifeStage_babyToAdult_atFive() {
        pet.setActionCount(4);
        petService.play(1L, OWNER_EMAIL);
        Pet saved = captureLastSavedPet();
        assertThat(saved.getActionCount()).isEqualTo(5);
        assertThat(saved.getLifeStage()).isEqualTo(LifeStage.ADULT);
    }

    @Test
    @DisplayName("lifeStage: ADULT→SENIOR al pasar de 9 a 10 acciones")
    void lifeStage_adultToSenior_atTen() {
        pet.setActionCount(9);
        pet.setLifeStage(LifeStage.ADULT);
        // re-stub para que findById devuelva el mismo objeto actualizado si hace falta
        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));

        petService.play(1L, OWNER_EMAIL);
        Pet saved = captureLastSavedPet();
        assertThat(saved.getActionCount()).isEqualTo(10);
        assertThat(saved.getLifeStage()).isEqualTo(LifeStage.SENIOR);
    }



    @Test
    @DisplayName("feed: 404 si la mascota no existe")
    void feed_notFoundPet() {
        when(petRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> petService.feed(1L, OWNER_EMAIL))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");
    }

    @Test
    @DisplayName("feed: 401 si el usuario no existe")
    void feed_unauthorizedWhenUserNotFound() {
        when(userRepository.findByEmail(OWNER_EMAIL)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> petService.feed(1L, OWNER_EMAIL))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("401");
    }

    @Test
    @DisplayName("feed: 403 si no es el dueño y no es admin")
    void feed_forbiddenWhenNotOwner() {
        User stranger = new User();
        stranger.setId(200L);
        stranger.setEmail("other@example.com");
        stranger.setUsername("other");
        stranger.setRoles(new HashSet<>(Set.of("ROLE_USER")));
        when(userRepository.findByEmail("other@example.com")).thenReturn(Optional.of(stranger));

        assertThatThrownBy(() -> petService.feed(1L, "other@example.com"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403");
        verify(petRepository, never()).save(any());
    }



    @Test
    @DisplayName("adminListPets: 403 si quien llama no es admin")
    void adminListPets_forbiddenWhenNotAdmin() {
        Pageable pageable = PageRequest.of(0, 10);
        assertThatThrownBy(() -> petService.adminListPets(null, pageable, OWNER_EMAIL))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403");
        verify(petRepository, never()).findAll(any(Pageable.class));
        verify(petRepository, never()).findAllByOwnerId(anyLong(), any(Pageable.class));
    }

    @Test
    @DisplayName("adminListPets: lista global cuando ownerId == null")
    void adminListPets_listsAllWhenOwnerIdNull() {
        Pageable pageable = PageRequest.of(0, 2, Sort.by("createdAt").descending());
        List<Pet> content = List.of(pet, clonePet(2L));
        Page<Pet> page = new PageImpl<>(content, pageable, 5);

        when(petRepository.findAll(pageable)).thenReturn(page);

        Page<PetResponse> result = petService.adminListPets(null, pageable, ADMIN_EMAIL);

        verify(petRepository).findAll(pageable);
        verify(petRepository, never()).findAllByOwnerId(anyLong(), any(Pageable.class));
        verify(petMapper, atLeastOnce()).toResponse(any(Pet.class));

        assertThat(result.getTotalElements()).isEqualTo(5);
        assertThat(result.getSize()).isEqualTo(2);
        assertThat(result.getNumber()).isEqualTo(0);
        assertThat(result.getContent()).hasSize(2);
    }

    @Test
    @DisplayName("adminListPets: lista por owner cuando ownerId != null")
    void adminListPets_listsByOwnerWhenOwnerIdPresent() {
        Pageable pageable = PageRequest.of(1, 3);
        List<Pet> content = List.of(pet);
        Page<Pet> page = new PageImpl<>(content, pageable, 1);

        when(petRepository.findAllByOwnerId(100L, pageable)).thenReturn(page);

        Page<PetResponse> result = petService.adminListPets(100L, pageable, ADMIN_EMAIL);

        verify(petRepository).findAllByOwnerId(100L, pageable);
        verify(petRepository, never()).findAll(any(Pageable.class));
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("adminListPets: página vacía se mapea a página vacía")
    void adminListPets_emptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        when(petRepository.findAll(pageable)).thenReturn(Page.empty(pageable));

        Page<PetResponse> res = petService.adminListPets(null, pageable, ADMIN_EMAIL);

        assertThat(res.getTotalElements()).isZero();
        assertThat(res.getContent()).isEmpty();
        assertThat(res.getSize()).isEqualTo(10);
        assertThat(res.getNumber()).isEqualTo(0);
    }



    private Pet captureLastSavedPet() {
        ArgumentCaptor<Pet> captor = ArgumentCaptor.forClass(Pet.class);
        verify(petRepository, atLeastOnce()).save(captor.capture());
        List<Pet> all = captor.getAllValues();
        return all.get(all.size() - 1);
    }

    private Pet clonePet(Long id) {
        Pet p = new Pet();
        p.setId(id);
        p.setName("Pet" + id);
        p.setBreed(Breed.LABRADOR);
        p.setOwner(owner);
        p.setHunger(10);
        p.setHygiene(90);
        p.setFun(80);
        p.setActionCount(0);
        p.setLifeStage(LifeStage.BABY);
        p.setDead(false);
        p.setDeathAt(null);
        return p;
    }
}