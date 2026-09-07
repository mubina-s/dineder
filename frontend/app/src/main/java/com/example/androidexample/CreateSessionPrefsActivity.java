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

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CreateSessionPrefsActivity extends AppCompatActivity {
    // existing fields
    private EditText preferenceNameEditText;
    private EditText ratingEditText;
    private EditText priceRangeEditText;
    private Button createBtn, switchSavedBtn, backBtn;

    // NEW: for "real app" price selector
    private Button btnPriceCheap;
    private Button btnPriceMedium;
    private Button btnPriceExpensive;

    private static final String URL_POST = "http://10.0.2.2:8080/sessionprefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_session_prefs);

        preferenceNameEditText = findViewById(R.id.preference_name_edt);
        ratingEditText = findViewById(R.id.rating_edt);
        priceRangeEditText = findViewById(R.id.price_range_edt);
        createBtn = findViewById(R.id.create_btn);
        switchSavedBtn = findViewById(R.id.create_switch_saved_btn);
        backBtn = findViewById(R.id.btnBack);

        // NEW: bind price selector buttons
        btnPriceCheap = findViewById(R.id.btnPriceCheap);
        btnPriceMedium = findViewById(R.id.btnPriceMedium);
        btnPriceExpensive = findViewById(R.id.btnPriceExpensive);

        // NEW: make chips fill the EditText value ($, $$, $$$)
        View.OnClickListener priceClickListener = v -> {
            if (v.getId() == R.id.btnPriceCheap) {
                priceRangeEditText.setText("$");
            } else if (v.getId() == R.id.btnPriceMedium) {
                priceRangeEditText.setText("$$");
            } else if (v.getId() == R.id.btnPriceExpensive) {
                priceRangeEditText.setText("$$$");
            }
        };

        btnPriceCheap.setOnClickListener(priceClickListener);
        btnPriceMedium.setOnClickListener(priceClickListener);
        btnPriceExpensive.setOnClickListener(priceClickListener);

        backBtn.setOnClickListener(v ->
                startActivity(new Intent(this, SessionPrefsActivity.class)));

        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (preferenceNameEditText.getText().toString().isEmpty()) {
                    preferenceNameEditText.setError("Name is required");
                }

                if (ratingEditText.getText().toString().isEmpty()) {
                    ratingEditText.setError("Rating is required");
                }

                if (priceRangeEditText.getText().toString().isEmpty()) {
                    priceRangeEditText.setError("Price Range is required");
                }

                if (!preferenceNameEditText.getText().toString().isEmpty()
                        && !ratingEditText.getText().toString().isEmpty()
                        && !priceRangeEditText.getText().toString().isEmpty()) {
                    createNewSessionPrefs();
                }
            }
        });

        switchSavedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CreateSessionPrefsActivity.this, SavedSessionPrefsActivity.class);
                startActivity(intent);
            }
        });
    }

    void createNewSessionPrefs() {

        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);

        String preferenceName = preferenceNameEditText.getText().toString();
        String rating = ratingEditText.getText().toString();
        String priceRange = priceRangeEditText.getText().toString();
        Integer hostId = Integer.parseInt(prefs.getString("USERID", "defaultHostID"));

        JSONObject sessionPrefsData = new JSONObject();
        try {
            sessionPrefsData.put("preferenceName", preferenceName);
            sessionPrefsData.put("rating", rating);
            sessionPrefsData.put("priceRange", priceRange);
            sessionPrefsData.put("hostId", hostId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest postRequest = new JsonObjectRequest(
                Request.Method.POST,
                URL_POST,
                sessionPrefsData,
                response -> {
                    try {
                        int id = response.getInt("id");
                        String resPreferenceName = response.optString("preferenceName", "default");
                        String resRating = response.optString("rating", "default");
                        String resPriceRange = response.optString("priceRange", "default");

                        JSONObject host = response.getJSONObject("host");
                        int hostID = host.getInt("id");
                        String hostUsername = host.getString("username");
                        String hostPassword = host.getString("password");
                        String hostEmail = host.getString("email");
                        String hostName = host.getString("name");

                        SharedPreferences sessionPrefs = CreateSessionPrefsActivity.this.getSharedPreferences("SessionPrefs", MODE_PRIVATE);
                        sessionPrefs.edit().putInt("ID", id).apply();
                        sessionPrefs.edit().putString("PREFERENCENAME", resPreferenceName).apply();
                        sessionPrefs.edit().putString("RATING", resRating).apply();
                        sessionPrefs.edit().putString("PRICERANGE", resPriceRange).apply();

                        sessionPrefs.edit().putInt("HOSTID", hostID).apply();
                        sessionPrefs.edit().putString("HOSTUSERNAME", hostUsername).apply();
                        sessionPrefs.edit().putString("HOSTPASSWORD", hostPassword).apply();
                        sessionPrefs.edit().putString("HOSTEMAIL", hostEmail).apply();
                        sessionPrefs.edit().putString("HOSTNAME", hostName).apply();

                        Toast.makeText(CreateSessionPrefsActivity.this, "New Session Preferences Created! : " + resPreferenceName, Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(CreateSessionPrefsActivity.this, SessionPrefsActivity.class);
                        startActivity(intent);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    String errorMsg = "Network error";

                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        try {
                            if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                                Log.e("Volley", "Network timeout or no connection");
                            } else if (error instanceof AuthFailureError) {
                                Log.e("Volley", "Authentication failure");
                            } else if (error instanceof ServerError) {
                                Log.e("Volley", "Server error");
                            } else if (error instanceof NetworkError) {
                                Log.e("Volley", "Network error");
                            } else if (error instanceof ParseError) {
                                Log.e("Volley", "Parse error");
                            }

                            String responseBody = new String(error.networkResponse.data, "UTF-8");
                            JSONObject errorJson = new JSONObject(responseBody);
                            errorMsg = errorJson.optString("message", "Unknown error");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    Toast.makeText(CreateSessionPrefsActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }

        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("x-mock-match-request-body", "true");
                return headers;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(CreateSessionPrefsActivity.this);
        queue.add(postRequest);
        Log.d("ActivityStatus", "onCreate completed");
    }
}
