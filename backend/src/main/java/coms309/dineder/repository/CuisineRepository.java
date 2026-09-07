package coms309.dineder.repository;

import coms309.dineder.entity.Cuisine;
import coms309.dineder.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Simple repository for cuisines. can find by name
 * @author Mason Gliege
 */
@Repository
public interface CuisineRepository extends JpaRepository<Cuisine, Long> {
    Optional<Cuisine> findByName(String name);

}
