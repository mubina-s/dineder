package coms309.dineder.dto;

import coms309.dineder.entity.Cuisine;

import java.util.List;

/**
 * Simple dto for creating a restaurant
 * @author Mason Gliege
 */
public class CreateRestaurantRequest {
    /**
     * Name of restaurant to be created
     */
    private String name;
    /**
     * A restaurants address
     */
    private String address;
    /**
     * Whole number rating of a restaurant (int)
     */
    private int rating;
    /**
     * String indicating a restaurants price range ("$", "$$", "$$$", "N/A")
     */
    private String priceRange;
    /**
     * String for a restaurants phone number
     */
    private String phone;
    /**
     * A restaurants website
     */
    private String website;
    /**
     * An optional list of existing cuisine ids that may be associated with a restaurant.
     * For example, include 5 if cuisine id 5 "Mexican" should go with this new restaurant
     */
    private List<Long> cuisineIds;
    /**
     * An optional Long id for an existing image to associate
     */
    private Long imageId;

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

    public List<Long> getCuisineIds() {
        return cuisineIds;
    }

    public void setCuisineIds(List<Long> cuisineIds) {
        this.cuisineIds = cuisineIds;
    }

    public Long getImageId() {
        return imageId;
    }

    public void setImageId(Long imageId) {
        this.imageId = imageId;
    }
}
