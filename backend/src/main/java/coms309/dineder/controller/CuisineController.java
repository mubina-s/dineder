package coms309.dineder.controller;

import coms309.dineder.entity.Cuisine;
import coms309.dineder.service.CuisineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import coms309.dineder.repository.*;

import java.util.List;

/**
 * Simple endpoints for cuisines. Only getters are used by a client at this time.
 * @author Mason Gliege
 */
@RestController
public class CuisineController {

    @Autowired
    private CuisineService cuisineService;

    /**
     * Return a list of all cuisines stored in the database
     * @return List of all cuisines stored in the database. Only has id and name for each cuisine
     */
    @GetMapping("/cuisines")
    public List<Cuisine> getCuisines() {
        return cuisineService.getAllCuisines();
    }

    /**
     * Create a new cuisine. Only name is needed, id will be generated.
     * @param cuisine cuisine name
     * @return return cuisine saved
     */
    @PostMapping("/cuisines")
    public Cuisine createCuisine(@RequestBody Cuisine cuisine) {
        return cuisineService.createCuisine(cuisine);
    }

    /**
     * Delete an existing cuisine in the database by its id, if it exists. Clients do not need to use this, just for admin testing
     * @param id Id of an existing cuisine to deleted.
     * @return String indicating the status, if the cuisine by that id was deleted or not.
     */
    @DeleteMapping("/cuisines/{id}")
    public String deleteCuisine(@PathVariable Long id) {
        try{
            cuisineService.deleteCuisine(id);
            return "Cuisine with id " + id + " deleted";
        } catch (Exception e){
            return "Cuisine with id " + id + " not found";
        }
    }
}
