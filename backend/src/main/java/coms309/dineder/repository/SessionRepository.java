package coms309.dineder.repository;

import coms309.dineder.entity.Restaurant;
import coms309.dineder.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Repository for sessions.
 * Can delete by id, find by join code, exists by join code, find all associated restaurants
 * @author Mason Gliege
 */
public interface SessionRepository extends JpaRepository<Session, Long> {

    @Transactional
    void deleteById(Long id);

    Optional<Session> findByJoinCode(String joinCode);


    boolean existsByJoinCode(String code);

    List<Session> findAllByRestaurants(Restaurant restaurant);
}
