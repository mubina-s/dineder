package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BufferBeforeTopChoiceActivity extends AppCompatActivity {

    private Long sessionId;
    private static final String TAG = "BufferBeforeTopChoice";
    private VotingWebSocketManager wsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buffer_before_top_choice);

        sessionId = getIntent().getLongExtra("sessionId", -1);

        wsManager = VotingWebSocketManager.getInstance();
        wsManager.connect(sessionId);

        // Listen for finalized voting results
        wsManager.addListener(message -> {
            try {
                JSONObject obj = new JSONObject(message);
                String action = obj.optString("action");
                if (action == null || action.isEmpty()) {
                    action = obj.optString("type");
                }

                if ("VOTING_FINALIZED".equals(action)) {
                    JSONObject results = obj.getJSONObject("results");
                    JSONArray voteResults = results.getJSONArray("results");

                    if (voteResults.length() > 0) {
                        // Backend sorts descending, so index 0 is the winner
                        JSONObject topRestaurant = voteResults.getJSONObject(0);
                        String winnerName = topRestaurant.getString("restaurantName");
                        Long winnerId = topRestaurant.getLong("restaurantId");

                        // Lookup extra attributes from SessionManager
                        String winnerWebsite = null;
                        String winnerPhone = null;
                        Session currentSession = SessionManager.getSession();
                        if (currentSession != null && currentSession.getRestaurantList() != null) {
                            for (Restaurant r : currentSession.getRestaurantList()) {
                                if (r.id.equals(winnerId)) {
                                    winnerWebsite = r.website;
                                    winnerPhone = r.phone;
                                    break;
                                }
                            }
                        }

                        String finalWinnerWebsite = winnerWebsite;
                        String finalWinnerPhone = winnerPhone;
                        runOnUiThread(() -> {
                            Intent intent = new Intent(BufferBeforeTopChoiceActivity.this, TopChoiceActivity.class);
                            intent.putExtra("restaurantId", winnerId);
                            intent.putExtra("restaurantName", winnerName); // optional, for immediate display
                            startActivity(intent);
                            finish();
                        });

                    }
                }
            } catch (JSONException e) {
                Log.e(TAG, "JSON parsing error: " + e.getMessage());
            }
        });
    }
}
