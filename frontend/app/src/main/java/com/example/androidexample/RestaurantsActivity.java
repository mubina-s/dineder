package com.example.androidexample;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * RestaurantsActivity displays a list of restaurants retrieved from the backend.
 * Users can create, update, or delete restaurants using dialogs. The data is
 * loaded through GET, POST, PUT, and DELETE REST API calls.
 *
 * Responsibilities:
 * - Fetch and display restaurant list in a RecyclerView.
 * - Provide UI dialogs for creating or editing restaurant entries (admin only).
 * - Communicate with the backend through Volley and JSON.
 */
public class RestaurantsActivity extends AppCompatActivity implements RestaurantsAdapter.OnRestaurantClick {

    private RecyclerView rv;
    private View fabAdd;
    private RestaurantsAdapter adapter;

    private long currentUserId = 0L;
    private boolean isAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurants);
        setTitle("Choose Restaurants");

        rv = findViewById(R.id.rvRestaurants);
        fabAdd = findViewById(R.id.fabAddRestaurant);

        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RestaurantsAdapter(this, this);
        rv.setAdapter(adapter);

        // read user + admin flag from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String userIdStr = prefs.getString("USERID", "0");
        try {
            currentUserId = Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            currentUserId = 0L;
        }
        isAdmin = prefs.getBoolean("IS_ADMIN", false);

        // only admins can create restaurants
        if (!isAdmin && fabAdd != null) {
            fabAdd.setVisibility(View.GONE);
        } else if (fabAdd != null) {
            fabAdd.setOnClickListener(v -> showCreateDialog());
        }

        loadRestaurants();

    }

    /* ------------------------ LOAD ------------------------ */

    /**
     * Performs a GET request to load all restaurants from the backend.
     * On success, populates the list in the adapter.
     */
    private void loadRestaurants() {
        JsonArrayRequest req = new JsonArrayRequest(
                Request.Method.GET,
                Api.RESTAURANTS,
                null,
                this::bindList,
                err -> Toast.makeText(this, "Failed to load restaurants", Toast.LENGTH_SHORT).show()
        );
        VolleySingleton.getInstance(this).addToRequestQueue(req);
    }

    /**
     * Converts a JSON array of restaurant items into Java model objects
     * and updates the RecyclerView adapter.
     *
     * @param arr JSON array returned from the backend
     */
    private void bindList(JSONArray arr) {
        ArrayList<Restaurant> list = new ArrayList<>();
        if (arr != null) {
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.optJSONObject(i);
                if (o == null) continue;
                Restaurant r = new Restaurant();
                r.id         = o.optLong("id", 0);
                r.name       = o.optString("name", "");
                r.address    = o.optString("address", "");
                r.rating     = o.optInt("rating", 0);
                r.priceRange = o.optString("priceRange", "");
                r.phone      = o.optString("phone", "");
                r.website    = o.optString("website", "");
                list.add(r);
            }
        }
        adapter.submit(list);
    }

    /* ------------------------ CREATE (ADMIN) ------------------------ */

    /**
     * Opens a dialog allowing the user to create a restaurant. After validation,
     * sends a POST request to the backend to add the new restaurant.
     */
    private void showCreateDialog() {
        if (!isAdmin) {
            Toast.makeText(this, "Only admins can add restaurants.", Toast.LENGTH_SHORT).show();
            return;
        }

        View view = getLayoutInflater().inflate(R.layout.dialog_restaurant, null, false);
        EditText etName   = view.findViewById(R.id.etName);
        EditText etAddr   = view.findViewById(R.id.etAddress);
        RatingBar rb      = view.findViewById(R.id.rbRating);
        EditText etPrice  = view.findViewById(R.id.etPriceRange);
        EditText etPhone  = view.findViewById(R.id.etPhone);
        EditText etWeb    = view.findViewById(R.id.etWebsite);

        new AlertDialog.Builder(this)
                .setTitle("Add Restaurant")
                .setView(view)
                .setPositiveButton("Save", (d,w)-> {
                    String name = etName.getText().toString().trim();
                    if (TextUtils.isEmpty(name)) {
                        Toast.makeText(this, "Name required", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    JSONObject body = new JSONObject();
                    try {
                        body.put("name", name);
                        body.put("address", etAddr.getText().toString().trim());
                        body.put("rating", Math.max(1, Math.round(rb.getRating())));
                        body.put("priceRange", etPrice.getText().toString().trim());
                        body.put("phone", etPhone.getText().toString().trim());
                        body.put("website", etWeb.getText().toString().trim());
                    } catch (JSONException ignored) {}

                    String url = Api.RESTAURANTS + "?adminId=" + currentUserId;

                    JsonObjectRequest post = new JsonObjectRequest(
                            Request.Method.POST,
                            url,
                            body,
                            res -> loadRestaurants(),
                            err -> Toast.makeText(this, "Create failed", Toast.LENGTH_SHORT).show()
                    );
                    VolleySingleton.getInstance(this).addToRequestQueue(post);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /* ------------------------ EDIT / DELETE (ADMIN) ------------------------ */

    /**
     * Callback triggered when a restaurant is clicked. Opens an edit dialog,
     * allowing update or deletion of the restaurant entry.
     *
     * @param item restaurant item that was clicked
     */
    @Override
    public void onRestaurantClick(Restaurant item) {
        if (!isAdmin) {
            Toast.makeText(this, "Only admins can edit restaurants.", Toast.LENGTH_SHORT).show();
            return;
        }

        View view = getLayoutInflater().inflate(R.layout.dialog_restaurant, null, false);
        EditText etName   = view.findViewById(R.id.etName);
        EditText etAddr   = view.findViewById(R.id.etAddress);
        RatingBar rb      = view.findViewById(R.id.rbRating);
        EditText etPrice  = view.findViewById(R.id.etPriceRange);
        EditText etPhone  = view.findViewById(R.id.etPhone);
        EditText etWeb    = view.findViewById(R.id.etWebsite);

        etName.setText(item.name);
        etAddr.setText(item.address);
        rb.setRating(item.rating);
        etPrice.setText(item.priceRange);
        etPhone.setText(item.phone);
        etWeb.setText(item.website);

        new AlertDialog.Builder(this)
                .setTitle("Edit Restaurant")
                .setView(view)
                .setPositiveButton("Update", (d,w)-> {
                    JSONObject body = new JSONObject();
                    try {
                        body.put("name", etName.getText().toString().trim());
                        body.put("address", etAddr.getText().toString().trim());
                        body.put("rating", Math.max(1, Math.round(rb.getRating())));
                        body.put("priceRange", etPrice.getText().toString().trim());
                        body.put("phone", etPhone.getText().toString().trim());
                        body.put("website", etWeb.getText().toString().trim());
                    } catch (JSONException ignored) {}

                    String url = Api.restaurantById(item.id) + "?adminId=" + currentUserId;

                    JsonObjectRequest put = new JsonObjectRequest(
                            Request.Method.PUT,
                            url,
                            body,
                            res -> loadRestaurants(),
                            err -> Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show()
                    );
                    VolleySingleton.getInstance(this).addToRequestQueue(put);
                })
                .setNeutralButton("Delete", (d,w)-> {
                    String url = Api.restaurantById(item.id) + "?adminId=" + currentUserId;

                    StringRequest del = new StringRequest(
                            Request.Method.DELETE,
                            url,
                            res -> loadRestaurants(),
                            err -> Toast.makeText(this, "Delete failed", Toast.LENGTH_SHORT).show()
                    );
                    VolleySingleton.getInstance(this).addToRequestQueue(del);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
