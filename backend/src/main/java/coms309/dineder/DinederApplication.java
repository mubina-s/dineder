package coms309.dineder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for DineDer application
 */
@SpringBootApplication
public class DinederApplication {
    public static void main(String[] args) {
        SpringApplication.run(DinederApplication.class, args);  // ← .class not .java
    }
}