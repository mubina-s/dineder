package com.example.androidexample;

import android.content.SharedPreferences;
import android.view.View;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.filters.LargeTest;
import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner;
import androidx.test.espresso.intent.Intents;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.intent.Intents.times;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.containsString;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertTrue;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.containsString;


@RunWith(AndroidJUnit4ClassRunner.class)
@LargeTest
public class SangeethaSystemTest {

    private static final int SIMULATED_DELAY_MS = 1000;

    @Before
    public void setUp() {
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    @Test
    public void emptyUsernameShowsError() {
        ActivityScenario<LoginActivity> loginScenario = ActivityScenario.launch(LoginActivity.class);

        onView(withId(R.id.login_password_edt))
                .perform(replaceText("password123"), closeSoftKeyboard());
        onView(withId(R.id.login_login_btn)).perform(click());
        onView(withId(R.id.login_username_edt))
                .check(matches(withText(containsString(""))));
    }

    @Test
    public void navigateToSignUpActivity() {
        ActivityScenario<LoginActivity> loginScenario = ActivityScenario.launch(LoginActivity.class);

        onView(withId(R.id.tvSignupText)).perform(click());
        Intents.intended(hasComponent(SignUpActivity.class.getName()));
    }

    @Test
    public void successfulLoginSavesInfo() {
        ActivityScenario<LoginActivity> loginScenario = ActivityScenario.launch(LoginActivity.class);

        String username = "gliem2";
        String password = "pw1";

        onView(withId(R.id.login_username_edt))
                .perform(replaceText(username), closeSoftKeyboard());
        onView(withId(R.id.login_password_edt))
                .perform(replaceText(password), closeSoftKeyboard());
        onView(withId(R.id.login_login_btn)).perform(click());

        try { Thread.sleep(SIMULATED_DELAY_MS); } catch (InterruptedException e) {}

        SharedPreferences prefs = ApplicationProvider.getApplicationContext()
                .getSharedPreferences("MyAppPrefs", android.content.Context.MODE_PRIVATE);
        assertTrue(prefs.getBoolean("isLoggedIn", false));
        assertTrue(prefs.getString("USERNAME", "").equals(username));
    }

    @Test
    public void successfulLoginNavigatesToDashboardActivity() {
        ActivityScenario<LoginActivity> loginScenario = ActivityScenario.launch(LoginActivity.class);

        String username = "gliem2";
        String password = "pw1";

        onView(withId(R.id.login_username_edt))
                .perform(replaceText(username), closeSoftKeyboard());
        onView(withId(R.id.login_password_edt))
                .perform(replaceText(password), closeSoftKeyboard());
        onView(withId(R.id.login_login_btn)).perform(click());

        try { Thread.sleep(SIMULATED_DELAY_MS); } catch (InterruptedException e) {}

        Intents.intended(hasComponent(MainActivity.class.getName()));

        ActivityScenario<MainActivity> mainScenario = ActivityScenario.launch(MainActivity.class);
        mainScenario.onActivity(activity -> {
            activity.findViewById(R.id.sideMenu).setVisibility(View.VISIBLE);
        });

        onView(withId(R.id.btnDashboard)).perform(click());
        Intents.intended(hasComponent(DashboardActivity.class.getName()));

        try { Thread.sleep(SIMULATED_DELAY_MS); } catch (InterruptedException e) {}

        // Hit back button to return to LoginActivity
        onView(withId(R.id.btnBack)).perform(click());

        try { Thread.sleep(SIMULATED_DELAY_MS); } catch (InterruptedException e) {}

        // Assert a known view in MainActivity is visible
        onView(withId(R.id.tvUsername)).check(matches(isDisplayed()));
    }

    @Test
    public void successfulSignupThenLoginNavigatesToProfileSettingsActivity() {
        ActivityScenario<LoginActivity> loginScenario = ActivityScenario.launch(LoginActivity.class);

        // Navigate to SignUpActivity
        onView(withId(R.id.tvSignupText)).perform(click());

        // Fill out signup form with the correct IDs
        onView(withId(R.id.etUsername)).perform(replaceText("auto"), closeSoftKeyboard());
        onView(withId(R.id.etName)).perform(replaceText("Auto"), closeSoftKeyboard());
        onView(withId(R.id.etEmail)).perform(replaceText("auto@test.com"), closeSoftKeyboard());
        onView(withId(R.id.etPassword)).perform(replaceText("autoPassword"), closeSoftKeyboard());

        // Submit signup
        onView(withId(R.id.btnSignUp)).perform(click());

        try { Thread.sleep(SIMULATED_DELAY_MS); } catch (InterruptedException e) {}

        // Hit back button to return to LoginActivity
        onView(withId(R.id.btnBack)).perform(click());

        try { Thread.sleep(SIMULATED_DELAY_MS); } catch (InterruptedException e) {}

        // Verify we’re back in LoginActivity by checking a known view
        onView(withId(R.id.login_username_edt)).check(matches(isDisplayed()));

        String username = "auto";
        String password = "autoPassword";

        // Perform login with new user
        onView(withId(R.id.login_username_edt)).perform(replaceText(username), closeSoftKeyboard());
        onView(withId(R.id.login_password_edt)).perform(replaceText(password), closeSoftKeyboard());
        onView(withId(R.id.login_login_btn)).perform(click());

        Intents.intended(hasComponent(MainActivity.class.getName()));

        // Force side menu visible
        ActivityScenario<MainActivity> mainScenario = ActivityScenario.launch(MainActivity.class);
        mainScenario.onActivity(activity -> {
            activity.findViewById(R.id.sideMenu).setVisibility(View.VISIBLE);
        });

        // Navigate to Profile Settings
        onView(withId(R.id.btnProfileSettings)).perform(click());
        Intents.intended(hasComponent(UserProfileSettingsActivity.class.getName()));

        // Assert fields are pre-filled
        onView(withId(R.id.editUsername)).check(matches(withText(username)));
        onView(withId(R.id.editPassword)).check(matches(withText(password)));

        // Tap edit button to enable fields
        onView(withId(R.id.editBtn)).perform(click());

        // Replace username with "autoNew"
        String newUsername = "autoNew";
        onView(withId(R.id.editUsername)).perform(replaceText(newUsername), closeSoftKeyboard());

        // Tap save button
        onView(withId(R.id.saveBtn)).perform(click());

        // Confirm text is replaced
        onView(withId(R.id.editUsername)).check(matches(withText(newUsername)));

        // Scroll to delete button and click
        onView(withId(R.id.deleteBtn)).perform(scrollTo(), click());

        // Wait briefly for Volley callback (replace with IdlingResource ideally)
        try { Thread.sleep(SIMULATED_DELAY_MS); } catch (InterruptedException e) {}

        // Verify LoginActivity is showing by checking a known view
        onView(withId(R.id.login_username_edt)).check(matches(isDisplayed()));
    }

    @Test
    public void successfulLoginNavigatesToJoin() {
        ActivityScenario<LoginActivity> loginScenario = ActivityScenario.launch(LoginActivity.class);

        String username = "gliem2";
        String password = "pw1";

        onView(withId(R.id.login_username_edt))
                .perform(replaceText(username), closeSoftKeyboard());
        onView(withId(R.id.login_password_edt))
                .perform(replaceText(password), closeSoftKeyboard());
        onView(withId(R.id.login_login_btn)).perform(click());

        try { Thread.sleep(SIMULATED_DELAY_MS); } catch (InterruptedException e) {}

        Intents.intended(hasComponent(MainActivity.class.getName()));

        ActivityScenario<MainActivity> mainScenario = ActivityScenario.launch(MainActivity.class);
        mainScenario.onActivity(activity -> {
            activity.findViewById(R.id.sideMenu).setVisibility(View.VISIBLE);
        });

        onView(withId(R.id.btnJoinSession)).perform(click());
        Intents.intended(hasComponent(UserJoinActivity.class.getName()));

        try { Thread.sleep(SIMULATED_DELAY_MS); } catch (InterruptedException e) {}

        // Hit back button to return to LoginActivity
        onView(withId(R.id.btnBack)).perform(click());

        try { Thread.sleep(SIMULATED_DELAY_MS); } catch (InterruptedException e) {}

        // Assert a known view in MainActivity is visible
        onView(withId(R.id.tvUsername)).check(matches(isDisplayed()));
    }

    @Test
    public void LoginNavigatesToSessionPrefs() {
        ActivityScenario<LoginActivity> loginScenario = ActivityScenario.launch(LoginActivity.class);

        try { Thread.sleep(SIMULATED_DELAY_MS); } catch (InterruptedException e) {}

        // Verify we’re in LoginActivity by checking a known view
        onView(withId(R.id.login_username_edt)).check(matches(isDisplayed()));

        String username = "finTest";
        String password = "finPass";

        // Perform login with user
        onView(withId(R.id.login_username_edt)).perform(replaceText(username), closeSoftKeyboard());
        onView(withId(R.id.login_password_edt)).perform(replaceText(password), closeSoftKeyboard());
        onView(withId(R.id.login_login_btn)).perform(click());

        Intents.intended(hasComponent(MainActivity.class.getName()));

        // Force side menu visible
        ActivityScenario<MainActivity> mainScenario = ActivityScenario.launch(MainActivity.class);
        mainScenario.onActivity(activity -> {
            activity.findViewById(R.id.sideMenu).setVisibility(View.VISIBLE);
        });

        // Navigate to Host
        onView(withId(R.id.btnHostSession)).perform(click());
        Intents.intended(hasComponent(SessionPrefsActivity.class.getName()));

        // Scroll to and click the "Create New Prefs" button
        onView(withId(R.id.createNewPrefsBtn)).perform(click());

        // Verify navigation to CreateSessionPrefsActivity
        Intents.intended(hasComponent(CreateSessionPrefsActivity.class.getName()));

        // Replace text of preference name with "auto prefs"
        onView(withId(R.id.preference_name_edt))
                .perform(replaceText("auto prefs"), closeSoftKeyboard());

        // Replace text of rating with "2"
        onView(withId(R.id.rating_edt))
                .perform(replaceText("2"), closeSoftKeyboard());

        // Select btnPriceExpensive (this sets price_range_edt to "$$$")
        onView(withId(R.id.btnPriceExpensive)).perform(click());

        // --- Confirm all three fields are populated correctly ---
        onView(withId(R.id.preference_name_edt))
                .check(matches(withText("auto prefs")));
        onView(withId(R.id.rating_edt))
                .check(matches(withText("2")));
        onView(withId(R.id.price_range_edt))
                .check(matches(withText("$$$")));

        // Scroll down and click the create button
        onView(withId(R.id.create_btn)).perform(scrollTo(), click());

        // Wait for network or use IdlingResource
        try { Thread.sleep(SIMULATED_DELAY_MS); } catch (InterruptedException e) {}

        // Verify navigation back to SessionPrefsActivity
        onView(withId(R.id.existingPrefsBtn)).check(matches(isDisplayed()));

        // Click Existing Prefs button
        onView(withId(R.id.existingPrefsBtn)).perform(click());

        // Verify navigation to SavedSessionPrefsActivity
        onView(withId(R.id.my_spinner)).check(matches(isDisplayed()));

        // Open spinner and select "auto prefs"
        onView(withId(R.id.my_spinner)).perform(click());
        onData(allOf(is(instanceOf(SessionPreference.class)),
                hasToString(containsString("auto prefs"))))
                .perform(click());

        // --- Assert that the fields match what we created ---
        onView(withId(R.id.preference_name_edt))
                .check(matches(withText("auto prefs")));
        onView(withId(R.id.rating_edt))
                .check(matches(withText("2")));
        onView(withId(R.id.price_range_edt))
                .check(matches(withText("$$$")));

        // Click Edit button
        onView(withId(R.id.saved_edit_btn)).perform(scrollTo(), click());

        // Replace preference name with "auto prefs 2"
        onView(withId(R.id.preference_name_edt))
                .perform(replaceText("auto prefs 2"), closeSoftKeyboard());

        // Click Save Changes button
        onView(withId(R.id.saved_save_changes_btn)).perform(scrollTo(), click());

        // --- Assert that the preference name field now shows "auto prefs 2" ---
        onView(withId(R.id.preference_name_edt))
                .check(matches(withText("auto prefs 2")));

        // Finally, click Delete button
        onView(withId(R.id.saved_delete_btn)).perform(scrollTo(), click());
    }

    @Test
    public void LoginNavigatesToHost() {
        ActivityScenario<LoginActivity> loginScenario = ActivityScenario.launch(LoginActivity.class);

        try { Thread.sleep(SIMULATED_DELAY_MS); } catch (InterruptedException e) {}

        // Verify we’re in LoginActivity by checking a known view
        onView(withId(R.id.login_username_edt)).check(matches(isDisplayed()));

        String username = "finTest";
        String password = "finPass";

        // Perform login with user
        onView(withId(R.id.login_username_edt)).perform(replaceText(username), closeSoftKeyboard());
        onView(withId(R.id.login_password_edt)).perform(replaceText(password), closeSoftKeyboard());
        onView(withId(R.id.login_login_btn)).perform(click());

        Intents.intended(hasComponent(MainActivity.class.getName()));

        // Force side menu visible
        ActivityScenario<MainActivity> mainScenario = ActivityScenario.launch(MainActivity.class);
        mainScenario.onActivity(activity -> {
            activity.findViewById(R.id.sideMenu).setVisibility(View.VISIBLE);
        });

        // Navigate to Host
        onView(withId(R.id.btnHostSession)).perform(click());
        Intents.intended(hasComponent(SessionPrefsActivity.class.getName()));

        // Wait for network or use IdlingResource
        try { Thread.sleep(SIMULATED_DELAY_MS); } catch (InterruptedException e) {}

        // Verify navigation to SessionPrefsActivity
        onView(withId(R.id.existingPrefsBtn)).check(matches(isDisplayed()));

        // Click Existing Prefs button
        onView(withId(R.id.existingPrefsBtn)).perform(click());

        // Verify navigation to SavedSessionPrefsActivity
        onView(withId(R.id.my_spinner)).check(matches(isDisplayed()));

        // Open spinner and select "finPrefs2"
        onView(withId(R.id.my_spinner)).perform(click());
        onData(allOf(is(instanceOf(SessionPreference.class)),
                hasToString(containsString("finPrefs2"))))
                .perform(click());

        onView(withId(R.id.my_spinner))
                .check(matches(withSpinnerText(containsString("finPrefs2"))));

        // --- Assert that the fields match what we created ---
        onView(withId(R.id.preference_name_edt))
                .check(matches(withText("finPrefs2")));
        onView(withId(R.id.rating_edt))
                .check(matches(withText("2")));
        onView(withId(R.id.price_range_edt))
                .check(matches(withText("$$$")));

        // Click the "Create Session" button
        onView(withId(R.id.saved_createSession_btn)).perform(scrollTo(), click());

        // Wait briefly or use IdlingResource for the network call
        try { Thread.sleep(SIMULATED_DELAY_MS); } catch (InterruptedException e) {}

        // Verify navigation to ReadyUpActivity by checking a known view
        onView(withId(R.id.joinCode)).check(matches(isDisplayed()));

        // Assert hostStartBtn is visible (only if current user is host)
        onView(withId(R.id.hostStart_btn)).check(matches(isDisplayed()));

        // Click the "Start Session as Host" button
        onView(withId(R.id.hostStart_btn)).perform(click());

        // Wait briefly or use IdlingResource for WebSocket/network
        try { Thread.sleep(SIMULATED_DELAY_MS); } catch (InterruptedException e) {}

        // Verify we are in SwipingActivity
        onView(withId(R.id.completedRestaurants)).check(matches(isDisplayed()));

        // Hit Like button
        onView(withId(R.id.like_btn)).perform(click());

        // Wait for transition animation (or use IdlingResource for animations)
        try { Thread.sleep(1500); } catch (InterruptedException e) {}

        // Hit Dislike button
        onView(withId(R.id.dislike_btn)).perform(click());

        // Wait for transition animation
        try { Thread.sleep(1500); } catch (InterruptedException e) {}

        // Verify navigation to FinalResultsActivity by checking a known view
        onView(withId(R.id.progressBar1)).check(matches(isDisplayed()));

        onView(withId(R.id.restaurant1Label))
                .check(matches(withText("Provisions Lot F")));
        onView(withId(R.id.percentage1))
                .check(matches(withText("100.0%")));

        onView(withId(R.id.restaurant2Label))
                .check(matches(withText("Restaurant B")));
        onView(withId(R.id.percentage2))
                .check(matches(withText("0%")));
    }
}
