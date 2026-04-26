package com.example.snackstream.models;

public class CircularImageModel {
    private String imageDescription;
    private String imageURI;

    public CircularImageModel(String imageDescription, String imageURI) {
        this.imageDescription = imageDescription;
        this.imageURI = imageURI;
    }

    public String getImageDescription() {
        return imageDescription;
    }

    public String getImageURI() {
        return imageURI;
    }
}
