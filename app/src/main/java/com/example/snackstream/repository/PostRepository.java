package com.example.snackstream.repository;

import android.net.Uri;
import android.util.Log;

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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PostRepository {

    private static PostRepository instance;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    private final MutableLiveData<List<PostCardModel>> postsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Set<String>> likedIdsLiveData = new MutableLiveData<>(new HashSet<>());

    public static PostRepository getInstance() {
        if (instance == null) instance = new PostRepository();
        return instance;
    }

    public MutableLiveData<List<PostCardModel>> getPosts() {
        return postsLiveData;
    }

    public MutableLiveData<Set<String>> getLikedIds() {
        return likedIdsLiveData;
    }

    public void uploadPost(Uri mediaUri, String caption, User user) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        String postId = db.collection("posts").document().getId();

        UploadUtils.uploadImage(mediaUri, postId, new UploadCallbackListener() {
            @Override
            public void onSuccess(String mediaUrl) {
                PostCardModel post = new PostCardModel(
                        postId, userId, user.username, user.fullname,
                        user.profileImage, mediaUrl, caption,
                        System.currentTimeMillis(), 0
                );
                db.collection("posts").document(postId).set(post);
            }
            @Override
            public void onError(String error) {}
        });
    }

    public void fetchPosts() {
        db.collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("PostRepo", "Fetch posts failed", error);
                        return;
                    }
                    if (value == null) return;
                    
                    List<PostCardModel> list = new ArrayList<>();
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        PostCardModel post = doc.toObject(PostCardModel.class);
                        if (post != null) {
                            if (post.postId == null) post.postId = doc.getId();
                            // 🔥 Ensure userId is retrieved from any possible field name
                            if (post.userId == null) {
                                post.userId = doc.getString("userId");
                                if (post.userId == null) post.userId = doc.getString("uid");
                                if (post.userId == null) post.userId = doc.getString("userId");
                            }
                            list.add(post);
                        }
                    }
                    postsLiveData.setValue(list);
                });
    }

    public void listenToUserLikes() {
        String userId = auth.getUid();
        if (userId == null) return;

        db.collection("users").document(userId).collection("liked_items")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("PostRepo", "Listen to likes failed", error);
                        return;
                    }
                    if (value == null) return;
                    
                    Set<String> ids = new HashSet<>();
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        ids.add(doc.getId());
                    }
                    Log.d("PostRepo", "Updated liked items count: " + ids.size());
                    likedIdsLiveData.setValue(ids);
                });
    }

    public void toggleLike(String collection, String docId, String userId) {
        if (docId == null || userId == null) {
            Log.e("PostRepo", "toggleLike: docId or userId is null");
            return;
        }

        DocumentReference docRef = db.collection(collection).document(docId);
        DocumentReference likeRef = docRef.collection("likes").document(userId);
        DocumentReference userLikeRef = db.collection("users").document(userId)
                .collection("liked_items").document(docId);

        likeRef.get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                likeRef.delete();
                userLikeRef.delete();
                docRef.update("likes", FieldValue.increment(-1));
            } else {
                Map<String, Object> data = new HashMap<>();
                data.put("timestamp", FieldValue.serverTimestamp());
                likeRef.set(data);
                userLikeRef.set(data);
                docRef.update("likes", FieldValue.increment(1));
            }
        }).addOnFailureListener(e -> Log.e("PostRepo", "toggleLike failed", e));
    }
}
