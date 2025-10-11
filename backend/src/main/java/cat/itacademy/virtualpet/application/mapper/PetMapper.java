package cat.itacademy.virtualpet.application.mapper;

import cat.itacademy.virtualpet.application.dto.pet.*;
import cat.itacademy.virtualpet.domain.pet.Pet;
import org.mapstruct.*;

/**
 * MapStruct mapper for converting between Pet entities and DTOs.
 * Includes new fields: dead, deathAt.
 */
@Mapper(componentModel = "spring")
public interface PetMapper {

    // ====== DTO → ENTITY ======
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true)        // se asigna en el servicio
    @Mapping(target = "createdAt", ignore = true)    // se genera automáticamente
    @Mapping(target = "lifeStage", ignore = true)    // se inicializa en el servicio
    @Mapping(target = "hunger", ignore = true)
    @Mapping(target = "hygiene", ignore = true)
    @Mapping(target = "fun", ignore = true)
    @Mapping(target = "actionCount", ignore = true)
    @Mapping(target = "dead", ignore = true)         // siempre empieza viva
    @Mapping(target = "deathAt", ignore = true)
    Pet toEntity(PetCreateRequest dto);

    // ====== ENTITY → DTO ======
    @Mapping(source = "owner.id", target = "ownerId")
    PetResponse toResponse(Pet pet);

    @Mapping(source = "owner.id", target = "ownerId")
    PetActionResponse toActionResponse(Pet pet);

    // ====== UPDATE (PATCH) ======
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "name", source = "name")
    void updateEntity(@MappingTarget Pet pet, PetUpdateRequest dto);
}
