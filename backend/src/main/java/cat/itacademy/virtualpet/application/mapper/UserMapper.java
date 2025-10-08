package cat.itacademy.virtualpet.application.mapper;

import cat.itacademy.virtualpet.application.dto.user.UserResponse;
import cat.itacademy.virtualpet.domain.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Convierte entidades User a DTOs UserResponse.
 * Utilizado en el bloque de administración (/admin/users).
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "roles", source = "roles")
    @Mapping(target = "createdAt", source = "createdAt")
    UserResponse toResponse(User user);

    /**
     * Convierte una lista de entidades User a una lista de DTOs UserResponse.
     * Útil para listados en la vista de administración.
     */
    List<UserResponse> toResponseList(List<User> users);
}
