package coms309.dineder.service;

import coms309.dineder.entity.AppReview;
import coms309.dineder.entity.User;
import coms309.dineder.repository.AppReviewRepository;
import coms309.dineder.repository.UserRepository;
import coms309.dineder.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for App Reviews
 * Handles business logic and database operations
 * Part of Main Feature 3/Websocket 2: App Reviews
 *
 * @author Malak Mansour
 */
@Service
public class AppReviewService {

    @Autowired
    private AppReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Create a new review (atomic write: rating + text saved together)
     */
    @Transactional
    public ReviewResponse createReview(CreateReviewRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Validate rating
        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        // Create and save review (atomic operation)
        AppReview review = new AppReview(user, request.getRating(), request.getReview());
        AppReview savedReview = reviewRepository.save(review);

        return convertToResponse(savedReview);
    }

    /**
     * Get all reviews ordered by newest first
     */
    public List<ReviewResponse> getAllReviews() {
        return reviewRepository.findAllByOrderByTimestampDesc().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get reviews by user ID
     */
    public List<ReviewResponse> getReviewsByUserId(Long userId) {
        return reviewRepository.findByUserId(userId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get reviews by rating (for filtering)
     */
    public List<ReviewResponse> getReviewsByRating(Integer rating) {
        return reviewRepository.findByRating(rating).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get low-rated reviews (for admin notifications)
     */
    public List<ReviewResponse> getLowRatedReviews(Integer threshold) {
        return reviewRepository.findByRatingLessThanEqual(threshold).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update an existing review
     */
    @Transactional
    public ReviewResponse updateReview(Long reviewId, Integer newRating, String newReviewText) {
        AppReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        if (newRating != null) {
            review.setRating(newRating);
        }
        if (newReviewText != null) {
            review.setReview(newReviewText);
        }

        AppReview updatedReview = reviewRepository.save(review);
        return convertToResponse(updatedReview);
    }

    /**
     * Delete a review
     */
    @Transactional
    public void deleteReview(Long reviewId) {
        if (!reviewRepository.existsById(reviewId)) {
            throw new RuntimeException("Review not found");
        }
        reviewRepository.deleteById(reviewId);
    }

    /**
     * Get app statistics (total users, reviews, average rating)
     */
    public AppStatisticsResponse getAppStatistics() {
        Long totalUsers = userRepository.count();
        Long totalReviews = reviewRepository.countTotalReviews();
        Double averageRating = reviewRepository.calculateAverageRating();

        return new AppStatisticsResponse(totalUsers, totalReviews,
                averageRating != null ? averageRating : 0.0);
    }

    /**
     * Check if review should trigger admin notification
     * (ratings <= 2 stars trigger notifications)
     */
    public boolean shouldNotifyAdmin(Integer rating) {
        return rating <= 2;
    }

    /**
     * Convert AppReview entity to ReviewResponse DTO
     */
    private ReviewResponse convertToResponse(AppReview review) {
        return new ReviewResponse(
                review.getId(),
                review.getUser().getId(),
                review.getUser().getUsername(),
                review.getRating(),
                review.getReview(),
                review.getTimestamp()
        );
    }
}