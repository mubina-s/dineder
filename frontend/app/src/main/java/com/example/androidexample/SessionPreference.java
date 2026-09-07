package com.example.androidexample;

public class SessionPreference {
    public int id;
    public String preferenceName;
    public String rating;
    public String priceRange;
    public User host; // Composition: nested object

    public SessionPreference(int id, String preferenceName, String rating, String priceRange, User host) {
        this.id = id;
        this.preferenceName = preferenceName;
        this.rating = rating;
        this.priceRange = priceRange;
        this.host = host;
    }

    public SessionPreference(int id, String preferenceName) {
        this.id = id;
        this.preferenceName = preferenceName;
    }


    @Override
    public String toString() {
        return preferenceName;
    }
}


