package com.example.androidexample;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MubinaLoginActivityTest {

    @Before
    public void clearPrefs() {
        Context ctx = ApplicationProvider.getApplicationContext();
        SharedPreferences prefs = ctx.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }

    @Test
    public void loginScreenLoads() {
        ActivityScenario.launch(LoginActivity.class);

        onView(withId(R.id.login_username_edt)).check(matches(isDisplayed()));
        onView(withId(R.id.login_password_edt)).check(matches(isDisplayed()));
        onView(withId(R.id.login_login_btn)).check(matches(isDisplayed()));
    }

    @Test
    public void typingWorks() {
        ActivityScenario.launch(LoginActivity.class);

        onView(withId(R.id.login_username_edt)).perform(typeText("mia"));
        onView(withId(R.id.login_password_edt)).perform(typeText("miss mmm"));
    }

    @Test
    public void navigateToSignUp() {
        ActivityScenario.launch(LoginActivity.class);

        onView(withId(R.id.tvSignupText)).perform(click());

        // If it doesn't crash — test passes
    }
}
