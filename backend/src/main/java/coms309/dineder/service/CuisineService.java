package coms309.dineder.service;

import coms309.dineder.entity.Cuisine;
import coms309.dineder.entity.Restaurant;
import coms309.dineder.repository.CuisineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class for cuisine endpoints.
 * Takes the complexity from the controllers and puts it here
 *
 * @author Mason Gliege
 */
@Service
public class CuisineService {
    @Autowired
    private CuisineRepository cuisineRepository;

    public List<Cuisine> getAllCuisines() {
        return cuisineRepository.findAll();
    }

    public Cuisine createCuisine(Cuisine cuisine) {
        return cuisineRepository.save(cuisine);
    }

    public void deleteCuisine(Long id) {
        Cuisine cuisineToDelete = cuisineRepository.findById(id).orElseThrow(() -> new RuntimeException("Cuisine with id " + id + " not found"));
        for (Restaurant restaurant : cuisineToDelete.getRestaurants()) {
            restaurant.getCuisines().remove(cuisineToDelete);
        }
        cuisineRepository.delete(cuisineToDelete);
    }
}
