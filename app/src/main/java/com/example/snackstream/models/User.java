package com.example.snackstream.models;

public class User {
    public String profileImage;
    public String username;
    public String email;
    public String fullname;

    // 🔥 REQUIRED for Firestore
    public User() {}

    public User(String profileImage, String username, String email, String fullName) {
        this.profileImage = profileImage;
        this.username = username;
        this.email = email;
        this.fullname = fullName;
    }
}