package coms309.dineder.repository;

import coms309.dineder.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import coms309.dineder.entity.Image;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Simple repository for images. can find by url
 * @author Mason Gliege
 */
@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    Optional<Image> findByUrl(String url);
}
