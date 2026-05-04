package com.example.snackstream.activities.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snackstream.R;
import com.example.snackstream.adapters.ReelAdapter;
import com.example.snackstream.databinding.FragmentReelBinding;
import com.example.snackstream.models.ReelModel;
import com.example.snackstream.repository.PostRepository;
import com.example.snackstream.repository.SearchRepository;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ReelFragment extends Fragment {

    private FragmentReelBinding binding;
    private ReelAdapter adapter;
    private final List<ReelModel> reelList = new ArrayList<>();
    private ListenerRegistration reelsListener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentReelBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        setupClickListeners();
        setupDataSync();
        fetchReels();
    }

    private void setupRecyclerView() {
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        binding.reelsRecycler.setLayoutManager(layoutManager);

        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(binding.reelsRecycler);

        adapter = new ReelAdapter();
        binding.reelsRecycler.setAdapter(adapter);
    }

    private void setupDataSync() {
        // 🔥 Sync Following State
        SearchRepository searchRepo = SearchRepository.getInstance();
        searchRepo.fetchFollowing();
        searchRepo.getFollowingIds().observe(getViewLifecycleOwner(), followingIds -> {
            if (followingIds != null) {
                adapter.setFollowingIds(followingIds);
            }
        });

        // 🔥 Sync Like State
        PostRepository postRepo = PostRepository.getInstance();
        postRepo.listenToUserLikes();
        postRepo.getLikedIds().observe(getViewLifecycleOwner(), likedIds -> {
            if (likedIds != null) {
                adapter.setLikedIds(likedIds);
            }
        });
    }

    private void setupClickListeners() {
        binding.cartBtn.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.cartFragment);
        });
    }

    private void fetchReels() {
        reelsListener = FirebaseFirestore.getInstance()
                .collection("reels")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("ReelFragment", "Error fetching reels", error);
                        return;
                    }

                    if (value == null) return;

                    reelList.clear();
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        ReelModel reel = doc.toObject(ReelModel.class);
                        if (reel != null) {
                            if (reel.userId == null) {
                                reel.userId = doc.getString("userId");
                                if (reel.userId == null) reel.userId = doc.getString("uid");
                            }
                            if (reel.reelId == null) reel.reelId = doc.getId();
                            
                            reelList.add(reel);
                        }
                    }
                    adapter.setReels(reelList);
                });
    }

    @Override
    public void onDestroyView() {
        if (reelsListener != null) {
            reelsListener.remove();
        }
        super.onDestroyView();
        binding = null;
    }
}
