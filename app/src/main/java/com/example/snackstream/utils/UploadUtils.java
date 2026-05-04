package com.example.snackstream.utils;

import android.net.Uri;
import android.util.Log;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;

import java.util.Map;

public class UploadUtils {
    static private String uploadPreset = "default_preset";
    public static void uploadImage(Uri imageUri, String postId, UploadCallbackListener listener) {

        MediaManager.get().upload(imageUri).unsigned(uploadPreset)
                .callback(new UploadCallback() {

                    @Override
                    public void onStart(String requestId) {
                        Log.d("UPLOAD", "Started");
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        // optional: progress update
                        Log.d("Cloudinary Quickstart", "Upload progress");
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        Log.d("CLOUDINARY_SUCCESS", "upload success");
                        String imageUrl = resultData.get("secure_url").toString();
                        listener.onSuccess(imageUrl);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Log.e("CLOUDINARY_ERROR", error.getDescription());
                        listener.onError(error.getDescription());
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {}

                })
                .dispatch();
    }

    public static void uploadProfileImage(Uri imageUri, String userId, UploadCallbackListener listener) {

        MediaManager.get().upload(imageUri)
                .option("public_id", "profile_images/" + userId) // 🔥 unique per user
                .option("overwrite", true)                       // 🔥 replace old image
                .option("invalidate", true)                      // 🔥 refresh CDN cache
                .callback(new UploadCallback() {

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String imageUrl = resultData.get("secure_url").toString();
                        listener.onSuccess(imageUrl);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        listener.onError(error.getDescription());
                    }

                    @Override public void onStart(String requestId) {}
                    @Override public void onProgress(String requestId, long bytes, long totalBytes) {}
                    @Override public void onReschedule(String requestId, ErrorInfo error) {}

                })
                .dispatch();
    }

    public static void uploadReel(Uri videoUri, String reelId, UploadCallbackListener listener) {

        MediaManager.get().upload(videoUri)
                .option("resource_type", "video")
                .option("folder", "reels")             // organize in Cloudinary
                .option("public_id", "reels/" + reelId)
                .option("overwrite", true)

                // 🔥 Optimization (VERY IMPORTANT for reels)
                .option("quality", "auto")
                .option("fetch_format", "auto")

                .callback(new UploadCallback() {

                    @Override
                    public void onStart(String requestId) {
                        Log.d("REEL_UPLOAD", "Upload started");
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        int progress = (int) ((bytes * 100) / totalBytes);
                        Log.d("REEL_UPLOAD", "Progress: " + progress + "%");
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {

                        String videoUrl = resultData.get("secure_url").toString();

                        // 🔥 Thumbnail generation
                        String thumbnailUrl;
                        if (resultData.get("thumbnail_url") != null) {
                            thumbnailUrl = resultData.get("thumbnail_url").toString();
                        } else {
                            // fallback: Cloudinary auto thumbnail
                            thumbnailUrl = videoUrl + ".jpg";
                        }

                        Log.d("REEL_UPLOAD", "Success");

                        // 🔥 You can extend listener if needed
                        listener.onSuccess(videoUrl);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Log.e("REEL_UPLOAD", error.getDescription());
                        listener.onError(error.getDescription());
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        Log.d("REEL_UPLOAD", "Rescheduled");
                    }
                })
                .dispatch();
    }
}