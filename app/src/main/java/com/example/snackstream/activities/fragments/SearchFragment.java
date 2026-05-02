package com.example.snackstream.activities.fragments;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.snackstream.R;
import com.example.snackstream.adapters.SearchDefaultAdapter;
import com.example.snackstream.adapters.SearchItemAdapter;
import com.example.snackstream.databinding.FragmentSearchBinding;
import com.example.snackstream.models.SearchItemModel;
import com.example.snackstream.repository.SearchRepository;
import com.google.android.material.search.SearchView;

import java.util.List;


public class SearchFragment extends Fragment {


    private FragmentSearchBinding binding;
    private SearchItemAdapter searchAdapter;
    private SearchRepository repo;
    private SearchDefaultAdapter defaultAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(getLayoutInflater());
        binding.setLifecycleOwner(getViewLifecycleOwner());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        repo = SearchRepository.getInstance();
        searchAdapter = new SearchItemAdapter();
        defaultAdapter = new SearchDefaultAdapter();

        // Connect SearchBar ↔ SearchView
        binding.searchView.setupWithSearchBar(binding.searchBar);


    /* default_list_item_adapter_start */
        binding.recyclerView1.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView1.setAdapter(defaultAdapter);

        repo.fetchFollowing();
        repo.fetchFollowers(); // you must implement this in repo

        repo.getFollowingIds().observe(getViewLifecycleOwner(), followingIds -> {
            updateDefaultList();
        });

        repo.getFollowersIds().observe(getViewLifecycleOwner(), followersIds -> {
            updateDefaultList();
        });

        //  Follow / Unfollow click handling
        defaultAdapter.setOnFollowClickListener((item, position) -> {

            if (item.isFollowing()) {
                repo.unfollowUser(item.userId);
                item.setFollowing(false);
            } else {
                repo.followUser(item.userId);
                item.setFollowing(true);
            }

            defaultAdapter.notifyItemChanged(position);
        });
    /* default_list_item_adapter_end */



        // Search results
        binding.recyclerView2.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView2.setAdapter(searchAdapter);

//        binding.recyclerView1.setBackgroundColor(Color.RED);

        // sync following state with search list
        repo.getUsers().observe(getViewLifecycleOwner(), users -> {
            List<String> followingIds = repo.getFollowingIds().getValue();

            if (followingIds != null) {
                for (SearchItemModel user : users) {
                    user.setFollowing(followingIds.contains(user.userId));
                }
            }

            searchAdapter.setList(users);
        });

        // Observe following changes (real-time UI update)
        repo.getFollowingIds().observe(getViewLifecycleOwner(), followingIds -> {
            List<SearchItemModel> users = repo.getUsers().getValue();
            if (users == null) return;

            for (SearchItemModel user : users) {
                user.setFollowing(followingIds.contains(user.userId));
            }
            searchAdapter.setList(users);
        });

        //  Follow / Unfollow click handling
        searchAdapter.setOnFollowClickListener((item, position) -> {

            if (item.isFollowing()) {
                repo.unfollowUser(item.userId);
                item.setFollowing(false);
            } else {
                repo.followUser(item.userId);
                item.setFollowing(true);
            }

            searchAdapter.notifyItemChanged(position);
        });

        //  Search input listener
        binding.searchView.getEditText().addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                repo.searchUsers(s.toString().trim());
            }
        });





        // show and hide bottom navbar
        binding.searchView.addTransitionListener(
                (searchView, previousState, newState) -> {
                    if (newState == SearchView.TransitionState.SHOWING) {
                        requireActivity().findViewById(R.id.nav_view).setVisibility(View.GONE); // hide navbar
                    } else if (newState == SearchView.TransitionState.HIDDEN) {
                        requireActivity().findViewById(R.id.nav_view).setVisibility(View.VISIBLE);
                    }
                }
        );
    }

    private void updateDefaultList() {

        List<String> followingIds = repo.getFollowingIds().getValue();
        List<String> followersIds = repo.getFollowersIds().getValue();
        Log.d("DEBUG", "updateDefaultList called");

        if (followingIds == null || followersIds == null) return;

        // Convert IDs → users (you must fetch user details)
        repo.getUsersByIds(followersIds, followers -> {

            repo.getUsersByIds(followingIds, following -> {

                for (SearchItemModel user : followers) {
                    user.setFollowing(followingIds.contains(user.userId));
                }

                for (SearchItemModel user : following) {
                    user.setFollowing(true);
                }

                defaultAdapter.setData(followers, following);
            });
        });
    }

    public void onDestroyView() {
        super.onDestroyView();
        requireActivity().findViewById(R.id.nav_view).setVisibility(View.VISIBLE);
    }
}

