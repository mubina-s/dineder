package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

public class FinalResultsActivity extends AppCompatActivity {

    ProgressBar[] progressBars = new ProgressBar[3];
    TextView[] percentages = new TextView[3];
    TextView[] labels = new TextView[3];
    Long[] restaurantIds = new Long[3];
    private VotingWebSocketManager wsManager;

    private boolean votingFinalizedReceived = false;
    private boolean votingCompleteReceived = false;

    private boolean navigated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final_results);

        Long sessionParticipantId = SessionParticipantManager.getSessionPar().getParticipantId();
        Long sessionId = SessionManager.getSession().getSessionId();
        int round = 1;

        // Bind UI
        progressBars[0] = findViewById(R.id.progressBar1);
        progressBars[1] = findViewById(R.id.progressBar2);
        progressBars[2] = findViewById(R.id.progressBar3);

        percentages[0] = findViewById(R.id.percentage1);
        percentages[1] = findViewById(R.id.percentage2);
        percentages[2] = findViewById(R.id.percentage3);

        labels[0] = findViewById(R.id.restaurant1Label);
        labels[1] = findViewById(R.id.restaurant2Label);
        labels[2] = findViewById(R.id.restaurant3Label);

        // --- Step 1: WebSocket for live updates ---
        wsManager = VotingWebSocketManager.getInstance();
        wsManager.connect(sessionId);

        wsManager.addListener(message -> {
            try {
                JSONObject obj = new JSONObject(message);
                String action = obj.optString("action");
                if (action == null || action.isEmpty()) {
                    action = obj.optString("type"); // tolerant parsing
                }
                Log.d("FinalResultsActivity", "Parsed action: " + action);

                if ("VOTING_UPDATE".equals(action)) {
                    JSONObject resultsObj = obj.getJSONObject("results");
                    JSONArray restaurants = resultsObj.getJSONArray("results");

                    for (int i = 0; i < Math.min(3, restaurants.length()); i++) {
                        JSONObject restaurant = restaurants.getJSONObject(i);
                        Long restaurantId = restaurant.getLong("restaurantId");
                        String name = restaurant.getString("restaurantName");
                        double percentage = restaurant.getDouble("percentage");

                        restaurantIds[i] = restaurantId;

                        final int index = i;
                        runOnUiThread(() -> {
                            labels[index].setText(name);
                            percentages[index].setText(String.format("%.1f%%", percentage));
                            progressBars[index].setProgress((int) percentage);
                        });
                    }

                    // No navigation here — just UI update
                    if (resultsObj.optBoolean("hasWinner")) {
                        try {
                            long winnerId = resultsObj.getLong("winningRestaurantId");
                            String winnerName = "";
                            for (int i = 0; i < restaurants.length(); i++) {
                                JSONObject r = restaurants.getJSONObject(i);
                                if (r.getLong("restaurantId") == winnerId) {
                                    winnerName = r.getString("restaurantName");
                                    break;
                                }
                            }
                            Log.d("FinalResultsActivity", "Winner detected in VOTING_UPDATE: " + winnerName);
                            // Do nothing else — wait for VOTING_FINALIZED
                        } catch (JSONException e) {
                            Log.e("FinalResultsActivity", "Error extracting winner from VOTING_UPDATE", e);
                        }
                    }

                } else if ("VOTING_COMPLETE".equals(action)) {
                    Log.d("FinalResultsActivity", "Received VOTING_COMPLETE: " + obj.toString());
                    votingCompleteReceived = true;

                    JSONObject resultsObj = obj.getJSONObject("results");
                    JSONArray restaurants = resultsObj.getJSONArray("results");

                    boolean isHost = Objects.equals(
                            SessionParticipantManager.getSessionPar().getUserId(),
                            SessionManager.getSession().getHostId()
                    );

                    int n = restaurants.length();
                    double epsilon = 0.5; // loosen tie detection
                    double[] percents = new double[n];
                    for (int i = 0; i < n; i++) {
                        percents[i] = restaurants.getJSONObject(i).getDouble("percentage");
                    }

                    boolean tieTopTwo = (n >= 2) && Math.abs(percents[0] - percents[1]) <= epsilon;
                    boolean allThreeTie = (n == 3) &&
                            Math.abs(percents[0] - percents[1]) <= epsilon &&
                            Math.abs(percents[1] - percents[2]) <= epsilon;

                    runOnUiThread(() -> {
                        if (allThreeTie) {
                            if (isHost) {
                                launchVotingActivityForTie(sessionId, sessionParticipantId, restaurants, new int[]{0, 1, 2});
                            } else {
                                launchBuffer(sessionId, sessionParticipantId);
                            }
                        } else if (tieTopTwo) {
                            if (isHost) {
                                launchVotingActivityForTie(sessionId, sessionParticipantId, restaurants, new int[]{0, 1});
                            } else {
                                launchBuffer(sessionId, sessionParticipantId);
                            }
                        } else {
                            // no tie → pick the highest percent
                            if (isHost) {
                                int winningIndex = 0;
                                double maxPercent = percents[0];
                                for (int i = 1; i < n; i++) {
                                    if (percents[i] > maxPercent) {
                                        maxPercent = percents[i];
                                        winningIndex = i;
                                    }
                                }
                                try {
                                    long winningRestaurantId = restaurants.getJSONObject(winningIndex).getLong("restaurantId");
                                    wsManager.finalizeVoting(sessionId, winningRestaurantId);
                                    Log.d("FinalResultsActivity", "Host sent FINALIZE_VOTING for restaurant " + winningRestaurantId);
                                } catch (JSONException e) {
                                    Log.e("FinalResultsActivity", "Finalize error", e);
                                }
                            }
                        }

                        checkFinalResult(sessionId, sessionParticipantId);
                    });

                }

                else if ("VOTING_FINALIZED".equals(action)) {
                    Log.d("FinalResultsActivity", "Received VOTING_FINALIZED: " + obj.toString());
                    votingFinalizedReceived = true; // mark that we got it

                    JSONObject resultsObj = obj.getJSONObject("results");
                    JSONArray restaurants = resultsObj.getJSONArray("results");

                    // ✅ Use the explicit winningRestaurantId from backend
                    long winnerId = resultsObj.getLong("winningRestaurantId");
                    String winnerName = "";

                    // Find the matching restaurant name in the array
                    for (int i = 0; i < restaurants.length(); i++) {
                        JSONObject r = restaurants.getJSONObject(i);
                        if (r.getLong("restaurantId") == winnerId) {
                            winnerName = r.getString("restaurantName");
                            break;
                        }
                    }

                    final String finalWinnerName = winnerName;
                    if (!navigated && !isFinishing()) {
                        navigated = true;
                        runOnUiThread(() -> {
                            Log.d("FinalResultsActivity", "Navigating to TopChoiceActivity for winner " + finalWinnerName);
                            Intent topIntent = new Intent(FinalResultsActivity.this, TopChoiceActivity.class);
                            topIntent.putExtra("sessionId", sessionId);
                            topIntent.putExtra("sessionParId", sessionParticipantId);
                            topIntent.putExtra("restaurantId", winnerId);
                            topIntent.putExtra("restaurantName", finalWinnerName);
                            startActivity(topIntent);
                            finish(); // ensure FinalResultsActivity closes for both host and participants
                        });
                    }
                } else if ("ERROR".equals(action)) {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Error: " + obj.optString("message"), Toast.LENGTH_SHORT).show()
                    );
                }
            } catch (JSONException e) {
                Log.e("FinalResultsActivity", "JSON parsing error", e);
            }
        });

        // Request current results again via WS (sync)
        wsManager.getResults(sessionId, round);

        // --- Step 2: Volley GET for initial snapshot ---
        String getUrl = "http://10.0.2.2:8080/api/votes/session/"
                + sessionId + "/results?round=" + round;

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest getRequest = new JsonObjectRequest(
                com.android.volley.Request.Method.GET,
                getUrl,
                null,
                response -> {
                    try {
                        JSONArray resultsArray = response.getJSONArray("results");
                        for (int i = 0; i < resultsArray.length() && i < 3; i++) {
                            JSONObject restaurantVote = resultsArray.getJSONObject(i);
                            int restaurantId = restaurantVote.getInt("restaurantId");
                            String restaurantName = restaurantVote.getString("restaurantName");
                            double percentage = restaurantVote.getDouble("percentage");

                            labels[i].setText(restaurantName);
                            percentages[i].setText(String.format("%.1f%%", percentage));
                            progressBars[i].setProgress((int) percentage);
                            restaurantIds[i] = (long) restaurantId;
                        }
                    } catch (JSONException e) {
                        Log.e("Volley", "JSON parsing error", e);
                    }
                },
                error -> Log.e("Volley", "Error fetching results", error)
        );
        queue.add(getRequest);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!navigated) {
            checkFinalResult(SessionManager.getSession().getSessionId(), SessionParticipantManager.getSessionPar().getParticipantId());
        }
    }

    private void checkFinalResult(Long sessionId, Long sessionParticipantId) {
        String url = "http://10.0.2.2:8080/api/votes/session/" + sessionId + "/final";
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest request = new JsonObjectRequest(
                com.android.volley.Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        long winnerId = response.getLong("restaurantId");
                        String winnerName = response.getString("restaurantName");

                        if (!navigated && !isFinishing()) {
                            navigated = true;
                            runOnUiThread(() -> {   // ✅ fix here
                                Intent topIntent = new Intent(FinalResultsActivity.this, TopChoiceActivity.class);
                                topIntent.putExtra("sessionId", sessionId);
                                topIntent.putExtra("sessionParId", sessionParticipantId);
                                topIntent.putExtra("restaurantId", winnerId);
                                topIntent.putExtra("restaurantName", winnerName);
                                startActivity(topIntent);
                                finish();
                            });
                        }
                    } catch (JSONException e) {
                        Log.e("FinalResultsActivity", "Error parsing final result", e);
                    }
                },
                error -> Log.d("FinalResultsActivity", "No final result yet")
        );

        queue.add(request);
    }

    /** Helper: launch VotingActivity with tied restaurants */
    private void launchVotingActivityForTie(Long sessionId, Long sessionParticipantId,
                                            JSONArray restaurants, int[] tiedIndices) {
        try {
            Intent intent = new Intent(FinalResultsActivity.this, VotingActivity.class);
            intent.putExtra("sessionId", sessionId);
            intent.putExtra("sessionParId", sessionParticipantId);

            // Add each tied restaurant as extras
            for (int i = 0; i < tiedIndices.length; i++) {
                int idx = tiedIndices[i];
                JSONObject r = restaurants.getJSONObject(idx);
                intent.putExtra("restaurant" + (i + 1) + "Id", r.getLong("restaurantId"));
                intent.putExtra("restaurant" + (i + 1) + "Name", r.getString("restaurantName"));
            }

            Log.d("FinalResultsActivity", "Host detected tie, launching VotingActivity with "
                    + tiedIndices.length + " restaurants");

            if (!navigated && !isFinishing()) {
                navigated = true;
                startActivity(intent);
                finish(); // close FinalResultsActivity for host
            }
        } catch (JSONException e) {
            Log.e("FinalResultsActivity", "Error building VotingActivity intent", e);
        }
    }

    /** Helper: launch Buffer activity for participants */
    private void launchBuffer(Long sessionId, Long sessionParticipantId) {
        Intent bufferIntent = new Intent(FinalResultsActivity.this, BufferBeforeTopChoiceActivity.class);
        bufferIntent.putExtra("sessionId", sessionId);
        bufferIntent.putExtra("sessionParId", sessionParticipantId);
        Log.d("FinalResultsActivity", "Participant detected tie, moving to BufferBeforeTopChoiceActivity");

        if (!navigated && !isFinishing()) {
            navigated = true;
            startActivity(bufferIntent);
            finish(); // close FinalResultsActivity for participant
        }
    }
}
