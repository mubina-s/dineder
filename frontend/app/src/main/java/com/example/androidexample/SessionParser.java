package com.example.androidexample;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SessionParser {

    public static List<Restaurant> parseRestaurants(JSONArray restaurantArray) {
        List<Restaurant> restaurants = new ArrayList<>();
        Log.d("SessionParser", "Parsing restaurant array. Length: " + restaurantArray.length());

        for (int i = 0; i < restaurantArray.length(); i++) {
            try {
                JSONObject obj = restaurantArray.getJSONObject(i);
                Long restaurantId = obj.getLong("id");
                String name = obj.optString("name");
                int rating = obj.optInt("rating");
                String priceRange = obj.optString("priceRange");
                String address = obj.optString("address");
                String phone = obj.getString("phone");
                String website = obj.getString("website");
                JSONArray cuisinesArray = obj.getJSONArray("cuisines");
                Set<String> cuisines = new HashSet<>();
                for (int j = 0; j < cuisinesArray.length(); j++) {
                    cuisines.add(cuisinesArray.getString(j));
                }
                String image = obj.getString("imageUrl");

                Restaurant restaurant = new Restaurant(restaurantId, name, rating, priceRange, address, phone, website, cuisines, image);
                restaurants.add(restaurant);

                Log.d("SessionParser", "Parsed restaurant: " + restaurant.toString());
            } catch (JSONException e) {
                Log.e("SessionParser", "Error parsing restaurant at index " + i, e);
            }
        }

        Log.d("SessionParser", "Total restaurants parsed: " + restaurants.size());
        return restaurants;
    }


    public static List<SessionParticipant> parseParticipants(JSONArray participantsArray) {
        List<SessionParticipant> participants = new ArrayList<SessionParticipant>();

        for (int i = 0; i < participantsArray.length(); i++) {
            try {
                JSONObject obj = participantsArray.getJSONObject(i);
                Long participantId = obj.optLong("participantId");
                Long userId = obj.optLong("userId");
                String name = obj.optString("name");
                Boolean ready = obj.optBoolean("ready");
                Boolean finishedSwiping = obj.optBoolean("finishedSwiping");

                SessionParticipant par = new SessionParticipant(participantId, userId, name, ready, finishedSwiping);

                participants.add(par);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return participants;
    }
}
