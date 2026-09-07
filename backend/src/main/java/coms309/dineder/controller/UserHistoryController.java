package coms309.dineder.controller;

import coms309.dineder.dto.UserHistoryDTO;
import coms309.dineder.dto.UserStatsDTO;
import coms309.dineder.service.UserHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserHistoryController {

    private final UserHistoryService userHistoryService;

    @Autowired
    public UserHistoryController(UserHistoryService userHistoryService) {
        this.userHistoryService = userHistoryService;
    }

    // GET /user/{id}/history
    @GetMapping("/{id}/history")
    public ResponseEntity<UserHistoryDTO> getUserHistory(@PathVariable("id") Long userId) {
        UserHistoryDTO history = userHistoryService.getUserHistory(userId);
        if (history == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(history);
    }

    // GET /user/{id}/stats
    @GetMapping("/{id}/stats")
    public ResponseEntity<UserStatsDTO> getUserStats(@PathVariable("id") Long userId) {
        UserStatsDTO stats = userHistoryService.getUserStats(userId);
        if (stats == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(stats);
    }

}
