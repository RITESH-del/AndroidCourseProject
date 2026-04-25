package com.example.snackstream.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snackstream.databinding.CircularImageItemsBinding;
import com.example.snackstream.models.CircularImageModel;

import java.util.ArrayList;
import java.util.List;

public class CircularImageAdapter extends RecyclerView.Adapter<CircularImageAdapter.ViewHolder> {

    private List<CircularImageModel> circularImageList = new ArrayList<>();

    public void setList(List<CircularImageModel> circularImageList) {
        this.circularImageList = circularImageList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CircularImageItemsBinding binding;

        public ViewHolder(CircularImageItemsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        CircularImageItemsBinding binding = CircularImageItemsBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );

        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        CircularImageModel item = circularImageList.get(position);
        holder.binding.setItem(item);
        holder.binding.executePendingBindings();
    }

    @Override
    public int getItemCount() {
        return circularImageList.size();
    }
}