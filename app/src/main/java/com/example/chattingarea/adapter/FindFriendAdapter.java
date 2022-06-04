package com.example.chattingarea.adapter;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chattingarea.model.UserDto;


import java.util.List;
import java.util.Objects;

public class FriendRequestAdapter extends
        ListAdapter<UserDto, FriendRequestAdapter.FriendRequestViewHolder> {

    static DiffUtil.ItemCallback<UserDto> diffCallback = new DiffUtil.ItemCallback<UserDto>() {
        @Override
        public boolean areItemsTheSame(@NonNull UserDto oldItem, @NonNull UserDto newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull UserDto oldItem, @NonNull UserDto newItem) {
            return Objects.equals(oldItem, newItem);
        }
    };

    protected FriendRequestAdapter() {
        super(diffCallback);
    }

    @NonNull
    @Override
    public FriendRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull FriendRequestViewHolder holder, int position) {

    }

    class FriendRequestViewHolder extends RecyclerView.ViewHolder{

        public FriendRequestViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
