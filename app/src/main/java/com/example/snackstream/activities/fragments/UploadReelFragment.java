package com.example.snackstream.activities.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.navigation.Navigation;

import com.example.snackstream.R;
import com.example.snackstream.databinding.FragmentUploadReelBinding;
import com.example.snackstream.models.ReelModel;
import com.example.snackstream.models.User;
import com.example.snackstream.repository.UserRepository;
import com.example.snackstream.utils.UploadCallbackListener;
import com.example.snackstream.utils.UploadUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class UploadReelFragment extends Fragment {

    private FragmentUploadReelBinding binding;
    private Uri videoUri;
    private User currentUser;
    private ExoPlayer player;

    public UploadReelFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String uriString = getArguments().getString("videoUri");
            if (uriString != null) {
                videoUri = Uri.parse(uriString);
            }
        }
        requireActivity().findViewById(R.id.nav_view).setVisibility(View.GONE); // hide navbar
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentUploadReelBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (videoUri == null) {
            Toast.makeText(getContext(), "Error: Video not found", Toast.LENGTH_SHORT).show();
            if (getActivity() != null) getActivity().onBackPressed();
            return;
        }

        UserRepository.getInstance().getUser().observe(getViewLifecycleOwner(), user -> {
            this.currentUser = user;
        });
        UserRepository.getInstance().fetchUser();

        initViews();
        initPlayer();
    }

    private void initPlayer() {
        player = new ExoPlayer.Builder(requireContext()).build();
        binding.playerView.setPlayer(player);

        MediaItem mediaItem = MediaItem.fromUri(videoUri);
        player.setMediaItem(mediaItem);
        player.setRepeatMode(Player.REPEAT_MODE_ALL);
        player.prepare();
        player.play();

        binding.videoPreviewContainer.setOnClickListener(v -> {
            if (player.isPlaying()) {
                player.pause();
                binding.playPauseIcon.setVisibility(View.VISIBLE);
                binding.playPauseIcon.setImageResource(R.drawable.play_ic);
            } else {
                player.play();
                binding.playPauseIcon.setVisibility(View.GONE);
            }
        });

        player.addListener(new Player.Listener() {
            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                if (isPlaying) {
                    binding.playPauseIcon.setVisibility(View.GONE);
                } else {
                    binding.playPauseIcon.setVisibility(View.VISIBLE);
                    binding.playPauseIcon.setImageResource(R.drawable.play_ic);
                }
            }
        });
    }

    private void initViews() {
        binding.backBtn.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        binding.shareBtn.setOnClickListener(v -> {
            uploadReel();
        });

        binding.saveDraftBtn.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Saved to drafts", Toast.LENGTH_SHORT).show();
        });
    }

    private void uploadReel() {
        if (currentUser == null) {
            Toast.makeText(getContext(), "User data not loaded yet, please wait...", Toast.LENGTH_SHORT).show();
            UserRepository.getInstance().fetchUser();
            return;
        }

        String caption = binding.captionEt.getText().toString().trim();
        
        binding.shareBtn.setEnabled(false);
        binding.shareBtn.setText("Uploading...");
        Toast.makeText(getContext(), "Uploading Reel...", Toast.LENGTH_SHORT).show();

        UploadUtils.uploadReel(
                videoUri,
                "reel_" + System.currentTimeMillis(),
                new UploadCallbackListener() {
                    @Override
                    public void onSuccess(String videoUrl) {
                        saveReelToDatabase(videoUrl, caption);
                    }

                    @Override
                    public void onError(String error) {
                        Log.e("UPLOAD", error);
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "Upload failed: " + error, Toast.LENGTH_SHORT).show();
                                binding.shareBtn.setEnabled(true);
                                binding.shareBtn.setText("Share");
                            });
                        }
                    }
                }
        );
    }

    private void saveReelToDatabase(String videoUrl, String caption) {
        String reelId = FirebaseFirestore.getInstance()
                .collection("reels")
                .document()
                .getId();

        ReelModel reel = new ReelModel();
        reel.reelId = reelId;
        reel.videoUrl = videoUrl;
        reel.caption = caption;
        reel.timestamp = System.currentTimeMillis();
        reel.userId = FirebaseAuth.getInstance().getUid();
        
        // Use details from currentUser for the ReelModel
        reel.username = currentUser.username;
        reel.userProfileImage = currentUser.profileImage;
        reel.likes = 0;

        FirebaseFirestore.getInstance()
                .collection("reels")
                .document(reelId)
                .set(reel)
                .addOnSuccessListener(aVoid -> {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Reel uploaded successfully!", Toast.LENGTH_SHORT).show();
                        Navigation.findNavController(requireView()).popBackStack();
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Failed to save reel info", Toast.LENGTH_SHORT).show();
                        binding.shareBtn.setEnabled(true);
                        binding.shareBtn.setText("Share");
                    }
                });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (player != null) {
            player.pause();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (player != null) {
            player.release();
            player = null;
        }
        binding = null;
        requireActivity().findViewById(R.id.nav_view).setVisibility(View.VISIBLE);
    }
}