package cat.itacademy.virtualpet.application.service.pet;

import cat.itacademy.virtualpet.application.dto.pet.*;
import cat.itacademy.virtualpet.application.mapper.PetMapper;
import cat.itacademy.virtualpet.domain.pet.Pet;
import cat.itacademy.virtualpet.domain.pet.PetRepository;
import cat.itacademy.virtualpet.domain.pet.enums.LifeStage;
import cat.itacademy.virtualpet.domain.user.User;
import cat.itacademy.virtualpet.domain.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Implementation of PetService with MapStruct, business rules, clamps, and ownership checks.
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

    @Override
    public Page<PetResponse> adminListPets(Long ownerId, Pageable pageable, String adminEmail) {
        User admin = getCurrentUser(adminEmail);
        if (!isAdmin(admin)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin only");
        }

        Page<Pet> page = (ownerId != null)
                ? petRepository.findAllByOwnerId(ownerId, pageable)
                : petRepository.findAll(pageable);

        // Page<T> tiene .map(...)
        return page.map(petMapper::toResponse);
    }

    // ================= CRUD =================

    @Override
    public PetResponse createPet(PetCreateRequest request, String userEmail) {
        User owner = getCurrentUser(userEmail);

        Pet pet = petMapper.toEntity(request);
        pet.setOwner(owner);

        // Valores iniciales
        pet.setLifeStage(LifeStage.BABY);
        pet.setHunger(50);
        pet.setHygiene(70);
        pet.setFun(60);
        pet.setActionCount(0);

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

    // ================= ACTIONS =================

    @Override
    public PetActionResponse feed(Long id, String userEmail) {
        Pet pet = findPetByIdAndCheckAccess(id, userEmail);
        pet.setHunger(Math.max(0, pet.getHunger() - 50));
        pet.setHygiene(Math.max(0, pet.getHygiene() - 5));
        incrementActions(pet);
        Pet saved = petRepository.save(pet);
        return petMapper.toActionResponse(saved);
    }

    @Override
    public PetActionResponse wash(Long id, String userEmail) {
        Pet pet = findPetByIdAndCheckAccess(id, userEmail);
        pet.setHygiene(Math.min(100, pet.getHygiene() + 30));
        pet.setHunger(Math.min(100, pet.getHunger() + 5));
        incrementActions(pet);
        Pet saved = petRepository.save(pet);
        return petMapper.toActionResponse(saved);
    }

    @Override
    public PetActionResponse play(Long id, String userEmail) {
        Pet pet = findPetByIdAndCheckAccess(id, userEmail);
        pet.setFun(Math.min(100, pet.getFun() + 40));
        pet.setHunger(Math.min(100, pet.getHunger() + 10));
        incrementActions(pet);
        Pet saved = petRepository.save(pet);
        return petMapper.toActionResponse(saved);
    }

    // ================= HELPERS =================

    /** Busca al usuario autenticado */
    private User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    /** Comprueba si el usuario tiene rol ADMIN */
    private boolean isAdmin(User user) {
        return user.getRoles().contains("ROLE_ADMIN");
    }

    /** Busca mascota por id y comprueba permisos */
    private Pet findPetByIdAndCheckAccess(Long id, String userEmail) {
        User user = getCurrentUser(userEmail);
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet not found"));

        if (!isAdmin(user) && !pet.getOwner().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: not your pet");
        }
        return pet;
    }

    /** Incrementa contador y recalcula etapa de vida */
    private void incrementActions(Pet pet) {
        pet.setActionCount(pet.getActionCount() + 1);
        updateLifeStage(pet);
    }

    /** Cambia la etapa de vida según el número de acciones */
    private void updateLifeStage(Pet pet) {
        int count = pet.getActionCount();
        if (count <= 4) pet.setLifeStage(LifeStage.BABY);
        else if (count <= 9) pet.setLifeStage(LifeStage.ADULT);
        else pet.setLifeStage(LifeStage.SENIOR);
    }
}
