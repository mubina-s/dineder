package com.example.androidexample;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Main screen for application reviews. Hey
 * <p>
 * {@code AppOverview} displays:
 * </p>
 * <ul>
 *     <li>Total number of reviews (stats)</li>
 *     <li>List of review cards loaded from the backend</li>
 *     <li>A form for logged-in users to submit a new review</li>
 * </ul>
 * <p>
 * Users can always read reviews, but must be logged in to:
 * </p>
 * <ul>
 *     <li>Create a new review</li>
 *     <li>Edit their own reviews</li>
 *     <li>Delete their own reviews</li>
 * </ul>
 * <p>
 * This activity communicates with the backend via REST API calls
 * (GET, POST, PUT, DELETE) defined in {@link Api}.
 * Authentication is managed by {@link AuthStore}.
 * </p>
 *
 * @author Mubina Sadriddinova
 */
public class AppOverview extends AppCompatActivity {

    /** Container for dynamically rendered review cards. */
    private LinearLayout reviewList;

    /** Rating bar used to create a new review. */
    private RatingBar ratingBar;

    /** Text field where user types review content. */
    private EditText reviewInput;

    /** Button to return to main. */
    private Button btnBack;

    /** Button to submit a new review. */
    private Button btnSubmit;

    /** Button to navigate to {@link LoginReviews}. */
    private Button btnLogin;

    /** Button to clear current auth and log out. */
    private Button btnLogout;

    /** TextView displaying total number of reviews. */
    private TextView txtTotalUsers;

    /** Current authentication information (role, userId, token). */
    private AuthStore.Auth auth; // role, userId, token

    /**
     * Initializes the UI, loads authentication state, configures button listeners,
     * styles the rating bar, and requests initial data (stats and review list).
     *
     * @param savedInstanceState previous instance state, or {@code null}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_overview);

        txtTotalUsers = findViewById(R.id.txtTotalUsers);
        reviewList    = findViewById(R.id.reviewsContainer);
        ratingBar     = findViewById(R.id.ratingBar);
        reviewInput   = findViewById(R.id.reviewInput);
        btnBack       = findViewById(R.id.btnBack);
        btnSubmit     = findViewById(R.id.btnSubmit);
        btnLogin      = findViewById(R.id.btnLogin);
        btnLogout     = findViewById(R.id.btnLogout);

        // Load current authentication (if any)
        auth = AuthStore.read(this);

        // Style rating bar for writing
        forceFiveStars(ratingBar);
        tintRatingBarGold(ratingBar);
        ratingBar.setIsIndicator(false);
        ratingBar.setStepSize(1f);

        // Button listeners
        btnBack.setOnClickListener(v ->
                startActivity(new Intent(this, MainActivity.class)));

        btnSubmit.setOnClickListener(v -> submitReview());

        btnLogin.setOnClickListener(v ->
                startActivity(new Intent(this, LoginReviews.class)));

        btnLogout.setOnClickListener(v -> {
            AuthStore.clear(this);
            auth = AuthStore.read(this);
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
            setTitle("App Reviews (guest)");
            btnSubmit.setEnabled(false);
            fetchReviews();
        });

        // Initial title and submit button state
        if (auth != null && auth.role != null) {
            setTitle("App Reviews (" + auth.role + ")");
            btnSubmit.setEnabled(true);
        } else {
            setTitle("App Reviews (guest)");
            btnSubmit.setEnabled(false);
        }

        // Load stats and reviews list
        fetchStats();
        fetchReviews();
    }

    /**
     * Called when returning to this screen.
     * Reloads authentication state so that the UI correctly reflects login/logout changes.
     */
    @Override
    protected void onResume() {
        super.onResume();
        auth = AuthStore.read(this);
        if (auth != null && auth.role != null) {
            setTitle("App Reviews (" + auth.role + ")");
            btnSubmit.setEnabled(true);
        } else {
            setTitle("App Reviews (guest)");
            btnSubmit.setEnabled(false);
        }
    }

    /* --------------------------- REST: STATS --------------------------- */

    /**
     * Loads total review statistics from the backend and updates the
     * "total users" TextView.
     * <p>
     * Uses a simple GET request to {@link Api#REVIEWS_STATS}.
     * </p>
     */
    private void fetchStats() {
        StringRequest req = new StringRequest(
                Request.Method.GET,
                Api.REVIEWS_STATS,
                resp -> {
                    try {
                        JSONObject o = new JSONObject(resp);
                        int total = o.optInt("totalReviews", -1);
                        if (total >= 0) {
                            txtTotalUsers.setText(String.valueOf(total));
                        } else {
                            txtTotalUsers.setText("N/A");
                        }
                    } catch (Exception ignored) {}
                },
                err -> txtTotalUsers.setText("N/A")
        );
        Volley.newRequestQueue(this).add(req);
    }

