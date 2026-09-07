package coms309.dineder.repository;

import coms309.dineder.entity.AppReview;
import coms309.dineder.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for AppReview entity
 * @author Malak Mansour
 */
@Repository
public interface AppReviewRepository extends JpaRepository<AppReview, Long> {

    List<AppReview> findByUser(User user);
    List<AppReview> findByUserId(Long userId);
    List<AppReview> findByRating(Integer rating);
    List<AppReview> findByRatingGreaterThanEqual(Integer rating);
    List<AppReview> findByRatingLessThanEqual(Integer rating);
    List<AppReview> findAllByOrderByTimestampDesc();

    @Query("SELECT AVG(r.rating) FROM AppReview r")
    Double calculateAverageRating();

    @Query("SELECT COUNT(r) FROM AppReview r")
    Long countTotalReviews();
}