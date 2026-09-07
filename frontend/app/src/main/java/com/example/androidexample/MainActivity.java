package com.example.androidexample;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private LinearLayout sideMenu;
    private Button btnHome, btnProfileSettings, btnAppReviews,
            btnHostSession, btnJoinSession, btnRestaurants, btnDashboard, btnLogout,
            btnUserHistory;
    private TextView tvUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Toolbar with menu (hamburger) icon
        Toolbar toolbar = findViewById(R.id.topAppBar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setNavigationIcon(R.drawable.baseline_menu_24); // your menu icon from drawable
            toolbar.setNavigationOnClickListener(v -> toggleMenu());
        }

        // overlay menu layout
        sideMenu = findViewById(R.id.sideMenu);

        // side menu buttons
        btnHome            = findViewById(R.id.btnHome);
        btnProfileSettings = findViewById(R.id.btnProfileSettings);
        btnAppReviews      = findViewById(R.id.btnAppReviews);
        btnHostSession     = findViewById(R.id.btnHostSession);
        btnJoinSession     = findViewById(R.id.btnJoinSession);
        btnRestaurants     = findViewById(R.id.btnRestaurants);
        btnDashboard       = findViewById(R.id.btnDashboard);
        btnLogout          = findViewById(R.id.btnLogout);
        btnUserHistory     = findViewById(R.id.btnUserHistory);

        tvUsername         = findViewById(R.id.tvUsername);

        btnHome.setOnClickListener(this);
        btnProfileSettings.setOnClickListener(this);
        btnAppReviews.setOnClickListener(this);
        btnHostSession.setOnClickListener(this);
        btnJoinSession.setOnClickListener(this);
        btnRestaurants.setOnClickListener(this);
        btnDashboard.setOnClickListener(this);
        btnLogout.setOnClickListener(this);
        btnUserHistory.setOnClickListener(this);

        // read username + admin flag from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String username = prefs.getString("USERNAME", "Username");
        boolean isAdmin = prefs.getBoolean("IS_ADMIN", false);

        tvUsername.setText(username);

        // if not admin, hide Restaurants button
        if (!isAdmin && btnRestaurants != null) {
            btnRestaurants.setVisibility(View.GONE);
        }
    }

    /** Show / hide the overlay side menu when menu icon is tapped */
    private void toggleMenu() {
        if (sideMenu == null) return;
        if (sideMenu.getVisibility() == View.VISIBLE) {
            sideMenu.setVisibility(View.GONE);
        } else {
            sideMenu.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.btnHome) {
            // just close menu, already on Home
            toggleMenu();

        } else if (id == R.id.btnProfileSettings) {
            startActivity(new Intent(this, UserProfileSettingsActivity.class));

        } else if (id == R.id.btnAppReviews) {
            startActivity(new Intent(this, LoginReviews.class)); // or AppOverview

        } else if (id == R.id.btnHostSession) {
            startActivity(new Intent(this, SessionPrefsActivity.class));

        } else if (id == R.id.btnJoinSession) {
            startActivity(new Intent(this, UserJoinActivity.class));

        } else if (id == R.id.btnRestaurants) {
            // only visible for admins, so safe to open Restaurants screen
            startActivity(new Intent(this, RestaurantsActivity.class));
        } else if (id == R.id.btnDashboard) {
            startActivity(new Intent(this, DashboardActivity.class));
        } else if (id == R.id.btnUserHistory) {
            startActivity(new Intent(this, UserHistoryActivity.class));

        } else if (id == R.id.btnLogout) {
            // clear saved login and go back to LoginActivity
            SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
            prefs.edit().clear().apply();

            Intent i = new Intent(this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        }
    }
}
