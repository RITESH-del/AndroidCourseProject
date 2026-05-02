package com.example.snackstream.activities.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.snackstream.R;
import com.example.snackstream.adapters.PostCardAdapter;
import com.example.snackstream.adapters.StoryItemAdapter;
import com.example.snackstream.databinding.FragmentHomeBinding;
import com.example.snackstream.models.PostCardModel;
import com.example.snackstream.models.SearchItemModel;
import com.example.snackstream.models.StoryItemModel;
import com.example.snackstream.repository.PostRepository;
import com.example.snackstream.repository.SearchRepository;
import com.example.snackstream.viewmodels.UserViewModel;
import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends Fragment {
        private FragmentHomeBinding binding;
        private PostCardAdapter adapter = new PostCardAdapter();
        private StoryItemAdapter storyAdapter = new StoryItemAdapter();
        private String profileImageUrl;

        private List<StoryItemModel> stories = new ArrayList<>();
        private final PostRepository postRepository = PostRepository.getInstance();




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        binding.setLifecycleOwner(getViewLifecycleOwner());
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /* manual_top_bar_click_handling_start */
        binding.topBar.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();

            if (id == R.id.addStoryFragment) {
                NavHostFragment.findNavController(this)
                        .navigate(R.id.addStoryFragment);
                return true;
            }

            if (id == R.id.cart) {
                // handle cart click
                NavHostFragment.findNavController(this)
                        .navigate(R.id.cartFragment);
                return true;
            }

            return false;
        });
        /* manual_top_bar_click_handling_end */

        binding.feedRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.feedRecycler.setAdapter(adapter);

        binding.storiesRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.storiesRecycler.setAdapter(storyAdapter);

        /* load_post_start */
        postRepository.getPosts().observe(getViewLifecycleOwner(), posts -> {
            if (posts != null) {
                adapter.setList(posts);
            }
        });
        postRepository.fetchPosts();
        /* load_post_end */

        // fetch user followers data
        SearchRepository searchRepo = SearchRepository.getInstance();
        searchRepo.fetchFollowing();

        /* add_followers_start */
        searchRepo.getFollowingIds().observe(getViewLifecycleOwner(), followingIds -> {

            if (followingIds == null || followingIds.isEmpty()) return;

            searchRepo.getUsersByIds(followingIds, users -> {

                List<StoryItemModel> storyList = new ArrayList<>();
                if (profileImageUrl != null) {
                    storyList.add(new StoryItemModel("Your Story", profileImageUrl));
                }
                for (SearchItemModel user : users) {
                    storyList.add(new StoryItemModel(
                            user.getUsername(),
                            user.getUserProfileImage()
                    ));
                }

                storyAdapter.setList(storyList);
            });
        });
        /* add_followers_end */


        /* load_profile_image_start */
//        UserViewModel viewModel = new ViewModelProvider(this).get(UserViewModel.class);
//
//        viewModel.getUser().observe(getViewLifecycleOwner(), user -> {
//            Log.d("USER_DEBUG", String.valueOf(user));
//            if (user != null) {
//                Log.d("PROFILE_DEBUG", String.valueOf(user.profileImage));
//                if (user.profileImage != null) {
//                    profileImageUrl = user.profileImage;
//                    if (!stories.isEmpty()) {
//                        stories.set(0, new StoryItemModel("Your Story", profileImageUrl));
//                    } else {
//                        stories.add(0, new StoryItemModel("Your Story", profileImageUrl));
//                    }
//                }
//            }
//        });
//        viewModel.fetchUser();
        /* load_profile_image_end */

        storyAdapter.setList(stories);

    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}