package com.example.snackstream.models;

public class StoryItemModel {
    private String username;
    private String userImgURI;
    private String imageUrl;
    public StoryItemModel(String username, String imageUrl) {
        this.username = username;
        this.userImgURI = imageUrl;
    }
    public String getUsername() {
        return username;
    }
    public String getUserImgURI() {
        return userImgURI;
    }
}