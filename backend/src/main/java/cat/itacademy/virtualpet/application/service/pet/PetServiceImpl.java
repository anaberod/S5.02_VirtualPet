package cat.itacademy.virtualpet.application.service.pet;

import cat.itacademy.virtualpet.application.dto.pet.*;
import cat.itacademy.virtualpet.application.mapper.PetMapper;
import cat.itacademy.virtualpet.domain.pet.Pet;
import cat.itacademy.virtualpet.domain.pet.PetRepository;
import cat.itacademy.virtualpet.domain.pet.enums.LifeStage;
import cat.itacademy.virtualpet.domain.user.User;
import cat.itacademy.virtualpet.domain.user.UserRepository;
import cat.itacademy.virtualpet.web.error.PetAlreadyCleanException;
import cat.itacademy.virtualpet.web.error.PetDeceasedException;
import cat.itacademy.virtualpet.web.error.PetNotHungryException;
import cat.itacademy.virtualpet.web.error.PetTooHappyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class PetServiceImpl implements PetService {

    private final PetRepository petRepository;
    private final UserRepository userRepository;
    private final PetMapper petMapper;

    public PetServiceImpl(PetRepository petRepository, UserRepository userRepository, PetMapper petMapper) {
        this.petRepository = petRepository;
        this.userRepository = userRepository;
        this.petMapper = petMapper;
    }

    // ========== ADMIN ==========

    @Override
    public Page<PetResponse> adminListPets(Long ownerId, Pageable pageable, String adminEmail) {
        User admin = getCurrentUser(adminEmail);
        if (!isAdmin(admin)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin only");
        }

        Page<Pet> page = (ownerId != null)
                ? petRepository.findAllByOwnerId(ownerId, pageable)
                : petRepository.findAll(pageable);

        return page.map(petMapper::toResponse);
    }

    // ========== CRUD ==========

    @Override
    public PetResponse createPet(PetCreateRequest request, String userEmail) {
        User owner = getCurrentUser(userEmail);

        Pet pet = petMapper.toEntity(request);
        pet.setOwner(owner);

        // Initial values
        pet.setLifeStage(LifeStage.BABY);
        pet.setHunger(50);
        pet.setHygiene(70);
        pet.setFun(60);
        pet.setActionCount(0);
        pet.setDead(false);
        pet.setDeathAt(null);

        Pet saved = petRepository.save(pet);
        return petMapper.toResponse(saved);
    }

    @Override
    public List<PetResponse> getAllPets(String userEmail) {
        User user = getCurrentUser(userEmail);
        boolean isAdmin = isAdmin(user);

        List<Pet> pets = isAdmin
                ? petRepository.findAll()
                : petRepository.findAllByOwnerId(user.getId());

        return pets.stream().map(petMapper::toResponse).toList();
    }

    @Override
    public PetResponse getPetById(Long id, String userEmail) {
        Pet pet = findPetByIdAndCheckAccess(id, userEmail);
        return petMapper.toResponse(pet);
    }

    @Override
    public PetResponse updatePet(Long id, PetUpdateRequest request, String userEmail) {
        Pet pet = findPetByIdAndCheckAccess(id, userEmail);
        petMapper.updateEntity(pet, request);
        Pet saved = petRepository.save(pet);
        return petMapper.toResponse(saved);
    }

    @Override
    public void deletePet(Long id, String userEmail) {
        Pet pet = findPetByIdAndCheckAccess(id, userEmail);
        petRepository.delete(pet);
    }

    // ========== ACTIONS (balance revertido + warnings) ==========

    @Transactional
    @Override
    public PetActionResponse feed(Long id, String userEmail) {
        Pet pet = findPetByIdAndCheckAccess(id, userEmail);
        checkIfDead(pet);

        // Tope: no alimentar si ya está sin hambre
        if (pet.getHunger() == 0) {
            throw new PetNotHungryException();
        }

        // Efectos
        pet.setHunger(Math.max(0, pet.getHunger() - 70));
        pet.setHygiene(Math.max(0, pet.getHygiene() - 5));
        pet.setFun(Math.max(0, pet.getFun() - 10));

        incrementAndEvaluateDeaths(pet);

        Pet saved = petRepository.save(pet);
        return buildResponseWithWarnings(saved);
    }

    @Transactional
    @Override
    public PetActionResponse wash(Long id, String userEmail) {
        Pet pet = findPetByIdAndCheckAccess(id, userEmail);
        checkIfDead(pet);

        // Tope: no lavar si ya está al 100 de higiene
        if (pet.getHygiene() == 100) {
            throw new PetAlreadyCleanException();
        }

        // Efectos
        pet.setHygiene(Math.min(100, pet.getHygiene() + 30));
        pet.setHunger(Math.min(100, pet.getHunger() + 10));
        pet.setFun(Math.max(0, pet.getFun() - 20));

        incrementAndEvaluateDeaths(pet);

        Pet saved = petRepository.save(pet);
        return buildResponseWithWarnings(saved);
    }

    @Transactional
    @Override
    public PetActionResponse play(Long id, String userEmail) {
        Pet pet = findPetByIdAndCheckAccess(id, userEmail);
        checkIfDead(pet);

        // Tope: no jugar si ya está al 100 de diversión
        if (pet.getFun() == 100) {
            throw new PetTooHappyException();
        }

        // Efectos
        pet.setFun(Math.min(100, pet.getFun() + 40));
        pet.setHunger(Math.min(100, pet.getHunger() + 15));
        // higiene no cambia

        incrementAndEvaluateDeaths(pet);

        Pet saved = petRepository.save(pet);
        return buildResponseWithWarnings(saved);
    }

    // ========== HELPERS ==========

    private User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private boolean isAdmin(User user) {
        return user.getRoles().contains("ROLE_ADMIN");
    }

    private Pet findPetByIdAndCheckAccess(Long id, String userEmail) {
        User user = getCurrentUser(userEmail);
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet not found"));

        if (!isAdmin(user) && !pet.getOwner().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: not your pet");
        }
        return pet;
    }

    private void checkIfDead(Pet pet) {
        if (pet.isDead() || pet.getLifeStage() == LifeStage.PASSED) {
            throw new PetDeceasedException();
        }
    }

    /** Sin tick pasivo: solo contar acción, actualizar etapa y evaluar muertes. */
    private void incrementAndEvaluateDeaths(Pet pet) {
        // contar acción y actualizar etapa (si no ha pasado)
        pet.setActionCount(pet.getActionCount() + 1);
        if (!pet.isDead() && pet.getLifeStage() != LifeStage.PASSED) {
            updateLifeStage(pet);
        }

        // Muertes por umbral
        if (pet.getHunger() == 100 || (pet.getHygiene() == 0 && pet.getFun() == 0)) {
            pet.setDead(true);
            pet.setLifeStage(LifeStage.PASSED);
            pet.setDeathAt(Instant.now());
            return;
        }

        // Muerte por vejez (SENIOR con >=15 acciones)
        if (pet.getLifeStage() == LifeStage.SENIOR && pet.getActionCount() >= 15) {
            pet.setDead(true);
            pet.setLifeStage(LifeStage.PASSED);
            pet.setDeathAt(Instant.now());
        }
    }

    private void updateLifeStage(Pet pet) {
        if (pet.isDead() || pet.getLifeStage() == LifeStage.PASSED) return;

        int count = pet.getActionCount();
        if (count <= 4) pet.setLifeStage(LifeStage.BABY);
        else if (count <= 9) pet.setLifeStage(LifeStage.ADULT);
        else pet.setLifeStage(LifeStage.SENIOR);
    }

    /** Respuesta con mensaje de muerte y lista de avisos (warnings). */
    private PetActionResponse buildResponseWithWarnings(Pet saved) {
        PetActionResponse resp = petMapper.toActionResponse(saved);

        // mensaje si ha muerto
        if (saved.isDead() || saved.getLifeStage() == LifeStage.PASSED) {
            resp.setMessage("Your pet has passed away 💔");
            resp.setWarnings(null); // opcional: no avisos si ya está muerta
            return resp;
        }

        // warnings por umbral
        List<String> warnings = new ArrayList<>();
        if (saved.getHunger() >= 75) warnings.add("hunger_high");
        if (saved.getHygiene() <= 25) warnings.add("hygiene_low");
        if (saved.getFun() <= 25) warnings.add("fun_low");

        resp.setWarnings(warnings.isEmpty() ? null : warnings);
        return resp;
    }
}
