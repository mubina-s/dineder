package com.example.androidexample;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static org.junit.Assert.*;

/**
 * Big combined test for your app.
 * - Espresso tests for Activities
 * - Pure Java tests for Api, Restaurant, Review, User, AuthStore, WebSocketManager, RestaurantsAdapter
 *
 * Put this file under:
 *   app/src/androidTest/java/com/example/androidexample/
 */
@RunWith(AndroidJUnit4.class)
public class MubinaFullSystemTest {

    private Context appContext;

    @Before
    public void setUpPrefs() {
        appContext = ApplicationProvider.getApplicationContext();

        // SharedPreferences used by MainActivity, RestaurantsActivity, UserHistory, Profile, etc.
        SharedPreferences prefs = appContext.getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        prefs.edit()
                .putBoolean("isLoggedIn", true)
                .putString("USERNAME", "mia")
                .putString("USERID", "1")
                .putString("PASSWORD", "miss mmm")
                .putString("EMAIL", "mia@example.com")
                .putString("NAME", "Mia Test")
                .putBoolean("IS_ADMIN", false)   // non-admin for safety
                .apply();

        // AuthStore prefs used by AppOverview / LoginReviews
        SharedPreferences auth = appContext.getSharedPreferences("auth_store", Context.MODE_PRIVATE);
        auth.edit()
                .putString("role", "user")
                .putLong("user_id", 1L)
                .putString("token", "dummy-token")
                .apply();
    }

    // ------------------------------------------------------------------------
    //  PURE-JAVA / HELPER CLASSES
    // ------------------------------------------------------------------------

    @Test
    public void api_allHelperMethodsProduceUrls() {
        long id1 = 5L;
        long id2 = 10L;
        long userId = 3L;
        long hostId = 9L;
        long sessionId = 22L;

        // Users / login
        assertTrue(Api.USERS.endsWith("/users"));
        assertTrue(Api.USERS_COUNT.endsWith("/users/count"));
        assertTrue(Api.LOGIN.endsWith("/users/login"));
        assertTrue(Api.SIGNup.endsWith("/users/signup"));
        assertTrue(Api.userById(id1).endsWith("/users/" + id1));

        // Restaurants
        assertTrue(Api.RESTAURANTS.endsWith("/restaurants"));
        assertTrue(Api.restaurantById(id2).endsWith("/restaurants/" + id2));
        assertTrue(Api.RESTAURANT_LEADERBOARD.endsWith("/restaurants/leaderboard"));
        assertTrue(Api.RESTAURANT_TRENDING.endsWith("/restaurants/trending"));

        // Reviews + WebSocket
        assertTrue(Api.REVIEWS.endsWith("/api/reviews"));
        assertTrue(Api.REVIEWS_STATS.endsWith("/api/reviews/statistics"));
        assertTrue(Api.reviewById(id1).endsWith("/api/reviews/" + id1));
        assertTrue(Api.WS_URL.startsWith("ws://"));

        // Friends
        assertTrue(Api.FRIENDS.endsWith("/friends"));
        assertTrue(Api.friendsOfUser(userId).endsWith("/friends/user/" + userId));
        assertTrue(Api.FRIEND_SEARCH.endsWith("/friends/search"));
        assertTrue(Api.friendDelete(id1).endsWith("/friends/" + id1));

        // Sessions
        assertTrue(Api.SESSIONS.endsWith("/sessions"));
        assertTrue(Api.sessionById(sessionId).endsWith("/sessions/" + sessionId));
        assertTrue(Api.CREATE_SESSION.endsWith("/sessions/create"));
        assertTrue(Api.joinSession("ABC123").endsWith("/sessions/join/ABC123"));
        assertTrue(Api.markReady(userId).endsWith("/sessions/ready/" + userId));
        assertTrue(Api.kickUser(hostId, userId)
                .endsWith("/sessions/" + hostId + "/kick/" + userId));
        assertTrue(Api.sessionResults(sessionId)
                .endsWith("/sessions/" + sessionId + "/results"));

        // User history / stats
        assertTrue(Api.userHistory(userId).endsWith("/user/" + userId + "/history"));
        assertTrue(Api.userStats(userId).endsWith("/user/" + userId + "/stats"));

        // Admin
        assertTrue(Api.ADMIN.endsWith("/admin"));
        assertTrue(Api.ADMIN_USERS.endsWith("/admin/users"));
        assertTrue(Api.ADMIN_RESTAURANTS.endsWith("/admin/restaurants"));
        assertTrue(Api.adminUserById(id1).endsWith("/admin/users/" + id1));
        assertTrue(Api.adminRestaurantById(id2).endsWith("/admin/restaurants/" + id2));
    }

