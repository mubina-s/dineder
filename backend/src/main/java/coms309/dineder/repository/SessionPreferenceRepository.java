package coms309.dineder.repository;

import coms309.dineder.entity.SessionPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionPreferenceRepository extends JpaRepository<SessionPreference, Long> {


    // Find all session preferences for a specific host user
    List<SessionPreference> findByHostId(Long hostId);

    boolean existsById(Long id);

    void deleteById(Long id);
}