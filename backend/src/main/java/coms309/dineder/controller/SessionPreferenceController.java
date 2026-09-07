package coms309.dineder.controller;

import coms309.dineder.entity.User;
import coms309.dineder.repository.SessionPreferenceRepository;
import coms309.dineder.entity.SessionPreference;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Controller for Session Preferences.
 * Manages saved preferences for dining sessions including ratings and price ranges.
 *
 * @author Malak Mansour
 */
@RestController
@Tag(name = "Session Preferences", description = "Endpoints for managing saved session preferences")
public class SessionPreferenceController {

    @Autowired
    SessionPreferenceRepository sessionPreferenceRepository;

    @Autowired
    UserRepository userRepository;

    private String success = "{\"message\":\"success\"}";
    private String failure = "{\"message\":\"failure\"}";

    // -------------------------------------------------------------------------
    @Operation(
            summary = "Get session preferences by user ID",
            description = "Displays names of all existing saved session preferences for the specified host user. Returns a simplified list with just id and preferenceName for dropdown display."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of session preferences retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Map.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content
            )
    })
    @GetMapping(path = "/sessionprefs/{userID}")
    List<Map<String, Object>> getSessionPrefsByUserId(
            @Parameter(description = "ID of the user (host) whose preferences are retrieved", required = true, example = "1")
            @PathVariable Long userID) {
        List<SessionPreference> prefs = sessionPreferenceRepository.findByHostId(userID);

        // Return simplified list with just id and name for dropdown
        return prefs.stream()
                .map(pref -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", pref.getId());
                    map.put("preferenceName", pref.getPreferenceName());
                    return map;
                })
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    @Operation(
            summary = "Get session preference details by ID",
            description = "When host selects from dropdown list, retrieves full preference details including preferenceName, rating, priceRange, and host information."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Session preference details retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SessionPreference.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Session preference not found",
                    content = @Content
            )
    })
    @GetMapping(path = "/sessionprefs/detail/{prefID}")
    SessionPreference getSessionPrefById(
            @Parameter(description = "ID of the session preference to retrieve", required = true, example = "1")
            @PathVariable Long prefID) {
        return sessionPreferenceRepository.findById(prefID).orElse(null);
    }

    // -------------------------------------------------------------------------
    @Operation(
            summary = "Create a new session preference",
            description = "Creates a new session preference with specified name, rating criteria, price range, and associated host user."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Session preference created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SessionPreference.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data or host not found",
                    content = @Content
            )
    })
    @PostMapping(path = "/sessionprefs")
    SessionPreference createSessionPreference(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Session preference data including preferenceName, rating, priceRange, and hostId",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"preferenceName\": \"Quick Lunch\", \"rating\": \"4+\", \"priceRange\": \"$$\", \"hostId\": 1}")
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody Map<String, Object> request) {
        if (request == null) return null;

        String preferenceName = (String) request.get("preferenceName");
        String rating = (String) request.get("rating");
        String priceRange = (String) request.get("priceRange");
        Integer hostIdInteger = (Integer) request.get("hostId");
        Long hostId = hostIdInteger.longValue();
        if (hostId == null) return null;

        User host = userRepository.findById(hostId).orElse(null);
        if (host == null) return null;

        SessionPreference pref = new SessionPreference(preferenceName, rating, priceRange, host);
        return sessionPreferenceRepository.save(pref);
    }

    // -------------------------------------------------------------------------
    @Operation(
            summary = "Update an existing session preference",
            description = "Updates one or more fields of an existing session preference. Only provided fields will be updated."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Session preference updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SessionPreference.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Session preference not found",
                    content = @Content
            )
    })
    @PutMapping(path = "/sessionprefs/{prefID}")
    SessionPreference updateSessionPreference(
            @Parameter(description = "ID of the session preference to update", required = true, example = "1")
            @PathVariable Long prefID,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated fields (preferenceName, rating, priceRange, hostId). Only include fields to update.",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"preferenceName\": \"Updated Name\", \"rating\": \"5\"}")
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody Map<String, Object> request) {
        SessionPreference pref = sessionPreferenceRepository.findById(prefID).orElse(null);
        if (pref == null) return null;

        if (request.containsKey("preferenceName")) {
            pref.setPreferenceName((String) request.get("preferenceName"));
        }
        if (request.containsKey("rating")) {
            pref.setRating((String) request.get("rating"));
        }
        if (request.containsKey("priceRange")) {
            pref.setPriceRange((String) request.get("priceRange"));
        }
        if (request.containsKey("hostId")) {
            Integer hostIdInteger = (Integer) request.get("hostId");
            Long hostId = hostIdInteger.longValue();
            User host = userRepository.findById(hostId).orElse(null);
            if (host != null) {
                pref.setHost(host);
            }
        }

        sessionPreferenceRepository.save(pref);
        return sessionPreferenceRepository.findById(prefID).orElse(null);
    }

    // -------------------------------------------------------------------------
    @Operation(
            summary = "Delete a session preference",
            description = "Permanently deletes a session preference by its ID. Returns the name of the deleted preference if successful."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Session preference deleted successfully. Returns the name of the deleted preference.",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(type = "string", example = "Quick Lunch")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Session preference not found (returns null)",
                    content = @Content
            )
    })
    @DeleteMapping(path = "/sessionprefs/{prefID}")
    String deleteSessionPreference(
            @Parameter(description = "ID of the session preference to delete", required = true, example = "1")
            @PathVariable Long prefID) {
        String preferenceName = null;
        SessionPreference pref;

        if (sessionPreferenceRepository.existsById(prefID)) {
            pref = sessionPreferenceRepository.findById(prefID).orElse(null);
            preferenceName = pref.getPreferenceName();
            sessionPreferenceRepository.deleteById(prefID);
        }

        return preferenceName;
    }
}