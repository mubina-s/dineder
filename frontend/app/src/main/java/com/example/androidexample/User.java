package com.example.androidexample;

public class User {
    public int id;
    public String username;
    public String password;
    public String email;
    public String name;
    public Boolean isReady;

    public User(int id, String username, String password, String email, String name, Boolean isReady) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.name = name;
        this.isReady = isReady;
    }

    public User(String name) {
        this.name = name;
        this.isReady = false;
    }

    @Override
    public String toString() {
        return name; // Optional: useful if you ever bind User objects to a Spinner
    }

    public String getName() {
        return name;
    }

    public int getUserId() {
        return this.id;
    }

    public String sessionPar() {
        if(isReady) {
            return name + " READY";
        }
        else {
            return name + " NOT READY";
        }
    }
}

