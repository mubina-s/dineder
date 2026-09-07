package coms309.dineder.dto;

import java.util.Set;

/**
 * Simple dto for restaurants (what gets returned)
 * @author Mason Gliege
 */
public class RestaurantDTO {
    /**
     * Unique identifier
     */
    private Long id;
    /**
     * Name of the restaurant
     */
    private String name;
    /**
     * Set of cuisines associated with a restaurant (cuisines table)
     */
    private Set<String> cuisines;
    /**
     * String for an image url for the restaurant (optional)
     */
    private String image;
    /**
     * Whole number rating of a restaurant (int)
     */
    private int rating;
    /**
     * String indicating a restaurants price range ("$", "$$", "$$$", "N/A")
     */
    private String priceRange;
    /**
     * A restaurants address
     */
    private String address;
    /**
     * A restaurants phone number (String)
     */
    private String phone;
    /**
     * Website of a restaurant
     */
    private String website;

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public Set<String> getCuisines() {
        return cuisines;
    }

    public void setCuisines(Set<String> cuisines) {
        this.cuisines = cuisines;
    }

    public String getImageUrl() {
        return image;
    }
    public void setImageUrl(String image) {
        this.image = image;
    }


}
