package com.example.androidexample;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

/**
 * Login screen for the App Reviews feature.
 * <p>
 * Users must log in before they can create, edit, or delete reviews.
 * This activity:
 * </p>
 * <ul>
 *     <li>Shows a dialog to enter username and password</li>
 *     <li>Sends the login request to the backend</li>
 *     <li>Stores token, userId, and role using {@link AuthStore}</li>
 *     <li>Navigates to {@link AppOverview} on successful login</li>
 * </ul>
 *
 * @author Mubina Sadriddinova
 */
public class LoginReviews extends AppCompatActivity {

    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_reviews);

        btnLogin = findViewById(R.id.btnLogin);

        // When the user clicks, open a dialog to enter credentials
        btnLogin.setOnClickListener(this::showLoginDialog);
    }

    /**
     * Shows a modal dialog with username and password fields.
     * On "Login", the dialog calls {@link #doLogin(String, String)}.
     *
     * @param v the view that triggered this dialog (login button)
     */
    private void showLoginDialog(View v) {
        View dialog = getLayoutInflater().inflate(R.layout.dialog_login_reviews, null);
        EditText etUser = dialog.findViewById(R.id.etUsername);
        EditText etPass = dialog.findViewById(R.id.etPassword);

        new AlertDialog.Builder(this)
                .setTitle("Login")
                .setView(dialog)
                .setPositiveButton("Login", (d, w) ->
                        doLogin(
                                etUser.getText().toString().trim(),
                                etPass.getText().toString()
                        ))
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Sends a POST request to the login endpoint and processes the response.
     * <p>
     * The method tries multiple possible field names for token and user ID,
     * because the backend might return:
     * </p>
     * <ul>
     *     <li>{@code token} or {@code jwt}</li>
     *     <li>{@code userId} or {@code id}</li>
     * </ul>
     * <p>
     * On success, it writes the auth info into {@link AuthStore} and starts
     * the {@link AppOverview} activity.
     * </p>
     *
     * @param username username typed by the user
     * @param password password typed by the user
     */
    private void doLogin(String username, String password) {
        try {
            JSONObject body = new JSONObject();
            body.put("username", username);
            body.put("password", password);

            JsonObjectRequest req = new JsonObjectRequest(
                    Request.Method.POST,
                    Api.LOGIN,
                    body,
                    resp -> {
                        // Try to get token from different field names
                        String token = resp.optString("token", null);
                        if (token == null || token.isEmpty()) {
                            token = resp.optString("jwt", null);
                        }

                        // Try to get userId from userId or id
                        long userId = resp.optLong("userId", -1);
                        if (userId <= 0) {
                            userId = resp.optLong("id", -1);
                        }

                        String role = resp.optString("role", "user");

                        if (userId <= 0) {
                            new AlertDialog.Builder(this)
                                    .setMessage("Login succeeded but no userId returned from server.")
                                    .setPositiveButton("OK", null)
                                    .show();
                            return;
                        }

                        // Store auth info for the rest of the app
                        AuthStore.write(
                                this,
                                new AuthStore.Auth(role, userId, token)
                        );

                        // Go to App Reviews page
                        startActivity(new Intent(this, AppOverview.class));
                        finish();
                    },
                    err -> new AlertDialog.Builder(this)
                            .setMessage("Login failed. Check your username or password.")
                            .setPositiveButton("OK", null)
                            .show()
            );

            Volley.newRequestQueue(this).add(req);
        } catch (Exception e) {
            new AlertDialog.Builder(this)
                    .setMessage("Login error: " + e.getMessage())
                    .setPositiveButton("OK", null)
                    .show();
        }
    }
}
