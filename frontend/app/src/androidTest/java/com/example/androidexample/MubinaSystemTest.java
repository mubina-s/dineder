package com.example.androidexample;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * ONE big test class that:
 * - Tests Login, SignUp, Main, Restaurants, UserHistory, App Reviews
 * - Also touches Api, VolleySingleton, WebSocketManager
 * - Does NOT use real backend (no server needed)
 */
@RunWith(AndroidJUnit4.class)
public class MubinaSystemTest {

    @Before
    public void seedLoginState() {
        Context ctx = ApplicationProvider.getApplicationContext();

        // SharedPreferences used by LoginActivity / others
        SharedPreferences prefs = ctx.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        prefs.edit()
                .putBoolean("isLoggedIn", true)
                .putString("USERNAME", "mia")
                .putString("USERID", "1")
                .putString("PASSWORD", "miss mmm")
                .putString("EMAIL", "mia@example.com")
                .putString("NAME", "Mia Test")
                .putBoolean("IS_ADMIN", false)
                .apply();

        // auth_store used by AuthStore (if any)
        SharedPreferences auth = ctx.getSharedPreferences("auth_store", Context.MODE_PRIVATE);
        auth.edit()
                .putString("role", "user")
                .putLong("user_id", 1L)
                .putString("token", "dummy-token")
                .apply();
    }

    // ------------------------------------------------------------------
    //  LOGIN ACTIVITY
    // ------------------------------------------------------------------

    @Test
    public void loginActivity_basicUiAndTyping() {
        ActivityScenario.launch(LoginActivity.class);

        onView(withId(R.id.login_username_edt)).check(matches(isDisplayed()));
        onView(withId(R.id.login_password_edt)).check(matches(isDisplayed()));
        onView(withId(R.id.login_login_btn)).check(matches(isDisplayed()));

        onView(withId(R.id.login_username_edt)).perform(typeText("mia"));
        onView(withId(R.id.login_password_edt)).perform(typeText("miss mmm"));
    }

    @Test
    public void loginActivity_navigateToSignUp_doesNotCrash() {
        ActivityScenario.launch(LoginActivity.class);

        onView(withId(R.id.tvSignupText)).perform(click());
        // If it switches to SignUpActivity or finishes without crash → pass
    }

    // ------------------------------------------------------------------
    //  SIGN UP ACTIVITY
    // ------------------------------------------------------------------

    @Test
    public void signUpActivity_uiAndTypingAndBack() {
        ActivityScenario.launch(SignUpActivity.class);

        onView(withId(R.id.etUsername)).check(matches(isDisplayed()));
        onView(withId(R.id.etName)).check(matches(isDisplayed()));
        onView(withId(R.id.etEmail)).check(matches(isDisplayed()));
        onView(withId(R.id.etPassword)).check(matches(isDisplayed()));
        onView(withId(R.id.btnSignUp)).check(matches(isDisplayed()));

        onView(withId(R.id.etUsername)).perform(typeText("newuser"));
        onView(withId(R.id.etName)).perform(typeText("New User"));
        onView(withId(R.id.etEmail)).perform(typeText("new@example.com"));
        onView(withId(R.id.etPassword)).perform(typeText("12345678"));

        onView(withId(R.id.btnBack)).perform(click());
    }

    // ------------------------------------------------------------------
    //  MAIN ACTIVITY / DASHBOARD
    // ------------------------------------------------------------------

    @Test
    public void mainActivity_showsWelcomeAndUsername() {
        ActivityScenario.launch(MainActivity.class);

        onView(withId(R.id.imgDashboard)).check(matches(isDisplayed()));
        onView(withId(R.id.tvWelcomeBack)).check(matches(isDisplayed()));
        onView(withId(R.id.tvUsername)).check(matches(isDisplayed()));
    }

    // ------------------------------------------------------------------
    //  RESTAURANTS ACTIVITY
    // ------------------------------------------------------------------

    @Test
    public void restaurantsActivity_recyclerLoads() {
        ActivityScenario.launch(RestaurantsActivity.class);

        // RecyclerView is visible
        onView(withId(R.id.rvRestaurants)).check(matches(isDisplayed()));

        // fabAddRestaurant exists but is GONE by default, so we do NOT assert isDisplayed()
        // If later your code makes it visible, this test will still be fine.
    }

    // ------------------------------------------------------------------
    //  USER HISTORY ACTIVITY
    // ------------------------------------------------------------------

    @Test
    public void userHistoryActivity_headerVisible() {
        ActivityScenario.launch(UserHistoryActivity.class);

        onView(withId(R.id.tvHeaderTitle)).check(matches(isDisplayed()));
    }

    // ------------------------------------------------------------------
    //  APP REVIEWS (LoginReviews Activity)
    // ------------------------------------------------------------------

    @Test
    public void appReviewsActivity_showsTitleTextAndLoginButton() {
        ActivityScenario.launch(LoginReviews.class);

        onView(withText("App Reviews")).check(matches(isDisplayed()));
        onView(withId(R.id.btnLogin)).check(matches(isDisplayed()));
    }

    // ------------------------------------------------------------------
    //  API CLASS (URL HELPERS)
    // ------------------------------------------------------------------

    @Test
    public void api_adminUrlHelpers_doNotCrash() {
        String u1 = Api.adminUserById(5L);
        String u2 = Api.adminRestaurantById(10L);

        assert u1 != null;
        assert u2 != null;
    }

    // ------------------------------------------------------------------
    //  VOLLEY SINGLETON
    // ------------------------------------------------------------------

    @Test
    public void volleySingleton_instanceAndQueueDoNotCrash() {
        Context ctx = ApplicationProvider.getApplicationContext();

        VolleySingleton vs = VolleySingleton.getInstance(ctx);
        RequestQueue queue = vs.getRequestQueue();

        assert vs != null;
        assert queue != null;

        Request<Object> dummy = new Request<Object>(Request.Method.GET, "http://example.com", error -> {}) {
            @Override
            protected Response<Object> parseNetworkResponse(NetworkResponse response) {
                return Response.success(null, null);
            }

            @Override
            protected void deliverResponse(Object response) {
                // no-op
            }
        };

        vs.addToRequestQueue(dummy);
    }

    // ------------------------------------------------------------------
    //  WEBSOCKET MANAGER
    // ------------------------------------------------------------------

    @Test
    public void webSocketManager_basicMethodsDoNotCrash() {
        WebSocketManager mgr = WebSocketManager.getInstance();

        mgr.sendMessage("test message without real connection");
        mgr.disconnect();
    }
}
