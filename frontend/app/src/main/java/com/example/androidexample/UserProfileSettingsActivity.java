package com.example.androidexample;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView; // for header name/email
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Activity that displays and manages user profile information.
 * <p>
 * Provides UI for viewing, editing, saving, and deleting user data.
 * Integrates with backend APIs via Volley to update or remove user records.
 * </p>
 * <p>
 *  Responsibilities:
 *  - Load user info from SharedPreferences
 *  - Allow editing of username, password, name, and email
 *  - Send PUT requests to update user info
 *  - Send DELETE requests to remove user account
 * </p>
 * @author Sangeetha Santhanu
 */
public class UserProfileSettingsActivity extends AppCompatActivity {

    private EditText editUsername;  // define username edittext variable
    private EditText editPassword;  // define password edittext variable
    private EditText editName;      // define name edittext variable
    private EditText editEmail;     // define email edittext variable

    private Button editBtn;   // define editing values button
    private Button saveBtn;   // define saving changes button
    private Button deleteBtn; // define deleting account button
    private Button confirmBtn;// define confirming info button

    // NEW: back button
    private Button backBtn;

    // NEW: header preview text views for name + email
    private TextView tvProfileName;
    private TextView tvProfileEmail;

    /**
     * Called when the activity is created.
     * <p>
     * Initializes UI components with stored user info and sets up button listeners
     * for editing, saving, deleting, and confirming profile changes.
     * </p>
     * @param savedInstanceState Bundle containing the activity's previously saved state,
     *                           or null if none exists.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile_settings);

        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);

        // 🔙 Back button
        backBtn = findViewById(R.id.btnBack);
        backBtn.setOnClickListener(v -> finish()); // just close this screen and go back

        editBtn = (Button) findViewById(R.id.editBtn);

        editUsername = (EditText) findViewById(R.id.editUsername);
        editUsername.setText(prefs.getString("USERNAME", "defaultUsername"));
        editUsername.setEnabled(false);

        editPassword = (EditText) findViewById(R.id.editPassword);
        editPassword.setText(prefs.getString("PASSWORD", "defaultPassword"));
        editPassword.setEnabled(false);

        editName = (EditText) findViewById(R.id.editName);
        editName.setText(prefs.getString("NAME", "defaultName"));
        editName.setEnabled(false);

        editEmail = (EditText) findViewById(R.id.editEmail);
        editEmail.setText(prefs.getString("EMAIL", "defaultEmail"));
        editEmail.setEnabled(false);

        saveBtn = (Button) findViewById(R.id.saveBtn);
        deleteBtn = (Button) findViewById(R.id.deleteBtn);
        confirmBtn = (Button) findViewById(R.id.confirmBtn);

        // header name + email preview
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);

        String nameFromPrefs = prefs.getString("NAME", "Your Name");
        String emailFromPrefs = prefs.getString("EMAIL", "email@example.com");

        tvProfileName.setText(nameFromPrefs);
        tvProfileEmail.setText(emailFromPrefs);

        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editUsername.setEnabled(true);
                editPassword.setEnabled(true);
                editName.setEnabled(true);
                editEmail.setEnabled(true);

                editUsername.requestFocus();
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserInfo();
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteUser();
            }
        });

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newIntent = new Intent(UserProfileSettingsActivity.this, MainActivity.class);
                startActivity(newIntent);
            }
        });
    }

    /**
     * Sends a Volley PUT request to update user information on the backend.
     * <p>
     * Collects input from UI fields, constructs a JSON body, and submits it to the server.
     * On success, disables editing and displays a confirmation message.
     * On error, re-enables fields and logs details.
     * </p>
     */
    private void updateUserInfo() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String userID = prefs.getString("USERID", "defaultUserID");
        String URL_PUT = "http://10.0.2.2:8080/users/" + userID;

        String username = editUsername.getText().toString().trim();
        editUsername.setEnabled(false);
        String password = editPassword.getText().toString().trim();
        editPassword.setEnabled(false);
        String name = editName.getText().toString().trim();
        editName.setEnabled(false);
        String email = editEmail.getText().toString().trim();
        editEmail.setEnabled(false);

        JSONObject jsonBody = new JSONObject();

        try {
            //TODO: make sure displays user info from login (password not dots?), input validation PUT
            jsonBody.put("id", userID);
            jsonBody.put("username", username);
            jsonBody.put("password", password);
            jsonBody.put("email", email);
            jsonBody.put("name", name);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest putRequest = new JsonObjectRequest(
                Request.Method.PUT,  // HTTP request method (PUT)
                URL_PUT,     // URL of the PUT API
                jsonBody,
                response -> {
                    Log.d("PUT Response", response.toString());
                    Toast.makeText(UserProfileSettingsActivity.this, "Changes saved!", Toast.LENGTH_SHORT).show();
                    editUsername.setEnabled(false);
                    editPassword.setEnabled(false);
                    editName.setEnabled(false);
                    editEmail.setEnabled(false);

                    // update SharedPreferences with latest values
                    SharedPreferences.Editor editor = getSharedPreferences("MyAppPrefs", MODE_PRIVATE).edit();
                    editor.putString("USERNAME", username);
                    editor.putString("PASSWORD", password);
                    editor.putString("NAME", name);
                    editor.putString("EMAIL", email);
                    editor.apply();

                    // update header preview with new name + email
                    if (tvProfileName != null) {
                        tvProfileName.setText(name);
                    }
                    if (tvProfileEmail != null) {
                        tvProfileEmail.setText(email);
                    }
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

                    editUsername.setEnabled(true);
                    editPassword.setEnabled(true);
                    editName.setEnabled(true);
                    editEmail.setEnabled(true);
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

    /**
     * Sends a Volley DELETE request to remove the user account from the backend.
     * <p>
     * On success, displays a confirmation message and navigates back to the main activity.
     * On failure, logs the error and shows a toast message.
     * </p>
     */
    private void deleteUser() {
        RequestQueue queue = Volley.newRequestQueue(this.getApplicationContext());

        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String userID = prefs.getString("USERID", "defaultUserID");
        String URL_DELETE = "http://10.0.2.2:8080/users/" + userID;

        StringRequest deleteRequest = new StringRequest(
                Request.Method.DELETE,
                URL_DELETE,
                response -> {
                    Toast.makeText(this.getApplicationContext(),  "Account deleted: " + response, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(UserProfileSettingsActivity.this, LoginActivity.class);
                    startActivity(intent);
                },
                error -> {
                    Log.e("DELETE Error", error.toString());
                    Toast.makeText(this.getApplicationContext(), "Delete failed", Toast.LENGTH_SHORT).show();
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
