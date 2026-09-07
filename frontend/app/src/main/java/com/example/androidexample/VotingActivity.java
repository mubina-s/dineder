package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class VotingActivity extends AppCompatActivity {

    private Button restaurant1Btn;
    private Button restaurant2Btn;
    private Button restaurant3Btn; // optional third button

    private VotingWebSocketManager wsManager;
    private Long sessionParticipantId;
    private Long sessionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voting);

        Intent intent = getIntent();
        sessionParticipantId = intent.getLongExtra("sessionParId", -1);
        sessionId = intent.getLongExtra("sessionId", -1);

        restaurant1Btn = findViewById(R.id.restaurant1_btn);
        restaurant2Btn = findViewById(R.id.restaurant2_btn);
        restaurant3Btn = findViewById(R.id.restaurant3_btn);

        // Set button texts from intent extras
        restaurant1Btn.setText(intent.getStringExtra("restaurant1Name"));
        restaurant2Btn.setText(intent.getStringExtra("restaurant2Name"));

        // Only set text if provided (for 3-way tie case)
        if (intent.hasExtra("restaurant3Name")) {
            restaurant3Btn.setVisibility(View.VISIBLE);
            restaurant3Btn.setText(intent.getStringExtra("restaurant3Name"));
        } else {
            restaurant3Btn.setVisibility(View.GONE);
        }

        // Initialize WebSocket manager
        wsManager = VotingWebSocketManager.getInstance();
        wsManager.connect(sessionId);

        // Listener for restaurant 1
        restaurant1Btn.setOnClickListener(v -> {
            handleTiebreakVote(intent.getLongExtra("restaurant1Id", -1));
        });

        // Listener for restaurant 2
        restaurant2Btn.setOnClickListener(v -> {
            handleTiebreakVote(intent.getLongExtra("restaurant2Id", -1));
        });

        // Listener for restaurant 3 (only if visible)
        restaurant3Btn.setOnClickListener(v -> {
            handleTiebreakVote(intent.getLongExtra("restaurant3Id", -1));
        });

        // ✅ Listen for VOTING_FINALIZED broadcast from backend
        wsManager.addListener(message -> {
            try {
                JSONObject obj = new JSONObject(message);
                String action = obj.optString("action");
                if (action == null || action.isEmpty()) {
                    action = obj.optString("type");
                }

                if ("VOTING_FINALIZED".equals(action)) {
                    Log.d("VotingActivity", "Received message: " + message);
                    JSONObject resultsObj = obj.getJSONObject("results");
                    JSONArray restaurants = resultsObj.getJSONArray("results");

                    // Use the explicit winningRestaurantId field
                    long winnerId = resultsObj.getLong("winningRestaurantId");
                    String winnerName = "";

                    for (int i = 0; i < restaurants.length(); i++) {
                        JSONObject r = restaurants.getJSONObject(i);
                        if (r.getLong("restaurantId") == winnerId) {
                            winnerName = r.getString("restaurantName");
                            break;
                        }
                    }

                    // Declare a final copy for use inside the lambda
                    final String finalWinnerName = winnerName;

                    runOnUiThread(() -> {
                        Intent topIntent = new Intent(VotingActivity.this, TopChoiceActivity.class);
                        topIntent.putExtra("sessionId", sessionId);
                        topIntent.putExtra("sessionParId", sessionParticipantId);
                        topIntent.putExtra("restaurantId", winnerId);
                        topIntent.putExtra("restaurantName", finalWinnerName);
                        startActivity(topIntent);
                        finish();
                    });

                }
                } catch (JSONException e) {
                Log.e("VotingActivity", "JSON parsing error", e);
            }
        });
    }

    /**
     * Host casts a tiebreaker vote → send CAST_VOTE first, then FINALIZE_VOTING.
     * Transition to TopChoiceActivity happens only after backend broadcasts VOTING_FINALIZED.
     */
    private void handleTiebreakVote(long restaurantId) {
        if (restaurantId == -1) return;

        // Step 1: Host casts a vote for chosen restaurant
        wsManager.sendVote(sessionId, sessionParticipantId, restaurantId, 1, 1);
        Log.d("VotingActivity", "Host cast tiebreaker vote for restaurant " + restaurantId);

        // Step 2: Finalize voting
        wsManager.finalizeVoting(sessionId, restaurantId);
        Log.d("VotingActivity", "Host sent FINALIZE_VOTING for restaurant " + restaurantId);

        // ✅ Still wait for VOTING_FINALIZED broadcast before transitioning
    }

}
