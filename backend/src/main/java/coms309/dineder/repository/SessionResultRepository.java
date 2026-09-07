package coms309.dineder.repository;

import coms309.dineder.entity.Restaurant;
import coms309.dineder.entity.SessionResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository for SessionResult entity
 * @author Malak Mansour
 */
public interface SessionResultRepository extends JpaRepository<SessionResult, Long> {
    SessionResult findBySessionId(Long sessionId);

    @Transactional
    void deleteById(Long id);

    // Used for deleting a session result by a restaurant
    @Transactional
    @Modifying
    @Query("DELETE FROM SessionResult sr WHERE sr.restaurant = :restaurant")
    void deleteByRestaurant(Restaurant restaurant);
}