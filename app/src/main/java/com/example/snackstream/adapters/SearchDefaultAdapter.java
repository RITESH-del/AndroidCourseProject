package com.example.snackstream.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.snackstream.R;
import com.example.snackstream.databinding.SearchItemsBinding;
import com.example.snackstream.models.FollowRow;
import com.example.snackstream.models.SearchItemModel;
import com.example.snackstream.repository.SearchRepository;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class SearchDefaultAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<FollowRow> list = new ArrayList<>();
    private OnFollowClickListener listener;


    public void setData(List<SearchItemModel> followers, List<SearchItemModel> following) {

        list.clear();

        if (!followers.isEmpty()) {
            list.add(new FollowRow("Followers"));
            for (SearchItemModel user : followers) {
                list.add(new FollowRow(user));
            }
        }

        if (!following.isEmpty()) {
            list.add(new FollowRow("Following"));
            for (SearchItemModel user : following) {
                list.add(new FollowRow(user));
            }
        }

        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return list.get(position).type;
    }

    //  Header ViewHolder
    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView title;

        public HeaderViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.headerText);
        }
    }

    //  User ViewHolder
    static class UserViewHolder extends RecyclerView.ViewHolder {
        SearchItemsBinding binding;

        public UserViewHolder(SearchItemsBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == FollowRow.TYPE_HEADER) {
            View view = inflater.inflate(R.layout.item_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            SearchItemsBinding binding = SearchItemsBinding.inflate(inflater, parent, false);
            return new UserViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        FollowRow row = list.get(position);

        if (row.type == FollowRow.TYPE_HEADER) {
            ((HeaderViewHolder) holder).title.setText(row.title);
        } else {
            UserViewHolder vh = (UserViewHolder) holder;
            SearchItemModel item = row.user;

            vh.binding.setItem(item);
            vh.binding.executePendingBindings();

            String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            // ❌ Hide button if it's YOU
            if (item.userId.equals(currentUserId)) {
                vh.binding.btnFollow.setVisibility(View.GONE);
                return;
            } else {
                vh.binding.btnFollow.setVisibility(View.VISIBLE);
            }

            // 🔥 Click handling
            vh.binding.btnFollow.setOnClickListener(v -> {
                if (item.isFollowing()) {
                    SearchRepository.getInstance().unfollowUser(item.userId);
                    item.setFollowing(false);
                } else {
                    SearchRepository.getInstance().followUser(item.userId);
                    item.setFollowing(true);
                }
                notifyItemChanged(position);
            });

            // 🔥 UI state
            if (item.isFollowing()) {
                vh.binding.btnFollow.setText("Following");
                vh.binding.btnFollow.setIcon(null);
            } else {
                vh.binding.btnFollow.setText("Follow");
                vh.binding.btnFollow.setIcon(
                        vh.itemView.getContext().getDrawable(R.drawable.ic_add)
                );
            }
        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void setOnFollowClickListener(OnFollowClickListener listener) {
        this.listener = listener;
    }

    public interface OnFollowClickListener {
        void onFollowClick(SearchItemModel item, int position);
    }
}