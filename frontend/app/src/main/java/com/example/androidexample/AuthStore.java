package com.example.androidexample;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Small helper class to store role/userId/token across activities.
 */
public final class AuthStore {

    private static final String PREF   = "auth_store";
    private static final String K_ROLE = "role";
    private static final String K_USER = "user_id";
    private static final String K_TOKEN = "token";

    /**
     * Simple holder for authenticated user.
     */
    public static final class Auth {
        public final String role;
        public final long userId;
        public final String token;

        public Auth(String role, long userId, String token) {
            this.role = role;
            this.userId = userId;
            this.token = token;
        }
    }

    private AuthStore() {}

    /** Read auth info from SharedPreferences. Returns null if not logged in. */
    public static Auth read(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        String role  = sp.getString(K_ROLE, null);
        long userId  = sp.getLong(K_USER, -1L);
        String token = sp.getString(K_TOKEN, null);

        if (userId <= 0) {
            // treat as not logged in
            return null;
        }
        if (role == null) {
            role = "user";
        }
        return new Auth(role, userId, token);
    }

    /** Save current auth (after login). */
    public static void write(Context ctx, Auth a) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        sp.edit()
                .putString(K_ROLE, a.role)
                .putLong(K_USER, a.userId)
                .putString(K_TOKEN, a.token)
                .apply();
    }

    /** Clear auth (back to guest). */
    public static void clear(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        sp.edit().clear().apply();
    }
}
