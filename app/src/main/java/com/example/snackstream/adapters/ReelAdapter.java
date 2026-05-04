package com.example.snackstream.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.snackstream.R;
import com.example.snackstream.models.ReelModel;
import com.example.snackstream.repository.PostRepository;
import com.example.snackstream.repository.SearchRepository;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReelAdapter extends RecyclerView.Adapter<ReelAdapter.ReelViewHolder> {

    private List<ReelModel> reels = new ArrayList<>();
    private List<String> followingIds = new ArrayList<>();
    private Set<String> likedIds = new HashSet<>();

    public void setReels(List<ReelModel> list) {
        this.reels = list;
        notifyDataSetChanged();
    }

    public void setFollowingIds(List<String> followingIds) {
        this.followingIds = followingIds;
        notifyDataSetChanged();
    }

    public void setLikedIds(Set<String> likedIds) {
        this.likedIds = likedIds;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.reel_item, parent, false);
        return new ReelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReelViewHolder holder, int position) {
        holder.bind(reels.get(position), followingIds, likedIds);
    }

    @Override
    public int getItemCount() {
        return reels.size();
    }

    @Override
    public void onViewAttachedToWindow(@NonNull ReelViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        if (holder.player != null) {
            holder.player.play();
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ReelViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        if (holder.player != null) {
            holder.player.pause();
        }
    }

    static class ReelViewHolder extends RecyclerView.ViewHolder {

        PlayerView playerView;
        ExoPlayer player;
        TextView username, caption, likeCount;
        MaterialButton btnFollow;
        ImageView profileImage, likeBtn;

        public ReelViewHolder(@NonNull View itemView) {
            super(itemView);
            playerView = itemView.findViewById(R.id.playerView);
            username = itemView.findViewById(R.id.username);
            caption = itemView.findViewById(R.id.caption);
            profileImage = itemView.findViewById(R.id.profileImage);
            likeBtn = itemView.findViewById(R.id.likeBtn);
            likeCount = itemView.findViewById(R.id.likeCount);
            btnFollow = itemView.findViewById(R.id.btnFollow);
        }

        public void bind(ReelModel reel, List<String> followingIds, Set<String> likedIds) {
            if (player != null) {
                player.release();
            }

            player = new ExoPlayer.Builder(itemView.getContext()).build();
            playerView.setPlayer(player);

            if (reel.videoUrl != null) {
                MediaItem mediaItem = MediaItem.fromUri(reel.videoUrl);
                player.setMediaItem(mediaItem);
                player.setRepeatMode(Player.REPEAT_MODE_ONE);
                player.prepare();
                player.setPlayWhenReady(false);
            }

            username.setText(reel.username != null ? reel.username : "User");
            caption.setText(reel.caption != null ? reel.caption : "");
            likeCount.setText(String.valueOf(reel.likes));

            Glide.with(itemView.getContext())
                    .load(reel.userProfileImage)
                    .placeholder(R.drawable.sample_user)
                    .error(R.drawable.sample_user)
                    .into(profileImage);

            String currentUserId = FirebaseAuth.getInstance().getUid();

            // 🔥 Sync Follow Button State
            if (currentUserId != null && currentUserId.equals(reel.userId)) {
                btnFollow.setVisibility(View.GONE);
            } else {
                btnFollow.setVisibility(View.VISIBLE);
                boolean isFollowing = followingIds.contains(reel.userId);
                btnFollow.setText(isFollowing ? "Following" : "Follow");
                btnFollow.setEnabled(true); // 🔥 Always enabled to allow unfollow
                btnFollow.setAlpha(1.0f);
                btnFollow.setIconResource(isFollowing ? 0 : R.drawable.ic_add);
            }

            // 🔥 Sync Like Icon State from centralized set
            likeBtn.setSelected(likedIds.contains(reel.reelId));

            likeBtn.setOnClickListener(v -> {
                if (currentUserId == null) return;

                boolean wasSelected = likeBtn.isSelected();
                likeBtn.setSelected(!wasSelected);
                animateLike(v);

                // Update count locally
                if (!wasSelected) {
                    reel.likes++;
                } else {
                    reel.likes = Math.max(0, reel.likes - 1);
                }
                likeCount.setText(String.valueOf(reel.likes));

                // 🔥 Call repository with "reels" collection
                PostRepository.getInstance().toggleLike("reels", reel.reelId, currentUserId);
            });

            // 🔥 Handle Follow/Unfollow
            btnFollow.setOnClickListener(v -> {
                if (reel.userId == null) return;
                
                if (followingIds.contains(reel.userId)) {
                    SearchRepository.getInstance().unfollowUser(reel.userId);
                    Toast.makeText(v.getContext(), "Unfollowed", Toast.LENGTH_SHORT).show();
                } else {
                    SearchRepository.getInstance().followUser(reel.userId);
                    Toast.makeText(v.getContext(), "Following", Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void animateLike(View view) {
            view.animate()
                    .scaleX(1.3f)
                    .scaleY(1.3f)
                    .setDuration(100)
                    .withEndAction(() -> {
                        view.animate()
                                .scaleX(1.0f)
                                .scaleY(1.0f)
                                .setDuration(100)
                                .start();
                    })
                    .start();
        }
    }
}
