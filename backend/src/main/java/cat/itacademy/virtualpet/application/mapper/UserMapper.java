package cat.itacademy.virtualpet.application.mapper;

import cat.itacademy.virtualpet.application.dto.user.UserResponse;
import cat.itacademy.virtualpet.domain.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "roles", source = "roles")
    UserResponse toResponse(User user);

    // útil cuando luego listes usuarios (admin)
    List<UserResponse> toResponseList(List<User> users);
}
