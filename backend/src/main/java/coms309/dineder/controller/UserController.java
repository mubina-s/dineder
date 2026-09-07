package coms309.dineder.controller;

import coms309.dineder.entity.User;
import coms309.dineder.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for User Management.
 * Handles user CRUD operations, authentication (login/signup), and user queries.
 *
 * @author Malak Mansour
 */
@RestController
@Tag(name = "Users", description = "Endpoints for user management, authentication, and profile operations")
public class UserController {

    @Autowired
    UserRepository userRepository;

    private String success = "{\"message\":\"success\"}";
    private String failure = "{\"message\":\"failure\"}";

    // -------------------------------------------------------------------------
    @Operation(
            summary = "Get all users",
            description = "Retrieves a complete list of all registered users in the system."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of users retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = User.class))
                    )
            )
    })
    @GetMapping(path = "/users")
    List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // -------------------------------------------------------------------------
    @Operation(
            summary = "Get total user count",
            description = "Returns the total number of registered users in the system."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User count retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "integer", example = "150")
                    )
            )
    })
    @GetMapping(path = "/users/count")
    int getUserCount() {
        return (int) userRepository.count();
    }

    // -------------------------------------------------------------------------
    @Operation(
            summary = "Get user by ID",
            description = "Retrieves detailed information for a specific user by their unique ID."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User found and retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = User.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found (returns null)",
                    content = @Content
            )
    })
    @GetMapping(path = "/users/{id}")
    User getUserById(
            @Parameter(description = "ID of the user to retrieve", required = true, example = "1")
            @PathVariable Long id) {
        return userRepository.findById(id).orElse(null);
    }

    // -------------------------------------------------------------------------
    @Operation(
            summary = "User signup",
            description = "Creates a new user account with provided credentials and profile information."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User account created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = User.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid user data provided (returns null)",
                    content = @Content
            )
    })
    @PostMapping(path = "/users/signup")
    User signup(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User registration data including name, email, username, and password",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = User.class)
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody User user) {
        if (user == null) return null;
        return userRepository.save(user);
    }

    // -------------------------------------------------------------------------
    @Operation(
            summary = "User login",
            description = "Authenticates a user with username and password. Returns full user object if credentials match, null otherwise."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful - returns user object",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = User.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials (returns null)",
                    content = @Content
            )
    })
    @PostMapping(path = "/users/login")
    User login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Login credentials containing username and password",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"username\": \"john_doe\", \"password\": \"password123\"}")
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody User loginRequest) {
        if (loginRequest == null) return null;
        User user = userRepository.findByUsernameAndPassword(
                loginRequest.getUsername(),
                loginRequest.getPassword()
        );
        return user; // returns full user object if match, null if not found
    }

    // -------------------------------------------------------------------------
    @Operation(
            summary = "Create a new user (alternative endpoint)",
            description = "Creates a new user and returns a success/failure message. Alternative to /users/signup."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "string", example = "{\"message\":\"success\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Failed to create user",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = "string", example = "{\"message\":\"failure\"}")
                    )
            )
    })
    @PostMapping(path = "/users")
    String createUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User data including name, email, username, and password",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = User.class)
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody User user) {
        if (user == null)
            return failure;
        userRepository.save(user);
        return success;
    }

    // -------------------------------------------------------------------------
    @Operation(
            summary = "Update user information",
            description = "Updates an existing user's profile information including name, email, username, and password."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = User.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found (returns null)",
                    content = @Content
            )
    })
    @PutMapping("/users/{id}")
    User updateUser(
            @Parameter(description = "ID of the user to update", required = true, example = "1")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated user information",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = User.class)
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody User request) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null)
            return null;
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setUsername(request.getUsername());
        userRepository.save(user);
        return userRepository.findById(id).orElse(null);
    }

    // -------------------------------------------------------------------------
    @Operation(
            summary = "Delete a user",
            description = "Permanently deletes a user account by ID. Returns the username of the deleted user if successful."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User deleted successfully. Returns username of deleted user.",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(type = "string", example = "john_doe")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found (returns null)",
                    content = @Content
            )
    })
    @DeleteMapping(path = "/users/{id}")
    String deleteUser(
            @Parameter(description = "ID of the user to delete", required = true, example = "1")
            @PathVariable Long id) {
        String username = null;
        User user;
        if (userRepository.existsById(id)) {
            user = userRepository.findById(id).orElse(null);
            username = user.getUsername();
            userRepository.deleteById(id);
        }
        return username;
    }
}