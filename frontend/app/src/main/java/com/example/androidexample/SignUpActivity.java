package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * SignUpActivity allows the user to create a new account by entering
 * username, email, name, and password. Sends a POST request to the backend
 * to register the account. Displays toast messages for both success and errors.
 */
public class SignUpActivity extends AppCompatActivity {

    private EditText etUsername, etEmail, etName, etPassword;
    private Button btnSignUp;
    private Button btnBack; // ⭐ ADDED

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        etUsername = findViewById(R.id.etUsername);
        etEmail    = findViewById(R.id.etEmail);
        etName     = findViewById(R.id.etName);
        etPassword = findViewById(R.id.etPassword);
        btnSignUp  = findViewById(R.id.btnSignUp);
        TextView tvLoginHint = findViewById(R.id.tvLoginHint);


        // ⭐ BACK BUTTON SETUP
        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        tvLoginHint.setOnClickListener(v -> {
            Intent i = new Intent(SignUpActivity.this, LoginActivity.class);
            startActivity(i);
            finish();  // optional
        });

        btnSignUp.setOnClickListener(v -> submit());
    }

    private void submit() {
        String username = get(etUsername);
        String email    = get(etEmail);
        String name     = get(etName);
        String password = get(etPassword);

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email) || TextUtils.isEmpty(name) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject body = new JSONObject();
        try {
            body.put("username", username);
            body.put("email",    email);
            body.put("name",     name);
            body.put("password", password);
        } catch (JSONException e) {
            Toast.makeText(this, "Compose JSON failed", Toast.LENGTH_SHORT).show();
            return;
        }

        String url = "http://10.0.2.2:8080/users/signup";

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.POST,
                url,
                body,
                response -> Toast.makeText(this, "Registered!", Toast.LENGTH_SHORT).show(),
                this::handleVolleyError
        );

        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    private String get(EditText et) {
        return et.getText() == null ? "" : et.getText().toString().trim();
    }

    private void handleVolleyError(VolleyError error) {
        Toast.makeText(this, (error == null) ? "Unknown error" : error.toString(), Toast.LENGTH_LONG).show();
    }
}