    /* --------------------------- REST: LIST --------------------------- */

    /**
     * Retrieves all reviews from the backend and renders each review card
     * inside the {@link #reviewList} container.
     */
    private void fetchReviews() {
        JsonArrayRequest req = new JsonArrayRequest(
                Request.Method.GET,
                Api.REVIEWS,
                null,
                arr -> {
                    reviewList.removeAllViews();
                    for (int i = 0; i < arr.length(); i++) {
                        try {
                            JSONObject o = arr.getJSONObject(i);
                            addReviewCard(Review.fromJson(o));
                        } catch (Exception ignored) {}
                    }
                },
                err -> Toast.makeText(this, "Failed to load reviews", Toast.LENGTH_SHORT).show()
        );
        Volley.newRequestQueue(this).add(req);
    }

    /* --------------------------- REST: CREATE --------------------------- */

    /**
     * Submits a new review to the backend using HTTP POST.
     * <p>
     * Requires the user to be logged in. If not logged in, the method
     * redirects to {@link LoginReviews}. It sends rating and text content
     * as JSON, handling multiple field names for extra safety.
     * </p>
     */
    private void submitReview() {
        int stars = Math.round(ratingBar.getRating());
        String text = reviewInput.getText().toString().trim();

        if (stars <= 0 || text.isEmpty()) {
            Toast.makeText(this, "Please choose stars and write a review", Toast.LENGTH_SHORT).show();
            return;
        }

        // Require login
        if (auth == null || auth.userId <= 0) {
            Toast.makeText(this, "Please log in first to write a review", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginReviews.class));
            return;
        }

