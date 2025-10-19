package cat.itacademy.virtualpet.application.mapper;

import cat.itacademy.virtualpet.application.dto.pet.*;
import cat.itacademy.virtualpet.domain.pet.Pet;
import org.mapstruct.*;


@Mapper(componentModel = "spring")
public interface PetMapper {


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lifeStage", ignore = true)
    @Mapping(target = "hunger", ignore = true)
    @Mapping(target = "hygiene", ignore = true)
    @Mapping(target = "fun", ignore = true)
    @Mapping(target = "actionCount", ignore = true)
    @Mapping(target = "dead", ignore = true)
    @Mapping(target = "deathAt", ignore = true)
    Pet toEntity(PetCreateRequest dto);


    @Mapping(source = "owner.id", target = "ownerId")
    PetResponse toResponse(Pet pet);

    @Mapping(source = "owner.id", target = "ownerId")
    PetActionResponse toActionResponse(Pet pet);


    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "name", source = "name")
    void updateEntity(@MappingTarget Pet pet, PetUpdateRequest dto);
}
