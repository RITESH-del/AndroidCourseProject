package com.example.snackstream.repository;


import android.util.Log;
import androidx.lifecycle.MutableLiveData;

import com.example.snackstream.models.SearchItemModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class SearchRepository {

    private static SearchRepository instance;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    private final MutableLiveData<List<SearchItemModel>> usersLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<String>> followingIdsLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<String>> followersIdsLiveData = new MutableLiveData<>(new ArrayList<>());
    
    private ListenerRegistration followingListener;
    private ListenerRegistration followersListener;

    public MutableLiveData<List<String>> getFollowersIds() {
        return followersIdsLiveData;
    }

    public static SearchRepository getInstance() {
        if (instance == null) instance = new SearchRepository();
        return instance;
    }

    public MutableLiveData<List<SearchItemModel>> getUsers() {
        return usersLiveData;
    }

    public MutableLiveData<List<String>> getFollowingIds() {
        return followingIdsLiveData;
    }

    // SEARCH USERS
    public void searchUsers(String query) {

        db.collection("users")
                .orderBy("username")
                .startAt(query.toLowerCase())
                .endAt(query.toLowerCase() + "\uf8ff")
                .limit(20)
                .get()
                .addOnSuccessListener(snapshot -> {

                    List<SearchItemModel> list = new ArrayList<>();

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        SearchItemModel user = doc.toObject(SearchItemModel.class);
                        if (user != null) {
                            user.setUserId(doc.getId());
                            list.add(user);
                        }
                    }

                    usersLiveData.setValue(list);
                });
    }

    //  FETCH FOLLOWING LIST (for UI state)
    public void fetchFollowing() {
        if (auth.getCurrentUser() == null) return;
        if (followingListener != null) return; // 🔥 Avoid duplicate listeners

        String currentUserId = auth.getCurrentUser().getUid();

        followingListener = db.collection("following")
                .document(currentUserId)
                .collection("userFollowing")
                .addSnapshotListener((snapshot, error) -> {
                    if (snapshot == null) return;

                    List<String> ids = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        ids.add(doc.getId());
                    }
                    followingIdsLiveData.setValue(ids);
                });
    }

    public void fetchFollowers() {
        if (auth.getCurrentUser() == null) return;
        if (followersListener != null) return; // 🔥 Avoid duplicate listeners

        String currentUserId = auth.getCurrentUser().getUid();

        followersListener = db.collection("followers")
                .document(currentUserId)
                .collection("userFollowers")
                .addSnapshotListener((snapshot, error) -> {
                    if (snapshot == null) return;

                    List<String> ids = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        ids.add(doc.getId());
                    }
                    followersIdsLiveData.setValue(ids);
                });
    }

    // FOLLOW USER
    public void followUser(String targetUserId) {
        if (auth.getCurrentUser() == null || targetUserId == null || targetUserId.isEmpty()) {
            return;
        }
        
        String currentUserId = auth.getCurrentUser().getUid();
        if (currentUserId.equals(targetUserId)) return;

        WriteBatch batch = db.batch();

        DocumentReference followingRef = db.collection("following")
                .document(currentUserId)
                .collection("userFollowing")
                .document(targetUserId);

        DocumentReference followersRef = db.collection("followers")
                .document(targetUserId)
                .collection("userFollowers")
                .document(currentUserId);

        DocumentReference targetUserRef = db.collection("users").document(targetUserId);

        Map<String, Object> data = new HashMap<>();
        data.put("timestamp", FieldValue.serverTimestamp());

        batch.set(followingRef, data);
        batch.set(followersRef, data);
        batch.update(targetUserRef, "followers", FieldValue.increment(1));

        batch.commit()
                .addOnSuccessListener(aVoid -> Log.d("SearchRepo", "Followed " + targetUserId))
                .addOnFailureListener(e -> Log.e("SearchRepo", "Follow failed", e));
    }

    //  UNFOLLOW USER
    public void unfollowUser(String targetUserId) {
        if (auth.getCurrentUser() == null || targetUserId == null) return;
        String currentUserId = auth.getCurrentUser().getUid();

        WriteBatch batch = db.batch();

        DocumentReference followingRef = db.collection("following")
                .document(currentUserId)
                .collection("userFollowing")
                .document(targetUserId);

        DocumentReference followersRef = db.collection("followers")
                .document(targetUserId)
                .collection("userFollowers")
                .document(currentUserId);
                
        DocumentReference targetUserRef = db.collection("users").document(targetUserId);

        batch.delete(followingRef);
        batch.delete(followersRef);
        batch.update(targetUserRef, "followers", FieldValue.increment(-1));

        batch.commit()
                .addOnSuccessListener(aVoid -> Log.d("SearchRepo", "Unfollowed " + targetUserId))
                .addOnFailureListener(e -> Log.e("SearchRepo", "Unfollow failed", e));
    }

    public void getUsersByIds(List<String> ids, OnUsersFetchedListener listener) {
        if (ids == null || ids.isEmpty()) {
            listener.onFetched(new ArrayList<>());
            return;
        }

        List<SearchItemModel> users = new ArrayList<>();
        AtomicInteger count = new AtomicInteger(0);

        for (String id : ids) {
            db.collection("users").document(id).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            DocumentSnapshot doc = task.getResult();
                            if (doc.exists()) {
                                SearchItemModel user = doc.toObject(SearchItemModel.class);
                                if (user != null) {
                                    user.setUserId(doc.getId());
                                    synchronized (users) {
                                        users.add(user);
                                    }
                                }
                            }
                        }
                        
                        if (count.incrementAndGet() == ids.size()) {
                            listener.onFetched(users);
                        }
                    });
        }
    }

    public interface OnUsersFetchedListener {
        void onFetched(List<SearchItemModel> users);
    }
}
