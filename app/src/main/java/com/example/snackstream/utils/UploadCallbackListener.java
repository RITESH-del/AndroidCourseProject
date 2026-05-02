package com.example.snackstream.utils;

public interface UploadCallbackListener {
    void onSuccess(String imageUrl);

    void onError(String error);
}
