package com.example.snackstream.models;

import com.google.firebase.firestore.Exclude;

public class ReelModel {
    public String reelId;
    public String userId;
    public String videoUrl;
    public String caption;
    public String username;
    public String userProfileImage;
    public long likes;
    public long timestamp;

    @Exclude
    public boolean isLiked;

    public ReelModel() {} // needed for Firebase
}