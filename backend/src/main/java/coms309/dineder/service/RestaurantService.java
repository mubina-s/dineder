package coms309.dineder.service;

import coms309.dineder.dto.CreateRestaurantRequest;
import coms309.dineder.dto.RestaurantDTO;
import coms309.dineder.entity.*;
import coms309.dineder.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

/**
 * Service class for restaurant endpoints.
 * Takes the complexity from the controllers and puts it here
 *
 * @author Mason Gliege
 */
@Service
public class RestaurantService {
    @Autowired
    RestaurantRepository restaurantRepository;

    @Autowired
    CuisineRepository cuisineRepository;
    @Autowired
    ImageRepository imageRepository;
    @Autowired
    SessionRepository sessionRepository;
    @Autowired
    SessionResultRepository sessionResultRepository;

    // Repositories used for admin feature
    @Autowired
    UserRepository userRepository;
    @Autowired
    AdminLogRepository adminLogRepository;

    /**
     * Helper method to verify admin status of a user
     * @param userId user id of admin
     * @return String username of the admin
     */
    private String verifyAdmin(Long userId) {
        // Check that user id is not null
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "ID needed");
        }
        // Check that a user exists
        User user = userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id "+userId));
        // Check if admin
        if (user.getIsAdmin() != true){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can perform this action");
        }
        return user.getUsername();
    }

    /**
     * Filters restaurants by a session preference. Can filter by rating and price range
     * @param preference preference object to filter by
     * @return list of restaurants
     */
    @Transactional
    public List<Restaurant> findRestaurantsByPreferences(SessionPreference preference) {
        // Clean rating
        // Default rating of -1 for no rating filter
        int rating = -1;
        // Parse int if rating is not N/A or null
        if (preference.getRating() != null && !preference.getRating().equals("")) {
            try{
                rating = Integer.parseInt(preference.getRating());
            } catch (NumberFormatException e){
                rating = -1;
            }
        }
        // Clean price range
        String price;
        if (preference.getPriceRange() == null || preference.getPriceRange().isEmpty() || preference.getPriceRange().equals("")) {
            price = "N/A";
        } else {
            price = preference.getPriceRange();
        }


        List<Restaurant> allRestaurants = restaurantRepository.findAll();

        List<Restaurant> filteredRestaurants = new ArrayList<>();
        // Check every restaurant
        for (Restaurant restaurant : allRestaurants) {
            // Check rating
            boolean ratingFound = false;
            if (rating == -1) {
                ratingFound = true;
            } else if (restaurant.getRating() >= rating) {
                ratingFound = true;
            }
            // Check price
            boolean priceFound = false;
            if (price.equals("N/A")) {
                priceFound = true;
            } else if (restaurant.getPriceRange().equals(price)) {
                priceFound = true;
            }
            // Add if both match (are true)
            if (ratingFound && priceFound) {
                filteredRestaurants.add(restaurant);
            }
        }

        return filteredRestaurants;
    }

    /**
     * Returns all restaurants as dtos
     * @return
     */
    @Transactional
    public List<RestaurantDTO> getAllRestaurantsDTO() {
        List<Restaurant> allRestaurants = restaurantRepository.findAll();
        List<RestaurantDTO> allRestaurantDTOs = new ArrayList<>();
        for (Restaurant restaurant : allRestaurants) {
            allRestaurantDTOs.add(convertRestaurantToDTO(restaurant));
        }
        return allRestaurantDTOs;
    }

    /**
     * Gets a restaurant by id and returns a restaurant dto
     * @param id
     * @return
     */
    @Transactional
    public RestaurantDTO getRestaurantByIdDTO(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant not found"));
        return convertRestaurantToDTO(restaurant);
    }

    /**
     * Saves and returns a dto of a new restaurant
     * @param request create restaurant request
     * @param userId id of admin user (for checking)
     * @return restaurant dto
     */
    @Transactional
    public RestaurantDTO createRestaurant(CreateRestaurantRequest request, Long userId) {
        // Verify the admin status
        String username = verifyAdmin(userId);
        // no null requests allowed
        if (request == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Restaurant request is null");
        }
        // Create restaurant with given attributes
        Restaurant restaurant = new Restaurant();
        restaurant.setName(request.getName());
        restaurant.setRating(request.getRating());
        restaurant.setPriceRange(request.getPriceRange());
        restaurant.setAddress(request.getAddress());
        restaurant.setPhone(request.getPhone());
        restaurant.setWebsite(request.getWebsite());
        // Find image by id and set image if provided
        if (request.getImageId() != null){
            Image image = imageRepository.findById(request.getImageId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found"));
            restaurant.setImage(image);
        }
        // If provided and not empty, find cuisines and add them to this restaurant
        if (request.getCuisineIds() != null && !request.getCuisineIds().isEmpty()){
            Set<Cuisine> cuisines = new HashSet<>();
            for (Long cuisineId : request.getCuisineIds()) {
                Cuisine cuisine = cuisineRepository.findById(cuisineId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cuisine not found"));
                cuisines.add(cuisine);
            }
            restaurant.setCuisines(cuisines);
        }

        Restaurant savedRestaurant = restaurantRepository.save(restaurant);

        // Log the action
        adminLogRepository.save(new AdminLog(userId, "CREATE_RESTAURANT", "Admin "+username+", id "+userId+" created restaurant id "+savedRestaurant.getId()+" with name "+savedRestaurant.getName()+"."));
        return getRestaurantByIdDTO(savedRestaurant.getId());
    }

    /**
     * Updates an existing restaurant, saves it, and returns a dto of it
     * @param id restaurant to update
     * @param request restaurant request with new fields
     * @param userId id of admin user (for checking)
     * @return dto of updated restaurant
     */
    @Transactional
    public RestaurantDTO updateRestaurant(Long id, Restaurant request, Long userId) {
        //Verify admin
        String username = verifyAdmin(userId);

        Restaurant restaurant = restaurantRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant not found"));
        int updatedFieldCount = 0;
        if (!Objects.equals(restaurant.getName(), request.getName())) {
            restaurant.setName(request.getName());
            updatedFieldCount++;
        }
        if (!Objects.equals(restaurant.getAddress(), request.getAddress())) {
            restaurant.setAddress(request.getAddress());
            updatedFieldCount++;
        }
        if (restaurant.getRating() != (request.getRating())){
            restaurant.setRating(request.getRating());
            updatedFieldCount++;
        }
        if (!Objects.equals(restaurant.getPriceRange(), request.getPriceRange())) {
            restaurant.setPriceRange(request.getPriceRange());
            updatedFieldCount++;
        }
        if (!Objects.equals(restaurant.getPhone(), request.getPhone())) {
            restaurant.setPhone(request.getPhone());
            updatedFieldCount++;
        }
        if (!Objects.equals(restaurant.getWebsite(), request.getWebsite())) {
            restaurant.setWebsite(request.getWebsite());
            updatedFieldCount++;
        }
        Restaurant updatedRestaurant = restaurantRepository.save(restaurant);
        adminLogRepository.save(new AdminLog(userId, "UPDATE_RESTAURANT", "Admin "+username+", id "+userId+" updated restaurant id "+updatedRestaurant.getId()+" with (new) name "+ updatedRestaurant.getName()+ ". Updated "+updatedFieldCount+" fields."));
        return convertRestaurantToDTO(updatedRestaurant);
    }

    /**
     * Delete a restaurant by id
     * @param id id to delete by
     * @return String of name of deleted restaurant
     */
    @Transactional
    public String deleteRestaurant(Long id, Long userId) {
        // Verify admin
        String username = verifyAdmin(userId);

        Restaurant restaurant = restaurantRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Restaurant not found"));
        String name = restaurant.getName();

        // Break link from session result
        sessionResultRepository.deleteByRestaurant(restaurant);

        //Unlink cuisines
        restaurant.setCuisines(new HashSet<>());

        //Unlink sessions
        List<Session> sessions = sessionRepository.findAllByRestaurants(restaurant);
        for (Session session : sessions) {
            session.getRestaurants().remove(restaurant);
        }

        //Remove restaurant
        restaurantRepository.delete(restaurant);
        //NOTE: we don't need to worry about votes because they're all deleted by cascadetype.all

        // Log the deletion
        adminLogRepository.save(new AdminLog(userId, "DELETE_RESTAURANT", "Admin "+username+", id "+userId+" deleted restaurant id "+id+" with name "+name+"."));

        return name;
    }

    /**
     * Convert restaurant to DTO
     * @param restaurant original restaurant
     * @return restaurant to DTO
     */
    private RestaurantDTO convertRestaurantToDTO(Restaurant restaurant) {
        RestaurantDTO restaurantDTO = new RestaurantDTO();
        restaurantDTO.setId(restaurant.getId());
        restaurantDTO.setName(restaurant.getName());
        restaurantDTO.setAddress(restaurant.getAddress());
        restaurantDTO.setRating(restaurant.getRating());
        restaurantDTO.setPriceRange(restaurant.getPriceRange());
        restaurantDTO.setPhone(restaurant.getPhone());
        restaurantDTO.setWebsite(restaurant.getWebsite());
        Set<String> cuisineNames = new HashSet<>();
        for (Cuisine cuisine : restaurant.getCuisines()) {
            cuisineNames.add(cuisine.getName());
        }
        restaurantDTO.setCuisines(cuisineNames);
        if (restaurant.getImage()!=null) {
            restaurantDTO.setImageUrl(restaurant.getImage().getUrl());
        }
        return restaurantDTO;

    }
}
