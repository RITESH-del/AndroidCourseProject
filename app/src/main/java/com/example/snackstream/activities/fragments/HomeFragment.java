package com.example.snackstream.activities.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.snackstream.R;
import com.example.snackstream.adapters.PostCardAdapter;
import com.example.snackstream.adapters.StoryItemAdapter;
import com.example.snackstream.databinding.FragmentHomeBinding;
import com.example.snackstream.models.SearchItemModel;
import com.example.snackstream.models.StoryItemModel;
import com.example.snackstream.repository.PostRepository;
import com.example.snackstream.repository.SearchRepository;
import com.example.snackstream.viewmodels.UserViewModel;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;
    private final PostCardAdapter adapter = new PostCardAdapter();
    private final StoryItemAdapter storyAdapter = new StoryItemAdapter();
    private String profileImageUrl;

    private final PostRepository postRepository = PostRepository.getInstance();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        requireActivity().findViewById(R.id.nav_view).setVisibility(View.VISIBLE); // hide navbar
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        binding.setLifecycleOwner(getViewLifecycleOwner());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerViews();
        setupClickListeners();
        setupDataObservers();
        
        // Initial data fetch
        postRepository.fetchPosts();
        postRepository.listenToUserLikes(); // Start listening to likes
        SearchRepository.getInstance().fetchFollowing();
    }

    private void setupRecyclerViews() {
        binding.feedRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.feedRecycler.setAdapter(adapter);

        binding.storiesRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.storiesRecycler.setAdapter(storyAdapter);
    }

    private void setupClickListeners() {
        binding.topBar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.addStoryFragment) {
                NavHostFragment.findNavController(this).navigate(R.id.addStoryFragment);
                return true;
            }
            if (id == R.id.cart) {
                NavHostFragment.findNavController(this).navigate(R.id.cartFragment);
                return true;
            }
            return false;
        });
    }

    private void setupDataObservers() {
        // Observe Posts
        postRepository.getPosts().observe(getViewLifecycleOwner(), posts -> {
            if (posts != null) {
                adapter.setList(posts);
            }
        });

        // Observe Likes for real-time sync
        postRepository.getLikedIds().observe(getViewLifecycleOwner(), likedIds -> {
            if (likedIds != null) {
                adapter.setLikedIds(likedIds);
            }
        });

        // Observe Following List
        SearchRepository searchRepo = SearchRepository.getInstance();
        searchRepo.getFollowingIds().observe(getViewLifecycleOwner(), followingIds -> {
            if (followingIds == null) return;
            
            // Sync Follow buttons
            adapter.setFollowingIds(followingIds);

            // Update stories
            searchRepo.getUsersByIds(followingIds, users -> {
                List<StoryItemModel> storyList = new ArrayList<>();
                storyList.add(new StoryItemModel("Your Story", profileImageUrl));
                for (SearchItemModel user : users) {
                    storyList.add(new StoryItemModel(
                            user.getUsername(),
                            user.getUserProfileImage()
                    ));
                }
                storyAdapter.setList(storyList);
            });
        });

        // Observe Current User
        UserViewModel userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        userViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null && user.profileImage != null) {
                profileImageUrl = user.profileImage;
                // Refresh story list with new image
                List<String> following = searchRepo.getFollowingIds().getValue();
                if (following == null) following = new ArrayList<>();
                // Trigger a refresh if needed
            }
        });
        userViewModel.fetchUser();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
