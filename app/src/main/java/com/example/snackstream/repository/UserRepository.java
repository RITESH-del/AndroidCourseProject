package com.example.snackstream.repository;

import android.net.Uri;

import androidx.lifecycle.MutableLiveData;

import com.example.snackstream.models.User;
import com.example.snackstream.utils.UploadCallbackListener;
import com.example.snackstream.utils.UploadUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class UserRepository {

    private static UserRepository instance;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    private final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

    private final MutableLiveData<User> userLiveData = new MutableLiveData<>();

    public static UserRepository getInstance() {
        if (instance == null) instance = new UserRepository();
        return instance;
    }

    public MutableLiveData<User> getUser() {
        return userLiveData;
    }

    public void syncUserFromAuth() {

        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser == null) return;

        String userId = firebaseUser.getUid();

        String profileImage = firebaseUser.getPhotoUrl() != null
                ? firebaseUser.getPhotoUrl().toString()
                : "";

        String fullname = firebaseUser.getDisplayName();
        String email = firebaseUser.getEmail();

        // 🔥 safer username generation
        String username = generateUsername(
                fullname != null ? fullname : email
        );

        User user = new User(
                profileImage,
                username,
                email,
                fullname
        );

        // 🔥 Save / merge into Firestore
        db.collection("users")
                .document(userId)
                .set(user, SetOptions.merge())
                .addOnSuccessListener(unused -> {
                    userLiveData.setValue(user); // update UI
                });
    }

    // 🔥 Fetch user
    public void fetchUser() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();

        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(document -> {

                    if (document.exists()) {
                        User user = document.toObject(User.class);
                        userLiveData.setValue(user);
                    } else {
                        User newUser = new User(
                                currentUser.getPhotoUrl() != null ? currentUser.getPhotoUrl().toString() : "",
                                generateUsername(currentUser.getDisplayName()),
                                currentUser.getEmail(),
                                currentUser.getDisplayName()
                        );
                        db.collection("users")
                                .document(userId)
                                .set(newUser)
                                .addOnSuccessListener(unused -> userLiveData.setValue(newUser));
                    }
                });
    }

    private String generateUsername(String name) { // e.g. riteshkumar_4821
        String base = (name != null)
                ? name.replaceAll("\\s+", "").toLowerCase()
                : "user";

        return base + "_" + (int)(Math.random() * 10000);
    }

    // 🔥 Upload profile image
    public void uploadProfileImage(Uri uri) {

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();

        UploadUtils.uploadProfileImage(uri, userId, new UploadCallbackListener() {

            @Override
            public void onSuccess(String imageUrl) {

                Map<String, Object> data = new HashMap<>();
                data.put("profileImage", imageUrl);

                db.collection("users")
                        .document(userId)
                        .set(data, SetOptions.merge())
                        .addOnSuccessListener(unused -> {
                            // update LiveData instantly
                            User current = userLiveData.getValue();
                            if (current == null) current = new User();
                            current.profileImage = imageUrl;

                            userLiveData.setValue(current);
                        });
            }
            @Override
            public void onError(String error) {
                // you can expose error LiveData later
            }
        });
    }
}