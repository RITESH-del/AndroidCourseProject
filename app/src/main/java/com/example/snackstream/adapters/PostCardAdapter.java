package com.example.snackstream.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.snackstream.databinding.PostCardLayoutBinding;
import com.example.snackstream.models.PostCardModel;
import com.example.snackstream.repository.PostRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class PostCardAdapter extends RecyclerView.Adapter<PostCardAdapter.ViewHolder> {

    private List<PostCardModel> postCardList = new ArrayList<>();

    public void setList(List<PostCardModel> postCardList) {
        this.postCardList = postCardList;
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

        /* handle_like_start */
        holder.binding.likeIcon.setOnClickListener(v -> {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            PostRepository.getInstance()
                    .toggleLike(item.postId, userId);

            v.setSelected(!v.isSelected());  // 🔥 toggles color via selector
            animateLike(v);                 // optional animation
        });
        /* handle_like_end */
        holder.binding.executePendingBindings();
    }

    private void animateLike(View view) {
        view.animate()
                .scaleX(1.4f)
                .scaleY(1.4f)
                .setDuration(150)
                .withEndAction(() -> {
                    view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(150)
                            .start();
                })
                .start();
    }

    @Override
    public int getItemCount() {
        return postCardList.size();
    }

}
