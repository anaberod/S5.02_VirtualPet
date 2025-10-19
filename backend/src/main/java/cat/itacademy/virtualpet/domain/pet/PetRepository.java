package cat.itacademy.virtualpet.domain.pet;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;


@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {


    List<Pet> findAllByOwnerId(Long ownerId);


    boolean existsByIdAndOwnerId(Long id, Long ownerId);


    Optional<Pet> findByIdAndOwnerId(Long id, Long ownerId);

    Page<Pet> findAllByOwnerId(Long ownerId, Pageable pageable);
}
