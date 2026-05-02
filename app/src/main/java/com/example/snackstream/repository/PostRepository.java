package com.example.snackstream.repository;

import android.net.Uri;

import androidx.lifecycle.MutableLiveData;

import com.example.snackstream.models.PostCardModel;
import com.example.snackstream.models.User;
import com.example.snackstream.utils.UploadCallbackListener;
import com.example.snackstream.utils.UploadUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PostRepository {

    private static PostRepository instance;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    private final MutableLiveData<List<PostCardModel>> postsLiveData = new MutableLiveData<>();

    public static PostRepository getInstance() {
        if (instance == null) instance = new PostRepository();
        return instance;
    }

    public MutableLiveData<List<PostCardModel>> getPosts() {
        return postsLiveData;
    }
    // 🔥 Upload Post
    public void uploadPost(Uri mediaUri, String caption, User user) {

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        String postId = db.collection("posts").document().getId();

        // Step 1: Upload media (reuse your UploadUtils)
        UploadUtils.uploadImage(mediaUri, postId, new UploadCallbackListener() {

            @Override
            public void onSuccess(String mediaUrl) {

                PostCardModel post = new PostCardModel(
                        postId,
                        userId,
                        user.username,
                        user.fullname,
                        user.profileImage,
                        mediaUrl,
                        caption,
                        System.currentTimeMillis(),
                        0
                );

                // Step 2: Save to Firestore
                db.collection("posts")
                        .document(postId)
                        .set(post)
                        .addOnSuccessListener(unused -> {
                            // optionally refresh feed
                            fetchPosts();
                        });
            }

            @Override
            public void onError(String error) {
                // handle error later
            }
        });
    }

    // 🔥 Fetch Posts (Feed)
    public void fetchPosts() {
        db.collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {

                    if (value == null) return;

                    List<PostCardModel> list = new ArrayList<>();

                    for (DocumentSnapshot doc : value.getDocuments()) {
                        PostCardModel post = doc.toObject(PostCardModel.class);
                        list.add(post);
                    }

                    postsLiveData.setValue(list);
                });
    }

    public void toggleLike(String postId, String userId) {

        DocumentReference likeRef = db.collection("posts")
                .document(postId)
                .collection("likes")
                .document(userId);

        likeRef.get().addOnSuccessListener(doc -> {

            if (doc.exists()) {
                // 🔻 Unlike
                likeRef.delete();

                db.collection("posts")
                        .document(postId)
                        .update("likes", FieldValue.increment(-1));

            } else {
                // 🔺 Like
                likeRef.set(new HashMap<>());

                db.collection("posts")
                        .document(postId)
                        .update("likes", FieldValue.increment(1));
            }
        });
    }
}
