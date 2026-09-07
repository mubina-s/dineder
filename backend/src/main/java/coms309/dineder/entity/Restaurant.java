package coms309.dineder.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**

 * Restaurant entity. Has name, address, rating, priceRange, phone, website.
 * One to many with votes
 * Many to many with cuisines
 * Many to one with images
 * @author Mason Gliege
 */
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String cuisine;
    private String address;
    private int rating;
    private String priceRange;
    private String phone;
    private String website;

    @OneToMany(mappedBy = "restaurant", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Vote> votes = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "restaurantCuisines", joinColumns = @JoinColumn(name = "restaurantId"), inverseJoinColumns = @JoinColumn(name = "cuisineId"))
    private Set<Cuisine> cuisines = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "imageId")
    private Image image;

    // Constructors
    public Restaurant() {}

    public Restaurant(String name, String address, int rating, String priceRange) {
        this.name = name;
        this.address = address;
        this.rating = rating;
        this.priceRange = priceRange;
    }

    public Restaurant(String name, String address, int rating, String priceRange, String phone, String website) {
        this.name = name;
        this.address = address;
        this.rating = rating;
        this.priceRange = priceRange;
        this.phone = phone;
        this.website = website;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getPriceRange() {
        return priceRange;
    }

    public void setPriceRange(String priceRange) {
        this.priceRange = priceRange;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public Set<Vote> getVotes() {
        return Collections.unmodifiableSet(votes);
    }

    public void setVotes(Set<Vote> votes) {
        this.votes = votes;
    }

    public Set<Cuisine> getCuisines() {
        return Collections.unmodifiableSet(cuisines);
    }

    public void setCuisines(Set<Cuisine> cuisines) {
        this.cuisines = cuisines;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    // Helper methods for safe bidirectional updates
    public void addVote(Vote vote) {
        votes.add(vote);
        vote.setRestaurant(this);
    }

    public void removeVote(Vote vote) {
        votes.remove(vote);
        vote.setRestaurant(null);
    }

    public void addCuisine(Cuisine cuisine) {
        cuisines.add(cuisine);
        cuisine.getRestaurants().add(this);
    }

    public void removeCuisine(Cuisine cuisine) {
        cuisines.remove(cuisine);
        cuisine.getRestaurants().remove(this);
    }

    // equals/hashCode based on ID
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Restaurant)) return false;
        Restaurant that = (Restaurant) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
