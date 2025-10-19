package cat.itacademy.virtualpet.application.service.user;

import cat.itacademy.virtualpet.application.dto.pet.PetResponse;
import cat.itacademy.virtualpet.application.dto.user.UserResponse;

import java.util.List;


public interface UserService {


    List<UserResponse> getAllUsers(String adminEmail);


    UserResponse getUserById(Long id, String adminEmail);


    List<PetResponse> getUserPets(Long userId, String adminEmail);


    void deleteUser(Long id, String adminEmail);


    void deleteUserPet(Long userId, Long petId, String adminEmail);
}
