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

/**
 * Implementación del servicio de administración de usuarios.
 */
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
        log.info("ADMIN {} requested full user list", adminEmail);
        List<User> users = userRepository.findAll();
        return userMapper.toResponseList(users);
    }

    // =================== VER USUARIO POR ID ===================

    @Override
    public UserResponse getUserById(Long id, String adminEmail) {
        log.info("ADMIN {} requested user {}", adminEmail, id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return userMapper.toResponse(user);
    }

    // =================== LISTAR MASCOTAS DE UN USUARIO ===================

    @Override
    public List<PetResponse> getUserPets(Long userId, String adminEmail) {
        log.info("ADMIN {} requested pets of user {}", adminEmail, userId);
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        List<Pet> pets = petRepository.findAllByOwnerId(userId);
        return pets.stream().map(petMapper::toResponse).toList();
    }

    // =================== ELIMINAR USUARIO Y SUS MASCOTAS ===================

    @Override
    public void deleteUser(Long id, String adminEmail) {
        log.info("ADMIN {} deleted user {}", adminEmail, id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Eliminar mascotas del usuario antes de borrar el usuario
        List<Pet> pets = petRepository.findAllByOwnerId(user.getId());
        petRepository.deleteAll(pets);

        // Eliminar usuario
        userRepository.delete(user);
    }

    // =================== ELIMINAR UNA MASCOTA CONCRETA DE UN USUARIO ===================

    @Override
    public void deleteUserPet(Long userId, Long petId, String adminEmail) {
        log.info("ADMIN {} requested deletion of pet {} from user {}", adminEmail, petId, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pet not found"));

        if (!pet.getOwner().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Pet does not belong to this user");
        }

        petRepository.delete(pet);
        log.info("ADMIN {} deleted pet {} of user {}", adminEmail, petId, userId);
    }
}
