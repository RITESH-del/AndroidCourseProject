package com.example.snackstream.models;

public class FollowRow {

    public static final int TYPE_HEADER = 0;
    public static final int TYPE_USER = 1;

    public int type;
    public String title;
    public SearchItemModel user;

    // Header
    public FollowRow(String title) {
        this.type = TYPE_HEADER;
        this.title = title;
    }

    // User
    public FollowRow(SearchItemModel user) {
        this.type = TYPE_USER;
        this.user = user;
    }
}
