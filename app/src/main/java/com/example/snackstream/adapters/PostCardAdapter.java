package com.example.snackstream.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snackstream.R;
import com.example.snackstream.databinding.PostCardLayoutBinding;
import com.example.snackstream.models.CartItem;
import com.example.snackstream.models.PostCardModel;
import com.example.snackstream.repository.PostRepository;
import com.example.snackstream.repository.SearchRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PostCardAdapter extends RecyclerView.Adapter<PostCardAdapter.ViewHolder> {

    private List<PostCardModel> postCardList = new ArrayList<>();
    private List<String> followingIds = new ArrayList<>();
    private Set<String> likedIds = new HashSet<>();

    public void setList(List<PostCardModel> postCardList) {
        this.postCardList = postCardList;
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

    static class ViewHolder extends RecyclerView.ViewHolder {
        PostCardLayoutBinding binding;

        public ViewHolder(PostCardLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        PostCardLayoutBinding binding = PostCardLayoutBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PostCardModel item = postCardList.get(position);
        holder.binding.setItem(item);

        String currentUserId = FirebaseAuth.getInstance().getUid();

        // 🔥 Sync Follow Button State
        if (currentUserId != null && currentUserId.equals(item.userId)) {
            holder.binding.btnFollow.setVisibility(View.GONE);
        } else {
            holder.binding.btnFollow.setVisibility(View.VISIBLE);
            boolean isFollowing = followingIds.contains(item.userId);
            holder.binding.btnFollow.setText(isFollowing ? "Following" : "Follow");
            holder.binding.btnFollow.setIconResource(isFollowing ? 0 : R.drawable.ic_add);
            // Ensure button is enabled to allow unfollowing
            holder.binding.btnFollow.setEnabled(true);
            holder.binding.btnFollow.setAlpha(1.0f);
        }

        // 🔥 Sync Like Icon State from centralized set
        holder.binding.likeIcon.setSelected(likedIds.contains(item.postId));

        holder.binding.likeIcon.setOnClickListener(v -> {
            if (currentUserId == null) return;
            // Immediate UI feedback
            boolean wasSelected = v.isSelected();
            v.setSelected(!wasSelected);
            animateLike(v);
            // Persistence
            PostRepository.getInstance().toggleLike("posts", item.postId, currentUserId);
        });

        holder.binding.btnFollow.setOnClickListener(v -> {
            if (item.userId == null || item.userId.isEmpty()) return;
            
            if (followingIds.contains(item.userId)) {
                SearchRepository.getInstance().unfollowUser(item.userId);
                Toast.makeText(v.getContext(), "Unfollowed", Toast.LENGTH_SHORT).show();
            } else {
                SearchRepository.getInstance().followUser(item.userId);
                Toast.makeText(v.getContext(), "Following", Toast.LENGTH_SHORT).show();
            }
        });

        holder.binding.btnAddToCart.setOnClickListener(v -> addToCart(item, v));

        holder.binding.executePendingBindings();
    }

    private void addToCart(PostCardModel item, View v) {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        CartItem cartItem = new CartItem(
                item.postId,
                item.fullname != null ? item.fullname : "Delicious Snack",
                item.caption != null ? item.caption : "Snack description",
                12.99,
                item.mediaUrl,
                1
        );

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .collection("cart")
                .document(item.postId)
                .set(cartItem)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(v.getContext(), "Added to cart!", Toast.LENGTH_SHORT).show();
                    animateAddToCart(v);
                });
    }

    private void animateAddToCart(View view) {
        view.animate().rotation(360).setDuration(400).withEndAction(() -> view.setRotation(0)).start();
    }

    private void animateLike(View view) {
        view.animate().scaleX(1.4f).scaleY(1.4f).setDuration(150).withEndAction(() -> view.animate().scaleX(1f).scaleY(1f).setDuration(150).start()).start();
    }

    @Override
    public int getItemCount() {
        return postCardList.size();
    }
}
