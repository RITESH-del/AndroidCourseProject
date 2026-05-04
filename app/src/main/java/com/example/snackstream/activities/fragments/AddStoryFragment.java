package com.example.snackstream.activities.fragments;

import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.snackstream.R;
import com.example.snackstream.databinding.FragmentAddStoryBinding;
import com.example.snackstream.models.User;
import com.example.snackstream.repository.PostRepository;
import com.example.snackstream.repository.UserRepository;


public class AddStoryFragment extends Fragment {

    FragmentAddStoryBinding binding;
    private Uri selectedImageUri;
    public String caption;

    private final PostRepository postRepository = PostRepository.getInstance();
    private final UserRepository userRepository = UserRepository.getInstance();

    // Single image picker
    private final ActivityResultLauncher<PickVisualMediaRequest> pickImageLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.PickVisualMedia(),
                    uri -> {
                        if (uri != null) {
                            selectedImageUri = uri;
                            // Show preview
                            binding.storyImage.setImageURI(uri);

                            // TODO: upload this URI to Firebase
                        }
                    });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAddStoryBinding.inflate(inflater, container, false);
        binding.setLifecycleOwner(getViewLifecycleOwner());
        binding.setItem(this);
        requireActivity().findViewById(R.id.nav_view).setVisibility(View.GONE); // hide navbar
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewCompat.setOnApplyWindowInsetsListener(binding.addCaption, (v, insets) -> {
            Insets imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(0, 0, 0, imeInsets.bottom);
            return insets;
        });

        userRepository.fetchUser();   //  REQUIRED

        binding.topBar.setNavigationOnClickListener(v -> {
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        });

        /* open_gallery_click_icon_start */
        binding.captionIcon.setOnClickListener(v -> {openGallery();});
        /* open_gallery_click_icon_end */
    }

    private void openGallery() {
        pickImageLauncher.launch(
                new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build()
        );
    }

    public void uploadPost() {
        if (selectedImageUri == null) return;

        userRepository.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user == null) return;

            postRepository.uploadPost(selectedImageUri, caption, user);
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        });

        userRepository.fetchUser(); // ensure it triggers
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        requireActivity().findViewById(R.id.nav_view).setVisibility(View.VISIBLE);
    }



}