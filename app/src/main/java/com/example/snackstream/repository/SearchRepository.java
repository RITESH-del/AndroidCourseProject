package com.example.snackstream.repository;


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
    private final MutableLiveData<List<String>> followingIdsLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<String>> followersIdsLiveData = new MutableLiveData<>();

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

        String currentUserId = auth.getCurrentUser().getUid();

        db.collection("following")
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

        String currentUserId = auth.getCurrentUser().getUid();

        db.collection("followers")
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

        String currentUserId = auth.getCurrentUser().getUid();

        // Check
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

        Map<String, Object> data = new HashMap<>();
        data.put("timestamp", FieldValue.serverTimestamp());

        batch.set(followingRef, data);
        batch.set(followersRef, data);

        batch.commit();
    }

    //  UNFOLLOW USER
    public void unfollowUser(String targetUserId) {
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

        batch.delete(followingRef);
        batch.delete(followersRef);

        batch.commit();
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
