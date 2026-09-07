package com.example.androidexample;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class UserHistoryActivity extends AppCompatActivity {

    private TextView tvHistoryEmpty;
    private TextView tvTopRestaurantsList;
    private TextView tvRecentList;

    private TextView tvStatsEmpty;
    private TextView tvTotalLikes;
    private TextView tvTotalDislikes;
    private TextView tvSessionsJoined;
    private TextView tvFavoriteCuisine;

    private ProgressBar progressHistory;

    private Button backBtn;

    // CARD containers for animation
    private View cardTopRestaurants;
    private View cardRecent;
    private View cardStats;

    private long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_history);
        setTitle("My History");

        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String userIdStr = prefs.getString("USERID", null);

        if (userIdStr == null) {
            Toast.makeText(this, "No user ID found. Please log in again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        try {
            userId = Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid user ID. Please log in again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Bind views
        progressHistory      = findViewById(R.id.progressHistory);

        tvHistoryEmpty       = findViewById(R.id.tvHistoryEmpty);
        tvTopRestaurantsList = findViewById(R.id.tvTopRestaurantsList);
        tvRecentList         = findViewById(R.id.tvRecentList);

        tvStatsEmpty         = findViewById(R.id.tvStatsEmpty);
        tvTotalLikes         = findViewById(R.id.tvTotalLikes);
        tvTotalDislikes      = findViewById(R.id.tvTotalDislikes);
        tvSessionsJoined     = findViewById(R.id.tvSessionsJoined);
        tvFavoriteCuisine    = findViewById(R.id.tvFavoriteCuisine);

        // Cards for animation
        cardTopRestaurants = findViewById(R.id.cardTopRestaurants);
        cardRecent = findViewById(R.id.cardRecent);
        cardStats = findViewById(R.id.cardStats);

        backBtn = findViewById(R.id.btnBack);

        // Load data
        loadUserHistory();
        loadUserStats();

        backBtn.setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
    }


    /* --------------------------------------------------------------
     *                 LOAD USER HISTORY (/history)
     * -------------------------------------------------------------- */

    private void loadUserHistory() {
        progressHistory.setVisibility(View.VISIBLE);
        tvHistoryEmpty.setVisibility(View.GONE);

        String url = Api.userHistory(userId);

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                res -> {
                    progressHistory.setVisibility(View.GONE);
                    bindHistory(res);
                },
                err -> {
                    progressHistory.setVisibility(View.GONE);
                    tvTopRestaurantsList.setText("No data.");
                    tvRecentList.setText("No data.");
                    tvHistoryEmpty.setVisibility(View.VISIBLE);
                    Toast.makeText(this, "Failed to load history", Toast.LENGTH_SHORT).show();
                }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }


    private void bindHistory(JSONObject res) {

        JSONArray sessions = res.optJSONArray("sessions");

        if (sessions == null || sessions.length() == 0) {
            tvHistoryEmpty.setVisibility(View.VISIBLE);
            tvTopRestaurantsList.setText("No top restaurants yet.");
            tvRecentList.setText("No recent likes yet.");
            return;
        }

        tvHistoryEmpty.setVisibility(View.GONE);

        Map<String, Integer> restaurantCount = new HashMap<>();
        List<String> recentVotes = new ArrayList<>();

        // Parse sessions + votes
        for (int i = 0; i < sessions.length(); i++) {
            JSONObject session = sessions.optJSONObject(i);
            if (session == null) continue;

            JSONArray votes = session.optJSONArray("votes");
            if (votes != null) {
                for (int j = 0; j < votes.length(); j++) {
                    JSONObject v = votes.optJSONObject(j);
                    if (v == null) continue;

                    String name = v.optString("restaurantName", "");
                    if (!name.isEmpty()) {
                        restaurantCount.put(name, restaurantCount.getOrDefault(name, 0) + 1);
                        recentVotes.add(name);
                    }
                }
            }

            JSONObject finalRes = session.optJSONObject("finalResult");
            if (finalRes != null) {
                String name = finalRes.optString("restaurantName", "");
                if (!name.isEmpty()) {
                    restaurantCount.put(name, restaurantCount.getOrDefault(name, 0) + 1);
                }
            }
        }

        /* ---------- TOP RESTAURANTS ---------- */
        if (restaurantCount.isEmpty()) {
            tvTopRestaurantsList.setText("No top restaurants yet.");
        } else {
            StringBuilder sb = new StringBuilder();
            restaurantCount.entrySet()
                    .stream()
                    .sorted((a, b) -> b.getValue() - a.getValue())
                    .limit(3)
                    .forEach(e -> sb.append("• ").append(e.getKey()).append("\n"));

            tvTopRestaurantsList.setText(sb.toString().trim());
        }

        /* ---------- RECENT LIKES ---------- */
        if (recentVotes.isEmpty()) {
            tvRecentList.setText("No recent likes yet.");
        } else {
            StringBuilder sb = new StringBuilder();
            int start = Math.max(0, recentVotes.size() - 5);

            for (int i = recentVotes.size() - 1; i >= start; i--) {
                sb.append("• ").append(recentVotes.get(i)).append("\n");
            }

            tvRecentList.setText(sb.toString().trim());
        }

        // Apply fade animation now that data is loaded
        cardTopRestaurants.startAnimation(
                AnimationUtils.loadAnimation(this, R.anim.fade_in));

        cardRecent.startAnimation(
                AnimationUtils.loadAnimation(this, R.anim.fade_in));
    }



    /* --------------------------------------------------------------
     *                 LOAD USER STATS (/stats)
     * -------------------------------------------------------------- */

    private void loadUserStats() {
        String url = Api.userStats(userId);

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                res -> bindStats(res),
                err -> {
                    tvStatsEmpty.setVisibility(View.VISIBLE);
                    tvTotalLikes.setText("Total Votes: -");
                    tvTotalDislikes.setText("Average Rating: -");
                    tvSessionsJoined.setText("Sessions Joined: -");
                    tvFavoriteCuisine.setText("Favorite Cuisine: -");
                    Toast.makeText(this, "Failed to load stats", Toast.LENGTH_SHORT).show();
                }
        );

        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }


    private void bindStats(JSONObject res) {

        int totalSessions = res.optInt("totalSessions", 0);
        int totalVotes    = res.optInt("totalVotes", 0);
        double avgRating  = res.optDouble("averageRating", 0.0);

        boolean hasStats = totalSessions > 0 || totalVotes > 0 || avgRating > 0;

        tvTotalLikes.setText("Total Votes: " + totalVotes);
        tvTotalDislikes.setText("Average Rating: " + avgRating);
        tvSessionsJoined.setText("Sessions Joined: " + totalSessions);
        tvFavoriteCuisine.setText("Favorite Cuisine: -");

        if (!hasStats) {
            tvStatsEmpty.setVisibility(View.VISIBLE);
        } else {
            tvStatsEmpty.setVisibility(View.GONE);
        }

        // Animate the stats card with slide-up
        cardStats.startAnimation(
                AnimationUtils.loadAnimation(this, R.anim.slide_up));
    }
}
