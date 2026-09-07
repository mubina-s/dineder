package coms309.dineder.service;

import coms309.dineder.entity.Cuisine;
import coms309.dineder.entity.Image;
import coms309.dineder.entity.Restaurant;
import coms309.dineder.repository.ImageRepository;
import coms309.dineder.repository.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
/**
 * Service class for image endpoints.
 * Takes the complexity from the controllers and puts it here
 *
 * @author Mason Gliege
 */
@Service
public class ImageService {
    @Autowired
    private ImageRepository imageRepository;
    @Autowired
    private RestaurantRepository restaurantRepository;

    public List<Image> findAll() {
        return imageRepository.findAll();
    }

    public Image createImage(Image image) {
        return imageRepository.save(image);
    }

    public void deleteImage(Long id) {
        Image imageToDelete = imageRepository.findById(id).orElseThrow(() -> new RuntimeException("Image with id " + id + " not found"));
        for (Restaurant restaurant : imageToDelete.getRestaurants()) {
            restaurant.setImage(null);
            restaurantRepository.save(restaurant);
        }
        imageRepository.delete(imageToDelete);
    }

}