        try {
            JSONObject body = new JSONObject();
            body.put("userId", auth.userId);
            body.put("rating", stars);
            body.put("stars", stars);
            body.put("review", text);
            body.put("content", text);
            body.put("text", text); // extra safety

            JsonObjectRequest req = new JsonObjectRequest(
                    Request.Method.POST,
                    Api.REVIEWS,
                    body,
                    resp -> {
                        ratingBar.setRating(0f);
                        reviewInput.setText("");
                        fetchReviews();
                    },
                    err -> {
                        String m = (err != null && err.networkResponse != null)
                                ? "Submit failed (" + err.networkResponse.statusCode + ")"
                                : "Submit failed";
                        Toast.makeText(this, m, Toast.LENGTH_SHORT).show();
                    }) {

                @Override
                public java.util.Map<String, String> getHeaders() {
                    java.util.HashMap<String, String> h = new java.util.HashMap<>();
                    if (auth != null && auth.token != null) {
                        h.put("Authorization", "Bearer " + auth.token);
                    }
                    h.put("Content-Type", "application/json; charset=utf-8");
                    return h;
                }

                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }
            };
            Volley.newRequestQueue(this).add(req);
        } catch (Exception ignored) {}
    }

    /* --------------------------- REST: UPDATE --------------------------- */

    /**
     * Updates an existing review using HTTP PUT.
     * <p>
     * Only the review owner may update it. Sends updated rating and content fields.
     * </p>
     *
     * @param id    ID of the review to update
     * @param stars new star rating
     * @param text  updated review content
     */
    private void updateReview(long id, int stars, String text) {
        if (auth == null || auth.userId <= 0) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject body = new JSONObject();
        try {
            body.put("userId", auth.userId);
            body.put("rating", stars);
            body.put("stars", stars);
            body.put("review", text);
            body.put("content", text);
            body.put("text", text);
        } catch (Exception ignored) {}

        JsonObjectRequest req = new JsonObjectRequest(
                Request.Method.PUT,
                Api.reviewById(id),
                body,
                resp -> {
                    Toast.makeText(this, "Updated!", Toast.LENGTH_SHORT).show();
                    fetchReviews();
                },
                err -> {
                    String m = (err != null && err.networkResponse != null)
                            ? "Update failed (" + err.networkResponse.statusCode + ")"
                            : "Update failed";
                    Toast.makeText(this, m, Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                java.util.HashMap<String, String> h = new java.util.HashMap<>();
                if (auth != null && auth.token != null) {
                    h.put("Authorization", "Bearer " + auth.token);
                }
                h.put("Content-Type", "application/json; charset=utf-8");
                return h;
            }

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }
        };

        Volley.newRequestQueue(this).add(req);
    }

    /* --------------------------- REST: DELETE --------------------------- */

    /**
     * Deletes one of the user's own reviews using an HTTP DELETE call.
     *
     * @param id ID of the review to delete
     */
    private void deleteReview(long id) {
        if (auth == null || auth.userId <= 0) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            return;
        }

        StringRequest req = new StringRequest(
                Request.Method.DELETE,
                Api.reviewById(id),
                resp -> {
                    Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
                    fetchReviews();
                },
                err -> Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show()
        ) {
            @Override
            public java.util.Map<String, String> getHeaders() {
                java.util.HashMap<String, String> h = new java.util.HashMap<>();
                if (auth != null && auth.token != null) {
                    h.put("Authorization", "Bearer " + auth.token);
                }
                return h;
            }
        };

        Volley.newRequestQueue(this).add(req);
    }

    /* ------------------------ UI rendering ------------------------ */

    /**
     * Creates and displays a review card.
     * <p>
     * The card shows author, text, and rating. Edit/Delete buttons are only
     * visible if the current logged-in user owns the review.
     * </p>
     *
     * @param r review model object containing rating, author, and text
     */
    private void addReviewCard(Review r) {
        View card = LayoutInflater.from(this).inflate(R.layout.item_review, reviewList, false);

        RatingBar rb = card.findViewById(R.id.rbStars);
        TextView tvA = card.findViewById(R.id.tvAuthor);
        TextView tvC = card.findViewById(R.id.tvContent);
        Button btnE  = card.findViewById(R.id.btnEdit);
        Button btnD  = card.findViewById(R.id.btnDelete);

        forceFiveStars(rb);
        tintRatingBarGold(rb);
        rb.setIsIndicator(true);
        rb.setStepSize(0.5f);
        rb.setRating(Math.max(0, Math.min(5, r.stars)));

        tvA.setText("By: " + (r.author == null ? "Anonymous" : r.author));
        tvC.setText(r.content == null ? "" : r.content);

        // Show edit/delete only for the owner of this review
        if (auth != null && auth.userId > 0 && auth.userId == r.userId) {
            btnE.setVisibility(View.VISIBLE);
            btnD.setVisibility(View.VISIBLE);
            btnE.setOnClickListener(v -> promptEdit(r));
            btnD.setOnClickListener(v -> deleteReview(r.id));
        } else {
            btnE.setVisibility(View.GONE);
            btnD.setVisibility(View.GONE);
        }

        reviewList.addView(card);
    }

    /**
     * Opens a dialog allowing the user to edit rating and review text,
     * then calls {@link #updateReview(long, int, String)}.
     *
     * @param r review to edit
     */
    private void promptEdit(Review r) {
        View dlg = getLayoutInflater().inflate(R.layout.dialog_edit_review, null);
        EditText et = dlg.findViewById(R.id.etEdit);
        RatingBar rb = dlg.findViewById(R.id.rbEdit);

        forceFiveStars(rb);
        tintRatingBarGold(rb);
        rb.setIsIndicator(false);
        rb.setStepSize(1f);
        rb.setRating(Math.max(0, Math.min(5, r.stars)));
        et.setText(r.content);

        new AlertDialog.Builder(this)
                .setTitle("Edit review")
                .setView(dlg)
                .setPositiveButton("Save", (d, w) ->
                        updateReview(r.id, Math.round(rb.getRating()), et.getText().toString()))
                .setNegativeButton("Cancel", null)
                .show();
    }

    /* ------------------- helpers ------------------- */

    /**
     * Ensures the given {@link RatingBar} always uses exactly five stars.
     *
     * @param rb rating bar being configured
     */
    private void forceFiveStars(RatingBar rb) {
        rb.setNumStars(5);
        rb.setMax(5);
    }

    /**
     * Applies custom gold/gray tinting to a {@link RatingBar}'s stars.
     *
     * @param rb rating bar to stylize
     */
    private void tintRatingBarGold(RatingBar rb) {
        try {
            LayerDrawable stars = (LayerDrawable) rb.getProgressDrawable();
            int gold = 0xFFFFD700;
            int gray = 0xFF9E9E9E;

            stars.getDrawable(0).setColorFilter(gray, PorterDuff.Mode.SRC_ATOP); // background
            stars.getDrawable(1).setColorFilter(gold, PorterDuff.Mode.SRC_ATOP); // secondary
            stars.getDrawable(2).setColorFilter(gold, PorterDuff.Mode.SRC_ATOP); // progress
        } catch (Exception ignored) {}
    }
}
