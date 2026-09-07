package com.example.androidexample;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

/**
 * Class for users to login to the app with their username and password
 *
 * Provides UI for entering both fields, validates input and sends login request to backend using Volley.
 * On success, stores data in SharedPreferences and navigates to the main screen.
 */
public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText;  // username input
    private EditText passwordEditText;  // password input
    private Button  loginBtn;           // login button

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameEditText = findViewById(R.id.login_username_edt);
        passwordEditText = findViewById(R.id.login_password_edt);
        loginBtn         = findViewById(R.id.login_login_btn);
        TextView tvSignup = findViewById(R.id.tvSignupText);   // "Sign up" text under login

        // when user taps "Sign Up" text -> go to SignUpActivity
        tvSignup.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

        // login button logic
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // validate inputs
                if (usernameEditText.getText().toString().isEmpty()) {
                    usernameEditText.setError("Username is required");
                }

                if (passwordEditText.getText().toString().isEmpty()) {
                    passwordEditText.setError("Password is required");
                }

                if (!usernameEditText.getText().toString().isEmpty()
                        && !passwordEditText.getText().toString().isEmpty()) {

                    String username = usernameEditText.getText().toString();
                    String password = passwordEditText.getText().toString();

                    JSONObject loginData = new JSONObject();
                    try {
                        loginData.put("username", username);
                        loginData.put("password", password);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    JsonObjectRequest loginRequest = new JsonObjectRequest(
                            Request.Method.POST,
                            Api.LOGIN,           // use Api.LOGIN
                            loginData,
                            response -> {
                                try {
                                    String retUsername = response.getString("username");
                                    String userID      = response.getString("id");
                                    String email       = response.getString("email");
                                    String name        = response.getString("name");
                                    boolean isAdmin    = response.optBoolean("isAdmin", false);

                                    // save everything into SharedPreferences
                                    SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                                    prefs.edit().putBoolean("isLoggedIn", true).apply();
                                    prefs.edit().putString("USERNAME", retUsername).apply();
                                    prefs.edit().putString("USERID", userID).apply();
                                    prefs.edit().putString("PASSWORD", password).apply();
                                    prefs.edit().putString("EMAIL", email).apply();
                                    prefs.edit().putString("NAME", name).apply();
                                    prefs.edit().putBoolean("IS_ADMIN", isAdmin).apply();

                                    Toast.makeText(LoginActivity.this,
                                            "Login successful! Welcome, " + retUsername,
                                            Toast.LENGTH_SHORT).show();

                                    // go to main menu screen
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();   // so back button won't go back to login
                                }
                                catch (JSONException e) {
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

                                Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
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

                    RequestQueue queue = Volley.newRequestQueue(LoginActivity.this);
                    queue.add(loginRequest);
                }
            }
        });
    }
}
