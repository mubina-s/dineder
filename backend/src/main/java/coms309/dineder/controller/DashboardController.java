package coms309.dineder.controller;

import coms309.dineder.dto.DashboardDTO;
import coms309.dineder.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller that serves the purpose of updating the dashboard with a restaurant leaderboard and statistics.
 * No database modifications can be done with this controller.
 * @author Mason Gliege
 */
@RestController
public class DashboardController {
    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/dashboard")
    public DashboardDTO getDashboard() {
        DashboardDTO dashboard = new DashboardDTO();
        return dashboardService.generateDashboardDTO();
    }
}