    @Test
    public void restaurant_modelConstructorsAndToString() {
        Restaurant r0 = new Restaurant();
        assertNull(r0.name);

        Restaurant r1 = new Restaurant("TestName");
        assertEquals("TestName", r1.name);

        Restaurant r2 = new Restaurant(
                1L,
                "Burger Place",
                4,
                "$$",
                "123 Main St",
                "555-1234",
                "https://burger.com",
                null,
                "image.png"
        );

        assertEquals(Long.valueOf(1L), r2.id);
        assertEquals("Burger Place", r2.name);
        assertEquals(4, r2.rating);
        assertEquals("$$", r2.priceRange);
        assertEquals("123 Main St", r2.address);
        assertEquals("555-1234", r2.phone);
        assertEquals("https://burger.com", r2.website);
        assertEquals("image.png", r2.image);

        String s = r2.toString();
        assertTrue(s.contains("Burger Place"));
    }

    @Test
    public void review_fromJsonHandlesFields() throws Exception {
        JSONObject userObj = new JSONObject();
        userObj.put("name", "Mubina");

        JSONObject o = new JSONObject();
        o.put("id", 100);
        o.put("userId", 200);
        o.put("rating", 10); // will be clamped to 5
        o.put("review", "Amazing app!");
        o.put("user", userObj);

        Review r = Review.fromJson(o);
        assertEquals(100, r.id);
        assertEquals(200, r.userId);
        assertEquals("Mubina", r.author);
        assertEquals(5, r.stars);
        assertEquals("Amazing app!", r.content);
    }

    @Test
    public void user_modelMethodsWork() {
        User u1 = new User(1, "mia", "pass", "m@example.com", "Mia", true);
        assertEquals("Mia", u1.getName());
        assertEquals(1, u1.getUserId());
        assertEquals("Mia READY", u1.sessionPar());
        assertEquals("Mia", u1.toString());

        User u2 = new User("Guest");
        assertEquals("Guest", u2.getName());
        assertEquals("Guest NOT READY", u2.sessionPar());
    }

    @Test
    public void authStore_writeReadAndClear() {
        AuthStore.Auth a = new AuthStore.Auth("admin", 99L, "token-123");
        AuthStore.write(appContext, a);

        AuthStore.Auth loaded = AuthStore.read(appContext);
        assertNotNull(loaded);
        assertEquals("admin", loaded.role);
        assertEquals(99L, loaded.userId);
        assertEquals("token-123", loaded.token);

        AuthStore.clear(appContext);
        AuthStore.Auth cleared = AuthStore.read(appContext);
        assertNull(cleared);
    }

    @Test
    public void webSocketEvents_constantsAccessible() {
        assertEquals("CREATE_REVIEW", WebSocketEvents.CREATE_REVIEW);
        assertEquals("UPDATE_REVIEW", WebSocketEvents.UPDATE_REVIEW);
        assertEquals("DELETE_REVIEW", WebSocketEvents.DELETE_REVIEW);
    }

    @Test
    public void webSocketManager_sendAndDisconnect_noCrash() {
        WebSocketManager mgr = WebSocketManager.getInstance();
        boolean sent = mgr.sendMessage("test"); // may be false, only care it doesn't crash
        mgr.disconnect();
    }

    @Test
    public void restaurantsAdapter_submitUpdatesList() {
        RestaurantsAdapter adapter = new RestaurantsAdapter(appContext, item -> {
            // no-op
        });

        List<Restaurant> list = new ArrayList<>();
        list.add(new Restaurant("R1"));
        list.add(new Restaurant("R2"));

        adapter.submit(list);
        assertEquals(2, adapter.getItemCount());

        adapter.submit(new ArrayList<>());
        assertEquals(0, adapter.getItemCount());
    }

    // ------------------------------------------------------------------------
    //  ESPRESSO TESTS – ACTIVITIES
    // ------------------------------------------------------------------------

