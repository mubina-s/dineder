package coms309.dineder.controller;

import coms309.dineder.entity.AdminLog;
import coms309.dineder.repository.AdminLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AdminController {
    @Autowired
    AdminLogRepository adminLogRepository;

    @GetMapping("/admin/logs")
    public List<AdminLog> getAdminLogs(){
        return adminLogRepository.findAll();
    }
}
