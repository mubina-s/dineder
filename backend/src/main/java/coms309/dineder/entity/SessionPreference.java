package coms309.dineder.entity;

import jakarta.persistence.*;

@Entity
public class SessionPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String preferenceName;
    private String rating; // "N/A", "1", "2", "3", "4", "5"
    private String priceRange; // "N/A", "$", "$$", "$$$"

    // Foreign key to User (host)
    @ManyToOne
    @JoinColumn(name = "host_id")
    private User host;

    public SessionPreference() {
    }

    public SessionPreference(String preferenceName, String rating, String priceRange, User host) {
        this.preferenceName = preferenceName;
        this.rating = rating;
        this.priceRange = priceRange;
        this.host = host;
    }

    // =============================== Getters and Setters ================================== //

    public Long getId() {
        return id;
    }

    public String getPreferenceName() {
        return preferenceName;
    }

    public void setPreferenceName(String preferenceName) {
        this.preferenceName = preferenceName;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getPriceRange() {
        return priceRange;
    }

    public void setPriceRange(String priceRange) {
        this.priceRange = priceRange;
    }

    public User getHost() {
        return host;
    }

    public void setHost(User host) {
        this.host = host;
    }
}