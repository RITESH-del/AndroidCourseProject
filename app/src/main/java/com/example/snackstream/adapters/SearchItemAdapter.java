package com.example.snackstream.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snackstream.databinding.SearchItemsBinding;
import com.example.snackstream.models.SearchItemModel;

import java.util.ArrayList;
import java.util.List;

public class SearchItemAdapter extends RecyclerView.Adapter<SearchItemAdapter.ViewHolder> {

    private List<SearchItemModel> list = new ArrayList<>();
    private OnFollowClickListener listener;

    public void setList(List<SearchItemModel> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public void setOnFollowClickListener(OnFollowClickListener listener) {
        this.listener = listener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        SearchItemsBinding binding;

        public ViewHolder(SearchItemsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        SearchItemsBinding binding = SearchItemsBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SearchItemModel item = list.get(position);

        holder.binding.setItem(item);
        holder.binding.executePendingBindings();

        // Follow button handling
        holder.binding.btnFollow.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFollowClick(item, position);
            }
        });

        if (item.isFollowing()) {
            holder.binding.btnFollow.setText("Following");
            holder.binding.btnFollow.setIcon(null);
        } else {
            holder.binding.btnFollow.setText("Follow");
            holder.binding.btnFollow.setIcon(
                    holder.itemView.getContext().getDrawable(com.example.snackstream.R.drawable.ic_add)
            );
        }
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    // Interface for click handling
    public interface OnFollowClickListener {
        void onFollowClick(SearchItemModel item, int position);
    }
}
