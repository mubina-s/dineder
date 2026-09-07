package com.example.androidexample;

import org.json.JSONObject;

/**
 * Represents a user-written review inside the DineDer app.
 * <p>
 * A {@code Review} stores:
 * </p>
 * <ul>
 *     <li>Unique review identifier</li>
 *     <li>Author (user) information</li>
 *     <li>Star rating value</li>
 *     <li>Free-text review content</li>
 * </ul>
 * <p>
 * It also provides a helper method {@link #fromJson(JSONObject)} to
 * convert a JSON object returned by the backend into a {@code Review}
 * instance, safely handling different field names that might be used by
 * the API.
 * </p>
 *
 * @author Mubina Sadriddinova
 */
public class Review {

    /** Unique review identifier. */
    public long id;

    /** ID of the user who wrote this review. */
    public long userId;

    /** Author name, or "Anonymous" if unknown. */
    public String author;

    /** Number of stars given in the review (0–5). */
    public int stars;

    /** Review text content written by the user. */
    public String content;

    /**
     * Creates a {@code Review} object from a JSON response sent by the backend.
     * <p>
     * This method safely reads multiple possible JSON fields, because
     * backend responses sometimes use different names. It handles:
     * </p>
     * <ul>
     *     <li>{@code rating} / {@code stars}</li>
     *     <li>{@code review} / {@code content} / {@code text} / {@code comment}</li>
     *     <li>{@code author} / {@code username} / {@code user.username} / {@code user.name}</li>
     * </ul>
     *
     * @param o JSON object containing review data
     * @return fully constructed {@code Review} instance; never {@code null}
     */
    public static Review fromJson(JSONObject o) {
        Review r = new Review();

        // ----- IDs -----
        r.id = o.optLong("id", -1);
        r.userId = o.optLong("userId", -1);

        // ----- AUTHOR -----
        String author = o.optString("author", null);
        if (author == null || author.isEmpty() || "null".equalsIgnoreCase(author)) {
            author = o.optString("username", null);
        }
        if (author == null || author.isEmpty() || "null".equalsIgnoreCase(author)) {
            JSONObject userObj = o.optJSONObject("user");
            if (userObj != null) {
                author = userObj.optString("username", null);
                if (author == null || author.isEmpty()) {
                    author = userObj.optString("name", null);
                }
            }
        }
        if (author == null || author.isEmpty()) {
            author = "Anonymous";
        }
        r.author = author;

        // ----- STARS -----
        int stars = o.optInt("rating", -1);
        if (stars < 0) {
            stars = o.optInt("stars", 0);
        }
        if (stars > 5) {
            stars = 5;
        }
        if (stars < 0) {
            stars = 0;
        }
        r.stars = stars;

        // ----- CONTENT -----
        String content = o.optString("review", null);
        if (content == null || content.isEmpty() || "null".equalsIgnoreCase(content)) {
            content = o.optString("content", null);
        }
        if (content == null || content.isEmpty() || "null".equalsIgnoreCase(content)) {
            content = o.optString("text", null);
        }
        if (content == null || content.isEmpty() || "null".equalsIgnoreCase(content)) {
            content = o.optString("comment", "");
        }
        r.content = content;

        return r;
    }

    @Override
    public String toString() {
        return "Review{" +
                "id=" + id +
                ", userId=" + userId +
                ", author='" + author + '\'' +
                ", stars=" + stars +
                ", content='" + content + '\'' +
                '}';
    }
}
