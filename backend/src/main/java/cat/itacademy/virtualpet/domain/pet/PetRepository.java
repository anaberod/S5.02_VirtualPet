package cat.itacademy.virtualpet.domain.pet;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * JPA repository for managing Pet entities.
 */
@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {

    /**
     * Finds all pets owned by a specific user.
     */
    List<Pet> findAllByOwnerId(Long ownerId);

    /**
     * Checks if a pet exists and belongs to a specific owner.
     */
    boolean existsByIdAndOwnerId(Long id, Long ownerId);

    /**
     * (Optional) Finds a pet by its id and owner id.
     */
    Optional<Pet> findByIdAndOwnerId(Long id, Long ownerId);

    Page<Pet> findAllByOwnerId(Long ownerId, Pageable pageable);
}
