package coms309.dineder.repository;

import coms309.dineder.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import jakarta.persistence.*;

import java.util.Optional;

/**
 * Simple user repository
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);



    @Transactional
    void deleteById(Long id);

    // Login
    User findByUsernameAndPassword(String username, String password);
}