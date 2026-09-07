package com.example.androidexample;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.assertion.ViewAssertions.matches;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MubinaMainActivityNavigationTest {

    @Before
    public void fakeLogin() {
        Context ctx = ApplicationProvider.getApplicationContext();
        SharedPreferences prefs = ctx.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);

        prefs.edit()
                .putBoolean("isLoggedIn", true)
                .putString("USERNAME", "mia")
                .apply();
    }

    @Test
    public void mainMenuLoads() {
        ActivityScenario.launch(MainActivity.class);
        onView(withId(R.id.imgDashboard)).check(matches(isDisplayed()));
    }

    @Test
    public void openSideMenu() {
        ActivityScenario.launch(MainActivity.class);

        onView(withId(R.id.topAppBar)).perform(click());
        // If no crash → pass
    }
}
