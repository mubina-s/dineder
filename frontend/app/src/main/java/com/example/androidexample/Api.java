package com.example.androidexample;

/**
 * Api holds all backend endpoint URLs used by the frontend.
 * Centralizing URLs here makes it easy to change the server base URL.
 */
public class Api {

    // ----------------------------------------------------------
    // 🔗 BASE URL
    // ----------------------------------------------------------

    /** Base URL of the backend server. */
    public static final String BASE =
            "http://10.0.2.2:8080";

    // ----------------------------------------------------------
    // 👤 USERS + LOGIN
    // ----------------------------------------------------------
    public static final String USERS        = BASE + "/users";
    public static final String USERS_COUNT  = BASE + "/users/count";
    public static final String LOGIN        = BASE + "/users/login";
    public static final String SIGNup       = BASE + "/users/signup";
    public static String userById(long id)  { return USERS + "/" + id; }


    // ----------------------------------------------------------
    // 🍽️ RESTAURANTS
    // ----------------------------------------------------------

    /** Endpoint for all restaurants (GET for everyone). */
    public static final String RESTAURANTS = BASE + "/restaurants";

    /**
     * Builds endpoint URL for a specific restaurant by id.
     *
     * @param id restaurant id
     * @return full URL for that restaurant resource
     */
    public static String restaurantById(long id) { return RESTAURANTS + "/" + id; }

    // Leaderboard / trending / stats
    public static final String RESTAURANT_LEADERBOARD = BASE + "/restaurants/leaderboard";
    public static final String RESTAURANT_TRENDING    = BASE + "/restaurants/trending";


    // ----------------------------------------------------------
    // ⭐ APP REVIEWS (HTTP + WebSocket)
    // ----------------------------------------------------------

    /** Endpoint for all reviews. */
    public static final String REVIEWS        = BASE + "/api/reviews";
    public static final String REVIEWS_STATS  = REVIEWS + "/statistics";

    /**
     * Builds endpoint URL for a specific review by id.
     *
     * @param id review id
     * @return full URL for that review resource
     */
    public static String reviewById(long id)  { return REVIEWS + "/" + id; }

    // WebSocket (correct path based on your backend)
    public static final String WS_URL =
            "ws://10.0.2.2:8080/ws/app-reviews";


    // ----------------------------------------------------------
    // 🧑‍🤝‍🧑 FRIEND FEATURE / ADD FRIENDS
    // ----------------------------------------------------------
    public static final String FRIENDS = BASE + "/friends";
    public static String friendsOfUser(long id) { return FRIENDS + "/user/" + id; }
    public static final String FRIEND_SEARCH = BASE + "/friends/search"; // POST username
    public static String friendDelete(long id) { return FRIENDS + "/" + id; }


    // ----------------------------------------------------------
    // 🎮 SESSION + SWIPING + READY UP + FINAL RESULTS
    // ----------------------------------------------------------
    public static final String SESSIONS = BASE + "/sessions";
    public static String sessionById(long id) { return SESSIONS + "/" + id; }

    // Host creates a new session
    public static final String CREATE_SESSION = BASE + "/sessions/create";

    // User joins session
    public static String joinSession(String code) { return BASE + "/sessions/join/" + code; }

    // Mark Ready
    public static String markReady(long userId) { return BASE + "/sessions/ready/" + userId; }

    // Kick user
    public static String kickUser(long hostId, long userId) {
        return BASE + "/sessions/" + hostId + "/kick/" + userId;
    }

    // Final results
    public static String sessionResults(long sessionId) {
        return BASE + "/sessions/" + sessionId + "/results";
    }


    // ----------------------------------------------------------
    // 🧾 USER HISTORY + STATS
    // ----------------------------------------------------------

    /** User voting / history summary. Backend controller: @RequestMapping("/user") */
    public static String userHistory(long userId) {
        return BASE + "/user/" + userId + "/history";
    }

    public static String userStats(long userId) {
        return BASE + "/user/" + userId + "/stats";
    }


    // ----------------------------------------------------------
    // 🛠️ ADMIN
    // ----------------------------------------------------------
    public static final String ADMIN = BASE + "/admin";

    public static final String ADMIN_USERS = ADMIN + "/users";
    public static final String ADMIN_RESTAURANTS = ADMIN + "/restaurants";

    public static String adminUserById(long id) {
        return ADMIN_USERS + "/" + id;
    }

    public static String adminRestaurantById(long id) {
        return ADMIN_RESTAURANTS + "/" + id;
    }
}
