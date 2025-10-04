package cat.itacademy.virtualpet.application.mapper;

import cat.itacademy.virtualpet.application.dto.pet.*;
import cat.itacademy.virtualpet.domain.pet.Pet;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface PetMapper {

    // JSON -> DTO ya lo hace Jackson; aquí DTO -> Entidad
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true)        // lo asigna el servicio con el usuario del token
    @Mapping(target = "createdAt", ignore = true)    // lo pone la entidad
    @Mapping(target = "lifeStage", ignore = true)    // valores iniciales en el servicio o en la entidad
    @Mapping(target = "hunger", ignore = true)
    @Mapping(target = "hygiene", ignore = true)
    @Mapping(target = "fun", ignore = true)
    @Mapping(target = "actionCount", ignore = true)
    Pet toEntity(PetCreateRequest dto);

    // Entidad -> DTO (propiedad anidada owner.id => ownerId)
    @Mapping(source = "owner.id", target = "ownerId")
    PetResponse toResponse(Pet pet);

    // Igual que PetResponse (tu clase extiende PetResponse, sirve igual)
    @Mapping(source = "owner.id", target = "ownerId")
    PetActionResponse toActionResponse(Pet pet);

    // Para updates simples (p.ej. renombrar)
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "name", source = "name")
    void updateEntity(@MappingTarget Pet pet, PetUpdateRequest dto);
}
