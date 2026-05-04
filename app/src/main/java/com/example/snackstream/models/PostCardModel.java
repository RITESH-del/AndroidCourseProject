package com.example.snackstream.models;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.PropertyName;

public class PostCardModel {
    public String postId;
    public String userId;
    public String username;
    public String fullname;
    public String userProfileImage;
    public String mediaUrl;
    public String caption;
    public long timestamp;
    
    @Exclude
    public boolean isLiked;

    private int likes;

    public PostCardModel() {} // needed for Firestore

    public PostCardModel(String postId, String userId, String username, String fullname
                ,String userProfileImage, String mediaUrl,
                String caption, long timestamp, int likes) {
        this.postId = postId;
        this.userId = userId;
        this.username = username;
        this.fullname = fullname;
        this.userProfileImage = userProfileImage;
        this.mediaUrl = mediaUrl;
        this.caption = caption;
        this.timestamp = timestamp;
        this.likes = likes;
    }

    @PropertyName("likes")
    public int getLikes() {
        return likes;
    }

    @PropertyName("likes")
    public void setLikes(Object likes) {
        if (likes instanceof Number) {
            this.likes = ((Number) likes).intValue();
        } else if (likes instanceof String) {
            try {
                this.likes = Integer.parseInt((String) likes);
            } catch (NumberFormatException e) {
                this.likes = 0;
            }
        }
    }
}