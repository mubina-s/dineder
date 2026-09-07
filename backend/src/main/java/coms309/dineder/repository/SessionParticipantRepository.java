package coms309.dineder.repository;

import coms309.dineder.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/**
 * Repository for session participants.
 * Can return boolean if a participant exists by given user and session
 */
public interface SessionParticipantRepository extends JpaRepository<SessionParticipant, Long> {

    @Transactional
    void deleteById(int id);

    boolean existsByUserAndSession(User user, Session session);

    // Count how many sessions a user has joined
    @Query("SELECT COUNT(sp) FROM SessionParticipant sp WHERE sp.user.id = :userId")
    int countByUserId(@Param("userId") Long userId);
}
