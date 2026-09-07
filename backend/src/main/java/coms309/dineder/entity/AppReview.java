package coms309.dineder.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Entity representing an app review submitted by users
 * Part of Main Feature 3/Websocket 2: App Reviews
 *
 * @author Malak Mansour
 */
@Entity
@Table(name = "app_reviews")
public class AppReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer rating;

    @Column(length = 1000)
    private String review;

    @Column(nullable = false)
    private Instant timestamp;

    @PrePersist
    protected void onCreate() {
        timestamp = Instant.now();
    }

    // Constructors
    public AppReview() {}

    public AppReview(User user, Integer rating, String review) {
        this.user = user;
        this.rating = rating;
        this.review = review;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        this.rating = rating;
    }

    public String getReview() { return review; }
    public void setReview(String review) { this.review = review; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}