package com.example.snackstream.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snackstream.databinding.StoryItemLayoutBinding;
import com.example.snackstream.models.StoryItemModel;

import java.util.List;

public class StoryItemAdapter extends RecyclerView.Adapter<StoryItemAdapter.ViewHolder> {
    private List<StoryItemModel> storyItems;

    public void setList(List<StoryItemModel> storyItems) {
        this.storyItems = storyItems;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        StoryItemLayoutBinding binding;
        public ViewHolder(StoryItemLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        StoryItemLayoutBinding binding = StoryItemLayoutBinding.inflate(inflater, parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        StoryItemModel item = storyItems.get(position);
        holder.binding.setItem(item);
        holder.binding.executePendingBindings();
    }

    public int getItemCount() {
        return storyItems.size();
    }


}
