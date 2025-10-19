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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Slf4j
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

    // ================== ADMIN ==================

    @Override
    public Page<PetResponse> adminListPets(Long ownerId, Pageable pageable, String adminEmail) {
        log.info("ADMIN {} → LIST PETS ownerId={} page={} size={} sort={}",
                adminEmail, ownerId,
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());

        User admin = getCurrentUser(adminEmail);
        if (!isAdmin(admin)) {
            log.warn("Forbidden: user {} tried to list all pets without ADMIN role", adminEmail);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin only");
        }

        Page<Pet> page = (ownerId != null)
                ? petRepository.findAllByOwnerId(ownerId, pageable)
                : petRepository.findAll(pageable);

        log.debug("ADMIN {} → LIST PETS result count={}", adminEmail, page.getNumberOfElements());
        return page.map(petMapper::toResponse);
    }

    // ================== CRUD ==================

    @Override
    public PetResponse createPet(PetCreateRequest request, String userEmail) {
        log.info("USER {} → CREATE PET name={}", userEmail, request.getName());
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
        log.info("USER {} → CREATED PET id={} name={} stage={}", userEmail, saved.getId(), saved.getName(), saved.getLifeStage());
        return petMapper.toResponse(saved);
    }

    @Override
    public List<PetResponse> getAllPets(String userEmail) {
        log.info("USER {} → GET ALL PETS", userEmail);
        User user = getCurrentUser(userEmail);
        boolean isAdmin = isAdmin(user);

        List<Pet> pets = isAdmin
                ? petRepository.findAll()
                : petRepository.findAllByOwnerId(user.getId());

        log.debug("USER {} → GET ALL PETS count={} (admin={})", userEmail, pets.size(), isAdmin);
        return pets.stream().map(petMapper::toResponse).toList();
    }

    @Override
    public PetResponse getPetById(Long id, String userEmail) {
        log.info("USER {} → GET PET {}", userEmail, id);
        Pet pet = findPetByIdAndCheckAccess(id, userEmail);
        return petMapper.toResponse(pet);
    }

    @Override
    public PetResponse updatePet(Long id, PetUpdateRequest request, String userEmail) {
        log.info("USER {} → UPDATE PET {}", userEmail, id);
        Pet pet = findPetByIdAndCheckAccess(id, userEmail);
        petMapper.updateEntity(pet, request);
        Pet saved = petRepository.save(pet);
        log.debug("USER {} → UPDATED PET {} -> stage={} hunger={} hygiene={} fun={}",
                userEmail, id, saved.getLifeStage(), saved.getHunger(), saved.getHygiene(), saved.getFun());
        return petMapper.toResponse(saved);
    }

    @Override
    public void deletePet(Long id, String userEmail) {
        log.info("USER {} → DELETE PET {}", userEmail, id);
        Pet pet = findPetByIdAndCheckAccess(id, userEmail);
        petRepository.delete(pet);
        log.info("USER {} → DELETED PET {}", userEmail, id);
    }

    // ================== ACTIONS ==================

    @Transactional
    @Override
    public PetActionResponse feed(Long id, String userEmail) {
        log.info("USER {} → FEED PET {}", userEmail, id);
        Pet pet = findPetByIdAndCheckAccess(id, userEmail);
        checkIfDead(pet);

        if (pet.getHunger() == 0) {
            log.warn("PET {} cannot be fed: hunger already 0", id);
            throw new PetNotHungryException();
        }

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
        log.info("USER {} → WASH PET {}", userEmail, id);
        Pet pet = findPetByIdAndCheckAccess(id, userEmail);
        checkIfDead(pet);

        if (pet.getHygiene() == 100) {
            log.warn("PET {} cannot be washed: hygiene already 100", id);
            throw new PetAlreadyCleanException();
        }

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
        log.info("USER {} → PLAY PET {}", userEmail, id);
        Pet pet = findPetByIdAndCheckAccess(id, userEmail);
        checkIfDead(pet);

        if (pet.getFun() == 100) {
            log.warn("PET {} cannot play: fun already 100", id);
            throw new PetTooHappyException();
        }

        pet.setFun(Math.min(100, pet.getFun() + 40));
        pet.setHunger(Math.min(100, pet.getHunger() + 15));

        incrementAndEvaluateDeaths(pet);
        Pet saved = petRepository.save(pet);
        return buildResponseWithWarnings(saved);
    }

    // ================== HELPERS ==================

    private User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("User not found by email {}", email);
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found");
                });
    }

    private boolean isAdmin(User user) {
        return user.getRoles().contains("ROLE_ADMIN");
    }

    private Pet findPetByIdAndCheckAccess(Long id, String userEmail) {
        User user = getCurrentUser(userEmail);
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("PET {} not found", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet not found");
                });

        if (!isAdmin(user) && !pet.getOwner().getId().equals(user.getId())) {
            log.warn("Access denied: user {} trying to access PET {} owned by {}", userEmail, id, pet.getOwner().getId());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied: not your pet");
        }
        return pet;
    }

    private void checkIfDead(Pet pet) {
        if (pet.isDead() || pet.getLifeStage() == LifeStage.PASSED) {
            log.warn("Operation on deceased PET {} (stage={}, dead={})", pet.getId(), pet.getLifeStage(), pet.isDead());
            throw new PetDeceasedException();
        }
    }

    private void incrementAndEvaluateDeaths(Pet pet) {
        pet.setActionCount(pet.getActionCount() + 1);
        if (!pet.isDead() && pet.getLifeStage() != LifeStage.PASSED) {
            updateLifeStage(pet);
        }

        if (pet.getHunger() == 100 || (pet.getHygiene() == 0 && pet.getFun() == 0)) {
            pet.setDead(true);
            pet.setLifeStage(LifeStage.PASSED);
            pet.setDeathAt(Instant.now());
            return;
        }

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

    private PetActionResponse buildResponseWithWarnings(Pet saved) {
        PetActionResponse resp = petMapper.toActionResponse(saved);
        if (saved.isDead() || saved.getLifeStage() == LifeStage.PASSED) {
            resp.setMessage("Your pet has passed away 💔");
            resp.setWarnings(null);
            return resp;
        }

        List<String> warnings = new ArrayList<>();
        if (saved.getHunger() >= 75) warnings.add("hunger_high");
        if (saved.getHygiene() <= 25) warnings.add("hygiene_low");
        if (saved.getFun() <= 25) warnings.add("fun_low");
        resp.setWarnings(warnings.isEmpty() ? null : warnings);
        return resp;
    }
}
