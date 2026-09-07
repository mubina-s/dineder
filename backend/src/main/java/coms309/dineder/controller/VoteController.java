package coms309.dineder.controller;

import coms309.dineder.entity.SessionResult;
import coms309.dineder.dto.*;
import coms309.dineder.service.VoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for Voting Operations.
 * Manages vote casting, vote results, and final session outcomes.
 *
 * @author Malak Mansour
 */
@RestController
@RequestMapping("/api/votes")
@Tag(name = "Voting", description = "Endpoints for managing votes, voting results, and session outcomes")
public class VoteController {

    @Autowired
    private VoteService voteService;

    // -------------------------------------------------------------------------
    @Operation(
            summary = "Cast a vote",
            description = "Allows a user to cast a vote for a restaurant in a specific voting session and round."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Vote cast successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = VoteResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid vote data",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content
            )
    })
    @PostMapping
    public ResponseEntity<VoteResponse> castVote(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Vote data including userId, sessionId, restaurantId, and round",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CastVoteRequest.class)
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody CastVoteRequest request) {
        try {
            VoteResponse vote = voteService.castVote(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(vote);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // -------------------------------------------------------------------------
    @Operation(
            summary = "Get voting results for a session",
            description = "Retrieves aggregated voting results for a specific session and round, including vote counts per restaurant."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Voting results retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = VotingResultsResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Session not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content
            )
    })
    @GetMapping("/session/{sessionId}/results")
    public ResponseEntity<VotingResultsResponse> getVotingResults(
            @Parameter(description = "ID of the voting session", required = true, example = "1")
            @PathVariable Long sessionId,
            @Parameter(description = "Voting round number", example = "1")
            @RequestParam(defaultValue = "1") int round) {
        try {
            VotingResultsResponse results = voteService.getVotingResults(sessionId, round);
            return ResponseEntity.ok(results);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // -------------------------------------------------------------------------
    @Operation(
            summary = "Save final session result",
            description = "Saves the final restaurant choice for a session after voting is complete."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Final result saved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"sessionId\": 1, \"restaurantId\": 5, \"restaurantName\": \"Pizza Palace\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Session or restaurant not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content
            )
    })
    @PostMapping("/session/{sessionId}/final")
    public ResponseEntity<Map<String, Object>> saveFinalResult(
            @Parameter(description = "ID of the voting session", required = true, example = "1")
            @PathVariable Long sessionId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Final restaurant selection containing restaurantId",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"restaurantId\": 5}")
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody Map<String, Long> request) {
        try {
            Long restaurantId = request.get("restaurantId");
            if (restaurantId == null) {
                return ResponseEntity.badRequest().build();
            }

            SessionResult result = voteService.saveFinalResult(sessionId, restaurantId);

            Map<String, Object> response = new HashMap<>();
            response.put("sessionId", result.getSession().getId());
            response.put("restaurantId", result.getRestaurant().getId());
            response.put("restaurantName", result.getRestaurant().getName());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // -------------------------------------------------------------------------
    @Operation(
            summary = "Get final session result",
            description = "Retrieves the final restaurant selection and details for a completed voting session."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Final result retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(example = "{\"sessionId\": 1, \"restaurantId\": 5, \"restaurantName\": \"Pizza Palace\", \"restaurantAddress\": \"123 Main St\", \"restaurantRating\": 4.5, \"restaurantPriceRange\": \"$$\", \"restaurantPhone\": \"555-1234\", \"restaurantWebsite\": \"http://pizzapalace.com\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Session result not found",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error",
                    content = @Content
            )
    })
    @GetMapping("/session/{sessionId}/final")
    public ResponseEntity<Map<String, Object>> getFinalResult(
            @Parameter(description = "ID of the voting session", required = true, example = "1")
            @PathVariable Long sessionId) {
        try {
            SessionResult result = voteService.getFinalResult(sessionId);
            if (result == null) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("sessionId", result.getSession().getId());
            response.put("restaurantId", result.getRestaurant().getId());
            response.put("restaurantName", result.getRestaurant().getName());
            response.put("restaurantAddress", result.getRestaurant().getAddress());
            response.put("restaurantRating", result.getRestaurant().getRating());
            response.put("restaurantPriceRange", result.getRestaurant().getPriceRange());
            response.put("restaurantPhone", result.getRestaurant().getPhone());
            response.put("restaurantWebsite", result.getRestaurant().getWebsite());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // -------------------------------------------------------------------------
    @Operation(
            summary = "Delete a vote by ID",
            description = "Permanently deletes a specific vote by its unique ID."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Vote deleted successfully",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(type = "string", example = "Successfully deleted vote by id: 1")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Vote not found",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(type = "string", example = "Error: vote not found by id: 1")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error deleting vote",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(type = "string", example = "Error deleting vote by id: 1")
                    )
            )
    })
    @DeleteMapping(path = "/{voteId}")
    public String deleteVoteById(
            @Parameter(description = "ID of the vote to delete", required = true, example = "1")
            @PathVariable Long voteId) {
        try {
            voteService.deleteVote(voteId);
            return "Successfully deleted vote by id: " + voteId;
        } catch (ResponseStatusException e) {
            return "Error: vote not found by id: " + voteId;
        } catch (Exception e) {
            return "Error deleting vote by id: " + voteId;
        }
    }

    // -------------------------------------------------------------------------
    @Operation(
            summary = "Delete all votes for a session",
            description = "Deletes all votes associated with a specific session ID. Useful for resetting or clearing a voting session."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "All session votes deleted successfully",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(type = "string", example = "Successfully deleted votes by session id: 1")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Session not found",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(type = "string", example = "Error: session id not found by id: 1")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error deleting votes",
                    content = @Content(
                            mediaType = "text/plain",
                            schema = @Schema(type = "string", example = "Error deleting votes by session id: 1")
                    )
            )
    })
    @DeleteMapping(path = "/session/{sessionId}")
    public String deleteVotesBySessionId(
            @Parameter(description = "ID of the session whose votes should be deleted", required = true, example = "1")
            @PathVariable Long sessionId) {
        try {
            voteService.deleteVotesBySessionId(sessionId);
            return "Successfully deleted votes by session id: " + sessionId;
        } catch (ResponseStatusException e) {
            return "Error: session id not found by id: " + sessionId;
        } catch (Exception e) {
            return "Error deleting votes by session id: " + sessionId;
        }
    }
}