    @Test
    public void loginActivity_basicUiAndSignUpNavigation() {
        ActivityScenario.launch(LoginActivity.class);

        onView(withId(R.id.login_username_edt)).check(matches(isDisplayed()));
        onView(withId(R.id.login_password_edt)).check(matches(isDisplayed()));
        onView(withId(R.id.login_login_btn)).check(matches(isDisplayed()));

        // Fill fields (no need to actually log in)
        onView(withId(R.id.login_username_edt)).perform(typeText("mia"), closeSoftKeyboard());
        onView(withId(R.id.login_password_edt)).perform(typeText("miss mmm"), closeSoftKeyboard());

        // Tap "Sign up" text to exercise navigation path
        onView(withId(R.id.tvSignupText)).perform(click());
    }

    @Test
    public void signUpActivity_fillFieldsAndSubmit() {
        ActivityScenario.launch(SignUpActivity.class);

        onView(withId(R.id.etUsername)).check(matches(isDisplayed()));
        onView(withId(R.id.etEmail)).check(matches(isDisplayed()));
        onView(withId(R.id.etName)).check(matches(isDisplayed()));
        onView(withId(R.id.etPassword)).check(matches(isDisplayed()));

        onView(withId(R.id.etUsername)).perform(typeText("newuser"), closeSoftKeyboard());
        onView(withId(R.id.etEmail)).perform(typeText("new@example.com"), closeSoftKeyboard());
        onView(withId(R.id.etName)).perform(typeText("New User"), closeSoftKeyboard());
        onView(withId(R.id.etPassword)).perform(typeText("password123"), closeSoftKeyboard());

        onView(withId(R.id.btnSignUp)).check(matches(isDisplayed()));
        onView(withId(R.id.btnSignUp)).perform(click());
    }

    @Test
    public void loginReviewsActivity_openLoginDialog() {
        ActivityScenario.launch(LoginReviews.class);

        onView(withId(R.id.btnLogin)).check(matches(isDisplayed()));
        onView(withId(R.id.btnLogin)).perform(click());
        // If dialog appears and no crash, showLoginDialog + doLogin path are executed.
    }

    @Test
    public void mainActivity_showsUsernameFromPrefs() {
        ActivityScenario.launch(MainActivity.class);

        onView(withId(R.id.tvUsername)).check(matches(isDisplayed()));
    }

    @Test
    public void restaurantsActivity_recyclerVisible() {
        ActivityScenario.launch(RestaurantsActivity.class);

        onView(withId(R.id.rvRestaurants)).check(matches(isDisplayed()));
        // Do NOT assert on fabAddRestaurant visibility (it may be GONE for non-admin)
    }

    @Test
    public void appOverviewActivity_basicUiAndSubmitValidation() {
        ActivityScenario.launch(AppOverview.class);

        onView(withId(R.id.txtTotalUsers)).check(matches(isDisplayed()));
        onView(withId(R.id.reviewsContainer)).check(matches(isDisplayed()));
        onView(withId(R.id.ratingBar)).check(matches(isDisplayed()));
        onView(withId(R.id.reviewInput)).check(matches(isDisplayed()));
        onView(withId(R.id.btnSubmit)).check(matches(isDisplayed()));

        // Trigger validation branch (empty stars/text)
        onView(withId(R.id.btnSubmit)).perform(click());
    }

    @Test
    public void userHistoryActivity_basicUi() {
        ActivityScenario.launch(UserHistoryActivity.class);

        onView(withId(R.id.tvTopRestaurantsList)).check(matches(isDisplayed()));
        onView(withId(R.id.tvRecentList)).check(matches(isDisplayed()));
        onView(withId(R.id.tvTotalLikes)).check(matches(isDisplayed()));
    }

    @Test
    public void userProfileSettingsActivity_editAndSave() {
        ActivityScenario.launch(UserProfileSettingsActivity.class);

        onView(withId(R.id.editUsername)).check(matches(isDisplayed()));
        onView(withId(R.id.editName)).check(matches(isDisplayed()));
        onView(withId(R.id.editEmail)).check(matches(isDisplayed()));

        onView(withId(R.id.editBtn)).perform(click());
        onView(withId(R.id.saveBtn)).perform(click());
    }

    @Test
    public void roleActivity_buttonsVisible() {
        ActivityScenario.launch(RoleActivity.class);

        onView(withId(R.id.host_btn)).check(matches(isDisplayed()));
        onView(withId(R.id.join_btn)).check(matches(isDisplayed()));
        onView(withId(R.id.dashboard_btn)).check(matches(isDisplayed()));
    }
}
