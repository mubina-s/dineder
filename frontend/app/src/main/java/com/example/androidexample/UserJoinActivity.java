package com.example.androidexample;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Activity that allows users to join an existing session or view results of a completed session.
 * <p>
 * Provides UI for entering a join code and buttons for joining or viewing results.
 * Integrates with backend APIs via Volley to:
 * - Send POST requests to join sessions
 * - Send GET requests to fetch session details
 * - Navigate to ReadyUpActivity or FinalResultsActivity depending on context
 * </p>
 * @author Sangeetha Santhanu
 */
public class UserJoinActivity extends AppCompatActivity {

    private Button backBtn;
    private Button joinBtn;
    private EditText joinCodeEt;

    /**
     * Called when the activity is created.
     * Initializes UI components and sets up listeners for join and results buttons.
     * Validates input fields and triggers appropriate network requests based on user actions.
     * @param savedInstanceState Bundle containing the activity's previously saved state,
     *                           or null if none exists.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_join);

        backBtn = (Button) findViewById(R.id.btnBack);
        joinBtn = (Button) findViewById(R.id.join_join_btn);
        joinCodeEt = (EditText) findViewById(R.id.join_code_edittext);

        backBtn.setOnClickListener(v ->
                startActivity(new Intent(this, MainActivity.class)));

        joinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String joinCode = joinCodeEt.getText().toString().trim();
                if (joinCode.isEmpty()) {
                    // It's empty — show a message or prevent submission
                    Toast.makeText(UserJoinActivity.this, "Please enter a join code", Toast.LENGTH_SHORT).show();
                }
                else {
                    SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);

                    String Posturl = "http://10.0.2.2:8080/sessions/join";

                    RequestQueue queue = Volley.newRequestQueue(v.getContext());

                    JSONObject postData = new JSONObject();
                    try {
                        postData.put("userId", prefs.getString("USERID", ""));
                        postData.put("joinCode", joinCode); // Replace with actual joinCode
                        //TODO: get userId from sharedPrefs or User object after switching to after login not main
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    JsonObjectRequest request = new JsonObjectRequest(
                            Request.Method.POST,
                            Posturl,
                            postData,
                            response -> {
                                // Success: handle response
                                Log.d("VolleyPOST", "Response: " + response.toString());

                                Long participantId = response.optLong("participantId");
                                Long userId = response.optLong("userId");
                                String name = response.optString("name");
                                Boolean isReady = response.optBoolean("isReady");
                                Boolean isFinishedSwiping = response.optBoolean("isFinishedSwiping");

                                SessionParticipant par = new SessionParticipant(participantId, userId, name, isReady, isFinishedSwiping);

                                // Store in singleton
                                SessionParticipantManager.setSessionPar(par);

                                // Now trigger GET request to fetch session info
                                fetchSessionInfoJoin(joinCode);
                            },
                            error -> {
                                // Error: handle failure
                                if (error.networkResponse != null) {
                                    Log.e("Volley", "Status code: " + error.networkResponse.statusCode);
                                    Log.e("Volley", "Response data: " + new String(error.networkResponse.data));
                                }
                                Log.e("VolleyPOST", "Error: " + error.toString());
                                Toast.makeText(UserJoinActivity.this, "Failed to join session", Toast.LENGTH_SHORT).show();
                            }
                    );

                    queue.add(request);

                }
            }
        });

    }

    /**
     * Fetches session information for an existing session to display on the ReadyUp screen.
     * <p>
     * Sends a GET request using the provided join code, parses session details,
     * stores them in the SessionManager singleton, and navigates to ReadyUpActivity.
     * </p>
     * @param joinCode the unique code identifying the session to join
     */
    public void fetchSessionInfoJoin(String joinCode) {
        String Geturl = "http://10.0.2.2:8080/sessions/" + joinCode;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                Geturl,
                null,
                response -> {
                    // Parse session info
                    Long id = response.optLong("id");
                    String hostname = response.optString("hostname");
                    Long hostId = response.optLong("hostId");
                    JSONArray restaurantArray = response.optJSONArray("restaurantDTOList");
                    JSONArray participantsArray = response.optJSONArray("participantsDTOList");

                    List<Restaurant> restaurants = SessionParser.parseRestaurants(restaurantArray);
                    for (Restaurant restaurant : restaurants) {
                        Log.d("SessionParser", "Parsed restaurants: " + restaurant.toString());
                    }

                    List<SessionParticipant> participants = SessionParser.parseParticipants(participantsArray);

                    Session session = new Session(id, joinCode, hostname, hostId, restaurants, participants);

                    // Store in singleton
                    SessionManager.setSession(session);

                    // Navigate to ReadyUpActivity
                    Intent intent = new Intent(UserJoinActivity.this, ReadyUpActivity.class);
                    startActivity(intent);

                },
                error -> {
                    Toast.makeText(UserJoinActivity.this, "Invalid join code", Toast.LENGTH_SHORT).show();
                }
        );

        RequestQueue queue = Volley.newRequestQueue(UserJoinActivity.this);
        queue.add(request);
    }

}
