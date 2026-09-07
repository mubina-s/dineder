package com.example.androidexample;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.Visibility.GONE;

import static org.hamcrest.Matchers.not;

/**
 * System tests for main flows:
 *  - Login validation (empty fields)
 *  - Typing login data
 *  - Guest behavior on AppOverview
 *  - Non-admin behavior on RestaurantsActivity
 *
 * Make sure these IDs exist:
 *  - activity_login.xml: login_username_edt, login_password_edt, login_login_btn
 *  - activity_app_overview.xml: btnSubmit
 *  - activity_restaurants.xml: fabAddRestaurant
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class MubinaSadriddinovaSystemTest {

    /** Helper: clear all login/admin state before some tests. */
    private void clearAuthState() {
        Context ctx = ApplicationProvider.getApplicationContext();

        // Clear AuthStore (used by AppOverview)
        AuthStore.clear(ctx);

        // Clear MyAppPrefs (used by RestaurantsActivity for IS_ADMIN)
        SharedPreferences prefs = ctx.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        prefs.edit().clear().commit();
    }

    /**
     * When user tries to login with empty username and password,
     * the EditTexts should show error messages.
     */
    @Test
    public void emptyLogin_showsFieldErrors() {
        ActivityScenario.launch(LoginActivity.class);

        // click login with both fields empty
        onView(withId(R.id.login_login_btn)).perform(click());

        // check errors
        onView(withId(R.id.login_username_edt))
                .check(matches(hasErrorText("Username is required")));

        onView(withId(R.id.login_password_edt))
                .check(matches(hasErrorText("Password is required")));
    }

    /**
     * User can type username and password and see the login button.
     * (We don't assert backend result here, just UI behavior.)
     */
    @Test
    public void userCanTypeLoginFields() {
        ActivityScenario.launch(LoginActivity.class);

        onView(withId(R.id.login_username_edt))
                .perform(typeText("testuser"), closeSoftKeyboard());

        onView(withId(R.id.login_password_edt))
                .perform(typeText("password123"), closeSoftKeyboard());

        // login button is visible
        onView(withId(R.id.login_login_btn))
                .check(matches(isDisplayed()));
    }

    /**
     * Guest opening AppOverview should not be able to submit a review,
     * so the submit button is disabled.
     */
    @Test
    public void guestOnAppOverview_submitDisabled() {
        clearAuthState();

        ActivityScenario.launch(AppOverview.class);

        onView(withId(R.id.btnSubmit))
                .check(matches(not(isEnabled())));
    }

    /**
     * Non-admin user opening RestaurantsActivity should not see the
     * "Add restaurant" FAB (it should be GONE).
     */
    @Test
    public void nonAdmin_restaurants_cannotSeeAddFab() {
        clearAuthState();

        ActivityScenario.launch(RestaurantsActivity.class);

        onView(withId(R.id.fabAddRestaurant))
                .check(matches(withEffectiveVisibility(GONE)));
    }
}
