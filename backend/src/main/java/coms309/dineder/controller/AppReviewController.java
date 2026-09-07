package coms309.dineder.controller;

import coms309.dineder.dto.*;
import coms309.dineder.service.AppReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.Parameter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for App Reviews.
 * Handles CRUD operations & analytics for the App Review feature.
 * Part of Main Feature 3 / WebSocket 2
 *
 * @author Malak Mansour
 */
@RestController
@RequestMapping("/api/reviews")
@Tag(name = "App Reviews", description = "Endpoints for creating and managing app reviews")
public class AppReviewController {

    @Autowired
    private AppReviewService reviewService;

    // -------------------------------------------------------------------------
    @Operation(
            summary = "Create a new review",
            description = "Creates a review using a user ID, review text, and rating (1-5).",
            requestBody = @RequestBody(
                    required = true,
                    description = "JSON containing review details (userId, reviewText, rating)",
                    content = @Content(schema = @Schema(implementation = CreateReviewRequest.class))
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Review successfully created",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ReviewResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data (e.g., rating out of range, missing required fields)",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content
            )
    })
    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(
            @org.springframework.web.bind.annotation.RequestBody CreateReviewRequest request) {
        try {
            ReviewResponse review = reviewService.createReview(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(review);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // -------------------------------------------------------------------------
    @Operation(
            summary = "Get all reviews",
            description = "Retrieves a list of every review stored in the system."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of reviews retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ReviewResponse.class))
                    )
            )
    })
    @GetMapping
    public ResponseEntity<List<ReviewResponse>> getAllReviews() {
        return ResponseEntity.ok(reviewService.getAllReviews());
    }

    // -------------------------------------------------------------------------
    @Operation(
            summary = "Get reviews by a specific user",
            description = "Returns all reviews submitted by the given user ID."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User reviews retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ReviewResponse.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content
            )
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsByUserId(
            @Parameter(description = "ID of the user whose reviews are retrieved", required = true, example = "1")
            @PathVariable Long userId) {
        return ResponseEntity.ok(reviewService.getReviewsByUserId(userId));
    }

    // -------------------------------------------------------------------------
    @Operation(
            summary = "Get reviews by rating",
            description = "Returns all reviews that match a specific star rating (1–5)."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Reviews retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ReviewResponse.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid rating value (must be 1-5)",
                    content = @Content
            )
    })
    @GetMapping("/rating/{rating}")
    public ResponseEntity<List<ReviewResponse>> getReviewsByRating(
            @Parameter(description = "Rating value (1–5)", required = true, example = "5")
            @PathVariable Integer rating) {
        if (rating < 1 || rating > 5) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(reviewService.getReviewsByRating(rating));
    }

    // -------------------------------------------------------------------------
    @Operation(
            summary = "Get low-rated reviews",
            description = "Retrieves reviews below a given rating threshold for admin monitoring. Default threshold = 2."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Low-rated reviews retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = ReviewResponse.class))
                    )
            )
    })
    @GetMapping("/low-rated")
    public ResponseEntity<List<ReviewResponse>> getLowRatedReviews(
            @Parameter(description = "Maximum rating threshold (reviews with rating <= threshold)", example = "2")
            @RequestParam(defaultValue = "2") Integer threshold) {
        return ResponseEntity.ok(reviewService.getLowRatedReviews(threshold));
    }

    // -------------------------------------------------------------------------
    @Operation(
            summary = "Update an existing review",
            description = "Updates a review's rating and/or text. At least one parameter (rating or review) must be provided."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Review updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ReviewResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Review not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content
            )
    })
    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> updateReview(
            @Parameter(description = "ID of the review to update", required = true, example = "1")
            @PathVariable Long reviewId,
            @Parameter(description = "Updated rating (1-5)", example = "4")
            @RequestParam(required = false) Integer rating,
            @Parameter(description = "Updated review text", example = "Great app!")
            @RequestParam(required = false) String review) {

        try {
            ReviewResponse updated = reviewService.updateReview(reviewId, rating, review);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // -------------------------------------------------------------------------
    @Operation(
            summary = "Delete a review",
            description = "Permanently deletes a review by its ID."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Review deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Review not found",
                    content = @Content
            )
    })
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @Parameter(description = "ID of the review to delete", required = true, example = "1")
            @PathVariable Long reviewId) {
        try {
            reviewService.deleteReview(reviewId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // -------------------------------------------------------------------------
    @Operation(
            summary = "Get app rating statistics",
            description = "Returns system-wide analytics including average rating, total review count, and rating distribution."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Statistics retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AppStatisticsResponse.class)
                    )
            )
    })
    @GetMapping("/statistics")
    public ResponseEntity<AppStatisticsResponse> getStatistics() {
        return ResponseEntity.ok(reviewService.getAppStatistics());
    }
}