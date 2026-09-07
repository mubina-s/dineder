package com.example.androidexample;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SavedSessionPrefsActivity extends AppCompatActivity {

    private Spinner spinner;

    private EditText preferenceNameEditText;
    private EditText ratingEditText;
    private EditText priceRangeEditText;

    private Button savedCreateSessionBtn;
    private Button savedCreateBtn;
    private Button savedEditBtn;
    private Button savedSaveChangesBtn;
    private Button savedDeleteBtn;
    private Button backBtn;

    private Boolean preferenceSelected;

    SessionPreference selectedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved_session_prefs);

        spinner = findViewById(R.id.my_spinner);
        preferenceNameEditText = findViewById(R.id.preference_name_edt);
        ratingEditText = findViewById(R.id.rating_edt);
        priceRangeEditText = findViewById(R.id.price_range_edt);
        savedCreateSessionBtn = findViewById(R.id.saved_createSession_btn);
        savedCreateBtn = findViewById(R.id.saved_create_btn);
        savedEditBtn = findViewById(R.id.saved_edit_btn);
        savedSaveChangesBtn = findViewById(R.id.saved_save_changes_btn);
        savedDeleteBtn = findViewById(R.id.saved_delete_btn);
        backBtn = findViewById(R.id.btnBack);

        preferenceSelected = false;

        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String userID = prefs.getString("USERID", "");

        if (!userID.equals("")) {
            fetchSessionPrefsForDropdown(userID);
        } else {
            Log.e("DropdownInit", "USERID not found in SharedPreferences");
        }

        backBtn.setOnClickListener(v ->
                startActivity(new Intent(this, SessionPrefsActivity.class)));

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPref = (SessionPreference) parent.getItemAtPosition(position);

                if (selectedPref.id != -1) {
                    findViewById(R.id.nameLayout).setVisibility(View.VISIBLE);
                    findViewById(R.id.ratingLayout).setVisibility(View.VISIBLE);
                    findViewById(R.id.priceRangeLayout).setVisibility(View.VISIBLE);
                    findViewById(R.id.PUTLayout).setVisibility(View.VISIBLE);
                    findViewById(R.id.saved_delete_btn).setVisibility(View.VISIBLE);

                    preferenceSelected = true;

                    fetchSessionPrefDetails(selectedPref.id);

                } else {
                    findViewById(R.id.nameLayout).setVisibility(View.GONE);
                    findViewById(R.id.ratingLayout).setVisibility(View.GONE);
                    findViewById(R.id.priceRangeLayout).setVisibility(View.GONE);
                    findViewById(R.id.PUTLayout).setVisibility(View.GONE);
                    findViewById(R.id.saved_delete_btn).setVisibility(View.GONE);

                    preferenceSelected = false;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                preferenceSelected = false;
            }
        });

        savedCreateSessionBtn.setOnClickListener(v -> {
            if (!preferenceSelected) {
                Toast.makeText(SavedSessionPrefsActivity.this,
                        "Select Session Preferences", Toast.LENGTH_SHORT).show();
            } else {
                String Posturl = "http://10.0.2.2:8080/sessions";

                RequestQueue queue = Volley.newRequestQueue(v.getContext());

                JSONObject postData = new JSONObject();
                try {
                    postData.put("preferenceId", selectedPref.id);
                    postData.put("hostId", prefs.getString("USERID", ""));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JsonObjectRequest request = new JsonObjectRequest(
                        Request.Method.POST,
                        Posturl,
                        postData,
                        response -> {
                            Log.d("VolleyPOST", "Response: " + response.toString());

                            Long sessionId = response.optLong("id");
                            String joinCode = response.optString("joinCode");
                            String hostname = response.optString("hostname");
                            Long hostId = response.optLong("hostId");
                            List<SessionParticipant> participants =
                                    SessionParser.parseParticipants(response.optJSONArray("participantsDTOList"));
                            List<Restaurant> restaurants =
                                    SessionParser.parseRestaurants(response.optJSONArray("restaurantDTOList"));

                            Session session = new Session(sessionId, joinCode, hostname, hostId, restaurants, participants);
                            SessionManager.setSession(session);

                            SessionParticipant par = new SessionParticipant(
                                    participants.get(0).getParticipantId(),
                                    hostId,
                                    hostname,
                                    participants.get(0).getIsReady(),
                                    participants.get(0).getfinishedSwiping());
                            SessionParticipantManager.setSessionPar(par);

                            Intent intent = new Intent(SavedSessionPrefsActivity.this, ReadyUpActivity.class);
                            startActivity(intent);

                        },
                        error -> {
                            if (error.networkResponse != null) {
                                Log.e("Volley", "Status code: " + error.networkResponse.statusCode);
                                Log.e("Volley", "Response data: " + new String(error.networkResponse.data));
                            }
                            Log.e("VolleyPOST", "Error: " + error.toString());
                            Toast.makeText(SavedSessionPrefsActivity.this,
                                    "Failed to create session", Toast.LENGTH_SHORT).show();
                        }
                );

                queue.add(request);

            }
        });

        savedCreateBtn.setOnClickListener(v -> {
            Intent intent = new Intent(SavedSessionPrefsActivity.this, CreateSessionPrefsActivity.class);
            startActivity(intent);
        });
    }

    private void fetchSessionPrefDetails(int prefId) {
        String GET_DETAILS_URL = "http://10.0.2.2:8080/sessionprefs/detail/" + prefId;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, GET_DETAILS_URL, null,
                response -> {
                    try {
                        int id = response.getInt("id");
                        String preferenceName = response.getString("preferenceName");
                        String rating = response.getString("rating");
                        String priceRange = response.getString("priceRange");

                        preferenceNameEditText.setText(preferenceName);
                        preferenceNameEditText.setEnabled(false);
                        ratingEditText.setText(rating);
                        ratingEditText.setEnabled(false);
                        priceRangeEditText.setText(priceRange);
                        priceRangeEditText.setEnabled(false);

                        savedEditBtn.setOnClickListener(v -> {
                            preferenceNameEditText.setEnabled(true);
                            ratingEditText.setEnabled(true);
                            priceRangeEditText.setEnabled(true);
                            preferenceNameEditText.requestFocus();
                        });

                        savedSaveChangesBtn.setOnClickListener(v -> updateSessionPrefsInfo(id));

                        savedDeleteBtn.setOnClickListener(view -> deleteSessionPrefs(id));

                    } catch (JSONException e) {
                        Log.e("SessionPrefError", "JSON parsing error", e);
                    }
                },
                error -> {
                    if (error.networkResponse != null) {
                        Log.e("VolleyError", "Status Code: " + error.networkResponse.statusCode);
                        Log.e("VolleyError", "Response: " + new String(error.networkResponse.data));
                    } else {
                        Log.e("VolleyError", "No network response", error);
                    }
                }
        );

        Volley.newRequestQueue(this).add(request);
    }

    public void fetchSessionPrefsForDropdown(String userID) {
        String GET_ALL_URL = "http://10.0.2.2:8080/sessionprefs/" + userID;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, GET_ALL_URL, null,
                response -> {
                    List<SessionPreference> options = new ArrayList<>();

                    SessionPreference placeholder = new SessionPreference(-1, "Select a preference");
                    options.add(placeholder);

                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            int id = obj.getInt("id");
                            String name = obj.getString("preferenceName");
                            options.add(new SessionPreference(id, name));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    // 🔴 HERE is the only real change: use custom pink layouts
                    ArrayAdapter<SessionPreference> adapter = new ArrayAdapter<>(
                            this,
                            R.layout.spinner_item_pink,          // selected item layout
                            options
                    );
                    adapter.setDropDownViewResource(R.layout.spinner_dropdown_pink); // dropdown layout
                    spinner.setAdapter(adapter);
                },
                error -> Log.e("DropdownFetch", "Error: " + error.getMessage())
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void updateSessionPrefsInfo(int prefID) {
        String URL_PUT = "http://10.0.2.2:8080/sessionprefs/" + prefID;

        String preferenceName = preferenceNameEditText.getText().toString().trim();
        preferenceNameEditText.setEnabled(false);
        String rating = ratingEditText.getText().toString().trim();
        ratingEditText.setEnabled(false);
        String priceRange = priceRangeEditText.getText().toString().trim();
        priceRangeEditText.setEnabled(false);

        JSONObject jsonBody = new JSONObject();

        try {
            jsonBody.put("preferenceName", preferenceName);
            jsonBody.put("rating", rating);
            jsonBody.put("priceRange", priceRange);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest putRequest = new JsonObjectRequest(
                Request.Method.PUT,
                URL_PUT,
                jsonBody,
                response -> {
                    Log.d("PUT Response", response.toString());
                    Toast.makeText(SavedSessionPrefsActivity.this,
                            "Changes saved!", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    Log.e("PUT Error", error.toString());

                    if (error.networkResponse != null) {
                        int statusCode = error.networkResponse.statusCode;
                        Log.e("VolleyError", "Status Code: " + statusCode);

                        try {
                            String responseBody = new String(error.networkResponse.data, "UTF-8");
                            Log.e("VolleyError", "Response Body: " + responseBody);
                        } catch (UnsupportedEncodingException e) {
                            Log.e("VolleyError", "Encoding error", e);
                        }
                    } else {
                        Log.e("VolleyError", "No network response");
                        Log.e("VolleyError", "Error type: " + error.getClass().getSimpleName());
                    }

                    preferenceNameEditText.setEnabled(true);
                    ratingEditText.setEnabled(true);
                    priceRangeEditText.setEnabled(true);
                }

        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer your_token"); // if needed
                headers.put("x-mock-match-request-body", "true");
                Log.d("Request Headers", headers.toString());
                return headers;
            }
        };

        Log.d("Request URL", putRequest.getUrl());
        Log.d("Request Body", jsonBody.toString());

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(putRequest);
    }

    private void deleteSessionPrefs(int prefID) {
        RequestQueue queue = Volley.newRequestQueue(this.getApplicationContext());

        String URL_DELETE = "http://10.0.2.2:8080/sessionprefs/" + prefID;

        StringRequest deleteRequest = new StringRequest(
                Request.Method.DELETE,
                URL_DELETE,
                response -> Toast.makeText(this.getApplicationContext(),
                        "Session Preferences deleted: " + response, Toast.LENGTH_SHORT).show(),
                error -> {
                    Log.e("DELETE Error", error.toString());
                    Toast.makeText(this.getApplicationContext(),
                            "Delete failed", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                return headers;
            }
        };

        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(deleteRequest);
    }
}
