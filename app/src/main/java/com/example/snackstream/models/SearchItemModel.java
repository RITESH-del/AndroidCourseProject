package com.example.snackstream.models;

public class SearchItemModel {

    public String userId;
    public String fullname;
    public String username;
    public String profileImage;
    public boolean isFollowing;

    // REQUIRED for Firestore
    public SearchItemModel() {}

    // Optional (useful when creating manually)
    public SearchItemModel(String userId, String fullname, String username, String userProfileImage) {
        this.userId = userId;
        this.fullname = fullname;
        this.username = username;
        this.profileImage = profileImage;
    }


    public String getFullname() {
        return fullname;
    }

    public String getUsername() {
        return username;
    }

    public String getUserProfileImage() {
        return profileImage;
    }

    public boolean isFollowing() {
        return isFollowing;
    }

    // 🔥 ADD THIS
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public void setFollowing(boolean following) {
        isFollowing = following;
    }
}