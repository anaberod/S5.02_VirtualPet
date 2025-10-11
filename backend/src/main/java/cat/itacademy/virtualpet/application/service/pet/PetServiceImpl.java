package cat.itacademy.virtualpet.application.service.pet;

import cat.itacademy.virtualpet.application.dto.pet.*;
import cat.itacademy.virtualpet.application.mapper.PetMapper;
import cat.itacademy.virtualpet.domain.pet.Pet;
import cat.itacademy.virtualpet.domain.pet.PetRepository;
import cat.itacademy.virtualpet.domain.pet.enums.LifeStage;
import cat.itacademy.virtualpet.domain.user.User;
import cat.itacademy.virtualpet.domain.user.UserRepository;
import cat.itacademy.virtualpet.web.error.PetDeceasedException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

/**
 * Implementation of PetService with business rules:
 * - Stats clamping (0–100)
 * - Evolution by action count
 * - Death after 5 actions in SENIOR stage
 */
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

    // ========== ACTIONS ==========

    @Transactional
    @Override
    public PetActionResponse feed(Long id, String userEmail) {
        Pet pet = findPetByIdAndCheckAccess(id, userEmail);
        checkIfDead(pet);

        pet.setHunger(Math.max(0, pet.getHunger() - 50));
        pet.setHygiene(Math.max(0, pet.getHygiene() - 5));
        incrementActionsAndCheckDeath(pet);

        Pet saved = petRepository.save(pet);
        return toActionResponseWithMessage(saved);
    }

    @Transactional
    @Override
    public PetActionResponse wash(Long id, String userEmail) {
        Pet pet = findPetByIdAndCheckAccess(id, userEmail);
        checkIfDead(pet);

        pet.setHygiene(Math.min(100, pet.getHygiene() + 30));
        pet.setHunger(Math.min(100, pet.getHunger() + 5));
        incrementActionsAndCheckDeath(pet);

        Pet saved = petRepository.save(pet);
        return toActionResponseWithMessage(saved);
    }

    @Transactional
    @Override
    public PetActionResponse play(Long id, String userEmail) {
        Pet pet = findPetByIdAndCheckAccess(id, userEmail);
        checkIfDead(pet);

        pet.setFun(Math.min(100, pet.getFun() + 40));
        pet.setHunger(Math.min(100, pet.getHunger() + 10));
        incrementActionsAndCheckDeath(pet);

        Pet saved = petRepository.save(pet);
        return toActionResponseWithMessage(saved);
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

    /** Lanzar excepción si la mascota ya está muerta */
    private void checkIfDead(Pet pet) {
        if (pet.isDead()) {
            throw new PetDeceasedException();
        }
    }

    /** Incrementa contador, actualiza etapa y comprueba si muere */
    private void incrementActionsAndCheckDeath(Pet pet) {
        pet.setActionCount(pet.getActionCount() + 1);
        updateLifeStage(pet);

        // Death rule: 5 actions in SENIOR (≈ action #15 total)
        if (pet.getLifeStage() == LifeStage.SENIOR && pet.getActionCount() >= 15) {
            pet.setDead(true);
            pet.setDeathAt(Instant.now());
        }
    }

    /** Cambia la etapa de vida según el número de acciones */
    private void updateLifeStage(Pet pet) {
        int count = pet.getActionCount();
        if (count <= 4) pet.setLifeStage(LifeStage.BABY);
        else if (count <= 9) pet.setLifeStage(LifeStage.ADULT);
        else pet.setLifeStage(LifeStage.SENIOR);
    }

    /** Construye la respuesta de acción con mensaje si la mascota ha muerto */
    private PetActionResponse toActionResponseWithMessage(Pet saved) {
        PetActionResponse resp = petMapper.toActionResponse(saved);
        if (saved.isDead()) {
            resp.setMessage("Your pet has passed away 💔");
        }
        return resp;
    }
}
