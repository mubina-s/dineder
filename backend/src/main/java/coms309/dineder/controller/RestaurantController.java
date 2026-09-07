package coms309.dineder.controller;

import coms309.dineder.*;
import coms309.dineder.dto.CreateRestaurantRequest;
import coms309.dineder.dto.RestaurantDTO;
import coms309.dineder.entity.Restaurant;
import coms309.dineder.repository.RestaurantRepository;
import coms309.dineder.service.RestaurantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints for managing restaurants with CRUD operations
 * @author Mason Gliege
 */
@RestController
public class RestaurantController {

    @Autowired
    RestaurantRepository restaurantRepository;

    @Autowired
    RestaurantService restaurantService;

    /**
     * Get a list of all restaurants that are currently in the database
     * @return List (not set) of all restaurants in the database. each restaurant is in dto form.
     */
    @GetMapping("/restaurants")
    List<RestaurantDTO> getaAllRestaurants(){
        return restaurantService.getAllRestaurantsDTO();
    }

    /**
     * Gets an existing restaurant object by its id
     * @param id id value of an existing restaurant
     * @return a restaurant dto if the restaurant exists. Response status exception if not found by that id
     */
    @GetMapping("/restaurants/{id}")
    RestaurantDTO getRestaurantById(@PathVariable Long id){
        return restaurantService.getRestaurantByIdDTO(id);
    }

    /**
     * Create a new restaurant with a CreateRestaurantRequest and saves it to the database with the provided fields. Also needs adminId
     * @param request CreateRestaurantRequest includes the relevant attributes of a restaurant. Also can provide ids of existing cuisines, or an image to link to this restaurant.
     * @param adminId user id of an admin, required for making a new restaurant
     * @return a data transfer object of a new restaurant and its fields. Will return response status exception if cuisine or image not found in database
     */
    @PostMapping("/restaurants")
    RestaurantDTO addRestaurant(@RequestBody CreateRestaurantRequest request, @RequestParam Long adminId){
        if (request == null) return null;
        return restaurantService.createRestaurant(request, adminId);
    }

    /**
     * Update the fields of an existing restaurant by its id (admin only)
     * @param id id of an existing restaurant in the database that will be updated
     * @param request Body for new restaurant fields to insert
     * @param adminId user id of an admin, required for updating a restaurant
     * @return a dto of a restaurant if the restaurant exists. response status exception if not found by the id given
     */
    @PutMapping("/restaurants/{id}")
    RestaurantDTO updateRestaurant(@PathVariable Long id, @RequestBody Restaurant request, @RequestParam Long adminId){
        return restaurantService.updateRestaurant(id, request, adminId);
    }

    /**
     * Delete an existing restaurant in the database by its id (admin only)
     * @param id id of the existing restaurant in the database
     * @param adminId user id of an admin, required for deleting a restaurant
     * @return While not necessary, returns a String of the restaurant name that just got deleted. Response status exception if not found by the id given
     */
    @DeleteMapping("/restaurants/{id}")
    String deleteRestaurant(@PathVariable Long id, @RequestParam Long adminId){
        return restaurantService.deleteRestaurant(id, adminId);
    }
}
