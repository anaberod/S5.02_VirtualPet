package cat.itacademy.virtualpet.application.mapper;

import cat.itacademy.virtualpet.application.dto.pet.PetActionResponse;
import cat.itacademy.virtualpet.application.dto.pet.PetCreateRequest;
import cat.itacademy.virtualpet.application.dto.pet.PetResponse;
import cat.itacademy.virtualpet.application.dto.pet.PetUpdateRequest;
import cat.itacademy.virtualpet.domain.pet.Pet;
import cat.itacademy.virtualpet.domain.user.User;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-10-08T13:13:57+0200",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.8 (Eclipse Adoptium)"
)
@Component
public class PetMapperImpl implements PetMapper {

    @Override
    public Pet toEntity(PetCreateRequest dto) {
        if ( dto == null ) {
            return null;
        }

        Pet pet = new Pet();

        pet.setName( dto.getName() );
        pet.setBreed( dto.getBreed() );

        return pet;
    }

    @Override
    public PetResponse toResponse(Pet pet) {
        if ( pet == null ) {
            return null;
        }

        PetResponse petResponse = new PetResponse();

        petResponse.setOwnerId( petOwnerId( pet ) );
        petResponse.setId( pet.getId() );
        petResponse.setName( pet.getName() );
        petResponse.setBreed( pet.getBreed() );
        petResponse.setLifeStage( pet.getLifeStage() );
        petResponse.setHunger( pet.getHunger() );
        petResponse.setHygiene( pet.getHygiene() );
        petResponse.setFun( pet.getFun() );
        petResponse.setActionCount( pet.getActionCount() );
        petResponse.setCreatedAt( pet.getCreatedAt() );

        return petResponse;
    }

    @Override
    public PetActionResponse toActionResponse(Pet pet) {
        if ( pet == null ) {
            return null;
        }

        PetActionResponse petActionResponse = new PetActionResponse();

        petActionResponse.setOwnerId( petOwnerId( pet ) );
        petActionResponse.setId( pet.getId() );
        petActionResponse.setName( pet.getName() );
        petActionResponse.setBreed( pet.getBreed() );
        petActionResponse.setLifeStage( pet.getLifeStage() );
        petActionResponse.setHunger( pet.getHunger() );
        petActionResponse.setHygiene( pet.getHygiene() );
        petActionResponse.setFun( pet.getFun() );
        petActionResponse.setActionCount( pet.getActionCount() );
        petActionResponse.setCreatedAt( pet.getCreatedAt() );

        return petActionResponse;
    }

    @Override
    public void updateEntity(Pet pet, PetUpdateRequest dto) {
        if ( dto == null ) {
            return;
        }

        pet.setName( dto.getName() );
    }

    private Long petOwnerId(Pet pet) {
        if ( pet == null ) {
            return null;
        }
        User owner = pet.getOwner();
        if ( owner == null ) {
            return null;
        }
        Long id = owner.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
