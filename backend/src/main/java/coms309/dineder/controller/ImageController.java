package coms309.dineder.controller;

import coms309.dineder.repository.ImageRepository;
import coms309.dineder.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import coms309.dineder.entity.*;

import java.util.List;

/**
 * Simple endpoints for images
 * @author Mason Gliege
 */
@RestController
public class ImageController {
    @Autowired
    ImageService imageService;

    /**
     * Get all images currently stored in the database (String urls of them, not actual images).
     * @return list of images, including the id and String url for each
     */
    @GetMapping("/images")
    public List<Image> getImages() {
        return imageService.findAll();
    }

    /**
     * Create a new image object and store it in the database. Only need the url
     * @param image image to pass in as a request. only url needed as a String
     * @return return an image object after creation
     */
    @PostMapping("/images")
    public Image createImage(@RequestBody Image image) {
        return imageService.createImage(image);
    }

    /**
     * Delete an image by id. Must be existing in the database to be deleted.
     * @param id image id to delete by
     * @return String indicating success or failure of the deletion, based on if it was found in the database or not.
     */
    @DeleteMapping("/images/{id}")
    public String deleteImage(@PathVariable Long id) {
        try{
            imageService.deleteImage(id);
            return "Image with id " + id + " has been deleted";
        } catch (Exception e){
            return "Image with id " + id + " could not be deleted";
        }
    }
}
