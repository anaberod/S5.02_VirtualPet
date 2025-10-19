package cat.itacademy.virtualpet.application.service.user;

import cat.itacademy.virtualpet.application.dto.pet.PetResponse;
import cat.itacademy.virtualpet.application.dto.user.UserResponse;
import cat.itacademy.virtualpet.application.mapper.PetMapper;
import cat.itacademy.virtualpet.application.mapper.UserMapper;
import cat.itacademy.virtualpet.domain.pet.Pet;
import cat.itacademy.virtualpet.domain.pet.PetRepository;
import cat.itacademy.virtualpet.domain.user.User;
import cat.itacademy.virtualpet.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PetRepository petRepository;
    private final UserMapper userMapper;
    private final PetMapper petMapper;

    // =================== LISTAR USUARIOS ===================

    @Override
    public List<UserResponse> getAllUsers(String adminEmail) {
        log.info("ADMIN {} → LIST USERS", adminEmail);
        List<User> users = userRepository.findAll();
        log.debug("ADMIN {} → LIST USERS count={}", adminEmail, users.size());
        return userMapper.toResponseList(users);
    }

    // =================== VER USUARIO POR ID ===================

    @Override
    public UserResponse getUserById(Long id, String adminEmail) {
        log.info("ADMIN {} → GET USER {}", adminEmail, id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("GET USER {} → NOT FOUND", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                });
        log.debug("GET USER {} → username={} email={}", id, user.getUsername(), user.getEmail());
        return userMapper.toResponse(user);
    }

    // =================== LISTAR MASCOTAS DE UN USUARIO ===================

    @Override
    public List<PetResponse> getUserPets(Long userId, String adminEmail) {
        log.info("ADMIN {} → LIST USER PETS userId={}", adminEmail, userId);
        if (!userRepository.existsById(userId)) {
            log.warn("LIST USER PETS → userId={} NOT FOUND", userId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        List<Pet> pets = petRepository.findAllByOwnerId(userId);
        log.debug("LIST USER PETS → userId={} count={}", userId, pets.size());
        return pets.stream().map(petMapper::toResponse).toList();
    }

    // =================== ELIMINAR USUARIO Y SUS MASCOTAS ===================

    @Override
    public void deleteUser(Long id, String adminEmail) {
        log.info("ADMIN {} → DELETE USER {}", adminEmail, id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("DELETE USER {} → NOT FOUND", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                });

        List<Pet> pets = petRepository.findAllByOwnerId(user.getId());
        int petCount = pets.size();
        if (petCount > 0) {
            log.debug("DELETE USER {} → deleting {} pets first", id, petCount);
            petRepository.deleteAll(pets);
        }

        userRepository.delete(user);
        log.info("ADMIN {} → DELETED USER {} (and {} pets)", adminEmail, id, petCount);
    }

    // =================== ELIMINAR UNA MASCOTA CONCRETA DE UN USUARIO ===================

    @Override
    public void deleteUserPet(Long userId, Long petId, String adminEmail) {
        log.info("ADMIN {} → DELETE PET {} of USER {}", adminEmail, petId, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("DELETE PET {} → USER {} NOT FOUND", petId, userId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                });

        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> {
                    log.warn("DELETE PET {} → NOT FOUND", petId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet not found");
                });

        if (!pet.getOwner().getId().equals(user.getId())) {
            log.warn("DELETE PET {} → belongsToUser={} but requestedUser={}", petId, pet.getOwner().getId(), userId);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Pet does not belong to this user");
        }

        petRepository.delete(pet);
        log.info("ADMIN {} → DELETED PET {} of USER {}", adminEmail, petId, userId);
    }
}
