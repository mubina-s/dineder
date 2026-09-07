package coms309.dineder.repository;


import coms309.dineder.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository for restaurants
 * @author Mason Gliege
 */
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

    @Transactional
    void deleteById(Long id);
}
