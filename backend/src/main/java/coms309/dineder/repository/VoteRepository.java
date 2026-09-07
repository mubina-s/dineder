package coms309.dineder.repository;

import coms309.dineder.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Repository for Vote entity
 * Handles existing session/round queries and user history/statistics queries
 */
public interface VoteRepository extends JpaRepository<Vote, Long> {

    @Transactional
    void deleteById(Long id);

    /**
     * Existing queries
     */
    @Query("SELECT v FROM Vote v WHERE v.participant.session.id = :sessionId AND v.round = :round")
    List<Vote> findBySessionIdAndRound(@Param("sessionId") int sessionId, @Param("round") int round);

    @Query("SELECT v FROM Vote v WHERE v.restaurant.id = :restaurantId AND v.participant.session.id = :sessionId AND v.round = :round")
    List<Vote> findByRestaurantAndSessionAndRound(@Param("restaurantId") int restaurantId,
                                                  @Param("sessionId") int sessionId,
                                                  @Param("round") int round);

    @Query("SELECT COUNT(v) FROM Vote v WHERE v.restaurant.id = :restaurantId AND v.participant.session.id = :sessionId AND v.round = :round AND v.value = 1")
    Long countVotesForRestaurant(@Param("restaurantId") int restaurantId,
                                 @Param("sessionId") int sessionId,
                                 @Param("round") int round);

    @Query("SELECT v.restaurant.id, COUNT(v) FROM Vote v WHERE v.participant.session.id = :sessionId AND v.round = :round AND v.value = 1 GROUP BY v.restaurant.id")
    List<Object[]> getVoteCountsByRestaurant(@Param("sessionId") Long sessionId, @Param("round") int round);

    @Transactional
    @Modifying
    @Query("DELETE FROM Vote v WHERE v.participant.session.id = :sessionId")
    void deleteBySessionId(@Param("sessionId") Long sessionId);

    /**
     * --- NEW METHODS FOR USER HISTORY/STATISTICS ---
     */

    // All votes by a specific user
    @Query("SELECT v FROM Vote v WHERE v.participant.user.id = :userId")
    List<Vote> findByUserId(@Param("userId") Long userId);

    // Top liked restaurants by user
    @Query("SELECT v.restaurant.name FROM Vote v WHERE v.participant.user.id = :userId AND v.value = 1 " +
            "GROUP BY v.restaurant.name ORDER BY COUNT(v) DESC")
    List<String> findTopRestaurantsByUserId(@Param("userId") Long userId);

    // Top liked cuisines by user
    @Query("SELECT v.restaurant.cuisine FROM Vote v WHERE v.participant.user.id = :userId AND v.value = 1 " +
            "GROUP BY v.restaurant.cuisine ORDER BY COUNT(v) DESC")
    List<String> findTopCuisinesByUserId(@Param("userId") Long userId);

    // Total likes by user
    @Query("SELECT COUNT(v) FROM Vote v WHERE v.participant.user.id = :userId AND v.value = 1")
    int countLikesByUserId(@Param("userId") Long userId);

    // Total dislikes by user
    @Query("SELECT COUNT(v) FROM Vote v WHERE v.participant.user.id = :userId AND v.value = 0")
    int countDislikesByUserId(@Param("userId") Long userId);

    /**
     * Methods for leaderboard:
     */
    // Used for getting all votes after a specific time (for most recent)
    List<Vote> findByTimestampAfter(java.time.Instant timestamp);

    // Used when calculating leaderboard and statistics. Gets all necessary restaurant, cuisine, and voting info at once
    @Query("SELECT v FROM Vote v JOIN FETCH v.restaurant r LEFT JOIN FETCH r.cuisines")
    List<Vote> findAllWithRelations();
}
