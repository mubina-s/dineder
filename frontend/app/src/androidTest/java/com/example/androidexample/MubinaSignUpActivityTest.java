package com.example.androidexample;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.assertion.ViewAssertions.matches;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MubinaSignUpActivityTest {

    @Test
    public void signUpScreenLoads() {
        ActivityScenario.launch(SignUpActivity.class);

        onView(withId(R.id.etUsername)).check(matches(isDisplayed()));
        onView(withId(R.id.etName)).check(matches(isDisplayed()));
        onView(withId(R.id.etEmail)).check(matches(isDisplayed()));
        onView(withId(R.id.etPassword)).check(matches(isDisplayed()));
        onView(withId(R.id.btnSignUp)).check(matches(isDisplayed()));
    }

    @Test
    public void typingWorks() {
        ActivityScenario.launch(SignUpActivity.class);

        onView(withId(R.id.etUsername)).perform(typeText("mia"));
        onView(withId(R.id.etName)).perform(typeText("Mia"));
        onView(withId(R.id.etEmail)).perform(typeText("mia@example.com"));
        onView(withId(R.id.etPassword)).perform(typeText("12345678"));
    }

    @Test
    public void backButtonDoesNotCrash() {
        ActivityScenario.launch(SignUpActivity.class);
        onView(withId(R.id.btnBack)).perform(click());
    }
}
