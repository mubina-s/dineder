package com.example.androidexample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.TextViewOnReceiveContentListener;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

public class DashboardActivity extends AppCompatActivity {

    private TextView votesCast;
    private TextView activeSessions;
    private TextView activeUsers;
    private TextView topCuisine;

    private TextView trendingNameView;

    private TextView trendingVotesView;

    private TextView leaderboardName1, leaderboardScore1;
    private TextView leaderboardName2, leaderboardScore2;
    private TextView leaderboardName3, leaderboardScore3;

    private Button backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        votesCast = (TextView) findViewById(R.id.votesCast);
        activeSessions = (TextView) findViewById(R.id.activeSessions);
        activeUsers = (TextView) findViewById(R.id.activeUsers);
        topCuisine = (TextView) findViewById(R.id.topCuisine);
        trendingNameView = (TextView) findViewById(R.id.trendingRestaurant);
        trendingVotesView = (TextView) findViewById(R.id.trendingRecentVotes);

        leaderboardName1 = findViewById(R.id.restaurant1);
        leaderboardScore1 = findViewById(R.id.score1);

        leaderboardName2 = findViewById(R.id.restaurant2);
        leaderboardScore2 = findViewById(R.id.score2);

        leaderboardName3 = findViewById(R.id.restaurant3);
        leaderboardScore3 = findViewById(R.id.score3);

        backBtn = findViewById(R.id.btnBack);

        String url = "http://10.0.2.2:8080/dashboard";

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null, // GET has no body
                response -> {
                    try {
                        // Parse stats
                        long totalDecisions = response.getLong("totalDecisions");
                        String currActiveSessions = Long.toString(response.getLong("activeSessions"));
                        String currActiveUsers = Long.toString(response.getLong("activeUsers"));
                        String mostPopularCuisine = response.getString("mostPopularCuisine");

                        // Parse trending
                        JSONObject trendingObj = response.getJSONObject("trending");
                        String trendingName = trendingObj.getString("name");
                        int recentVotesCount = trendingObj.getInt("recentVotesCount");

                        // Parse leaderboard
                        JSONArray leaderboardArray = response.getJSONArray("leaderboard");

                        if (leaderboardArray.length() == 3) {
                            JSONObject entry1 = leaderboardArray.getJSONObject(0);
                            leaderboardName1.setText(entry1.getString("name"));
                            leaderboardScore1.setText(String.format("%.1f%%", entry1.getDouble("score")));

                            JSONObject entry2 = leaderboardArray.getJSONObject(1);
                            leaderboardName2.setText(entry2.getString("name"));
                            leaderboardScore2.setText(String.format("%.1f%%", entry2.getDouble("score")));

                            JSONObject entry3 = leaderboardArray.getJSONObject(2);
                            leaderboardName3.setText(entry3.getString("name"));
                            leaderboardScore3.setText(String.format("%.1f%%", entry3.getDouble("score")));
                        }

                        // ✅ Update UI
                        votesCast.setText(String.valueOf(totalDecisions));
                        activeSessions.setText(String.valueOf(currActiveSessions));
                        activeUsers.setText(String.valueOf(currActiveUsers));
                        topCuisine.setText(mostPopularCuisine);

                        trendingNameView.setText(trendingName);
                        trendingVotesView.setText(String.valueOf(recentVotesCount));

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Log.e("Dashboard", "Error fetching dashboard", error);
                    Toast.makeText(DashboardActivity.this, "Failed to load dashboard", Toast.LENGTH_SHORT).show();
                }
        );

// Add to request queue
        RequestQueue queue = Volley.newRequestQueue(DashboardActivity.this);
        queue.add(request);

        backBtn.setOnClickListener(v ->
                startActivity(new Intent(this, MainActivity.class)));

    }
}
