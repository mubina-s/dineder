package coms309.dineder.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Cuisine entity. Essentially just a String for name. Also has set of restaurants its mapped to.
 * Many to many with restaurants
 * @author Mason Gliege
 */
@Entity
@Table(name = "cuisines")
public class Cuisine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name; // Mexican, thai, etc

    @JsonIgnore // prevent looping issue
    @ManyToMany(mappedBy = "cuisines") // No cascading or orphan removal. We dont want to delete a restaurant when deleting a cuisine
    private Set<Restaurant> restaurants = new HashSet<>();

    public Cuisine() {}

    public Cuisine(String name) {
        this.name = name;
    }

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

    public Set<Restaurant> getRestaurants() {
        return restaurants;
    }

    public void setRestaurants(Set<Restaurant> restaurants) {
        this.restaurants = restaurants;
    }
}
