package cat.itacademy.virtualpet.application.service.user;

import cat.itacademy.virtualpet.application.dto.pet.PetResponse;
import cat.itacademy.virtualpet.application.dto.user.UserResponse;

import java.util.List;

/**
 * Servicio de administración de usuarios (solo para ROLE_ADMIN).
 * Permite al administrador listar usuarios, ver mascotas y eliminar.
 */
public interface UserService {

    /**
     * Lista todos los usuarios del sistema.
     */
    List<UserResponse> getAllUsers(String adminEmail);

    /**
     * Devuelve la información de un usuario concreto.
     */
    UserResponse getUserById(Long id, String adminEmail);

    /**
     * Lista todas las mascotas pertenecientes a un usuario.
     */
    List<PetResponse> getUserPets(Long userId, String adminEmail);

    /**
     * Elimina un usuario y todas sus mascotas.
     */
    void deleteUser(Long id, String adminEmail);

    /**
     * Elimina una mascota específica de un usuario.
     */
    void deleteUserPet(Long userId, Long petId, String adminEmail);
}
