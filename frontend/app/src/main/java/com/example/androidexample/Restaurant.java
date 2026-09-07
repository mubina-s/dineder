package com.example.androidexample;

import java.util.Set;

/**
 * Represents a single restaurant option inside the DineDer app.
 * <p>
 * This class is a simple data model used on the frontend to display
 * restaurant information such as:
 * </p>
 * <ul>
 *     <li>Name</li>
 *     <li>Rating</li>
 *     <li>Price range</li>
 *     <li>Address and contact details</li>
 * </ul>
 * <p>
 * It has no Android dependencies and is safe to use in unit tests
 * or for Javadoc generation.
 * </p>
 *
 * @author Mubina Sadriddinova
 */
public class Restaurant {

    /** Unique identifier for the restaurant. */
    public Long id;

    /** Display name of the restaurant. */
    public String name;

    /** Rating value from 1 to 5 stars. */
    public int rating;

    /** Price category such as "$", "$$", or "$$$". */
    public String priceRange;

    /** Physical street address of the restaurant. */
    public String address;

    /** Phone number customers can call. */
    public String phone;

    /** Website URL of the restaurant, if available. */
    public String website;

    public Set<String> cuisines;

    public String image;

    /**
     * Default empty constructor.
     * Used when building an object manually or from JSON.
     */
    public Restaurant() { }

    /**
     * Creates a restaurant with only a name.
     *
     * @param name name of the restaurant
     */
    public Restaurant(String name) {
        this.name = name;
    }

    /**
     * Creates a full restaurant object with all available fields.
     *
     * @param id          unique restaurant ID
     * @param name        restaurant name
     * @param rating      rating from 1 to 5 stars
     * @param priceRange  price indication (e.g., "$$")
     * @param address     restaurant address
     * @param phone       contact phone number
     * @param website     website URL
     */
    public Restaurant(Long id, String name, int rating, String priceRange,
                      String address, String phone, String website, Set<String> cuisines, String image) {
        this.id = id;
        this.name = name;
        this.rating = rating;
        this.priceRange = priceRange;
        this.address = address;
        this.phone = phone;
        this.website = website;
        this.cuisines = cuisines;
        this.image = image;
    }

    /**
     * Returns a readable string containing restaurant details.
     *
     * @return restaurant information in text form
     */
    @Override
    public String toString() {
        return "Restaurant{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", rating=" + rating +
                ", priceRange='" + priceRange + '\'' +
                ", address='" + address + '\'' +
                ", phone='" + phone + '\'' +
                ", website='" + website + '\'' +
                ", cuisines='" + cuisines + '\'' +
                ", image='" + image + '\'' +
                '}';
    }
}
