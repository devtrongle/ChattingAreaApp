package com.example.chattingarea.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chattingarea.R;
import com.example.chattingarea.model.UserDto;

import java.util.Objects;

public class MyFriendAdapter extends
        ListAdapter<UserDto, MyFriendAdapter.FriendRequestViewHolder> {

    static DiffUtil.ItemCallback<UserDto> diffCallbackMyFriend = new DiffUtil.ItemCallback<UserDto>() {
        @Override
        public boolean areItemsTheSame(@NonNull UserDto oldItem, @NonNull UserDto newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull UserDto oldItem, @NonNull UserDto newItem) {
            return Objects.equals(oldItem, newItem);
        }
    };

    private ClickListener mClickListener;

    public void setOnClickListener(ClickListener listener){
        this.mClickListener = listener;
    }

    public MyFriendAdapter() {
        super(diffCallbackMyFriend);
    }

    public interface ClickListener {
        void onSendRequestClick(UserDto userDto, int position);
    }

    @NonNull
    @Override
    public FriendRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FriendRequestViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_friend, parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull FriendRequestViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class FriendRequestViewHolder extends RecyclerView.ViewHolder{
        private ImageView imvAvatar;
        private TextView tvName;
        private TextView tvPhone;

        public FriendRequestViewHolder(@NonNull View itemView) {
            super(itemView);
            imvAvatar = itemView.findViewById(R.id.avatar_image_view);
            tvName = itemView.findViewById(R.id.tvName);
            tvPhone = itemView.findViewById(R.id.tvPhone);
        }

        public void bind(UserDto userDto){
            tvName.setText(userDto.getName());
            tvPhone.setText(userDto.getPhoneNumber());

            Glide.with(imvAvatar.getContext())
                    .load(userDto.getUrlAva())
                    .placeholder(R.drawable.img)
                    .into(imvAvatar);
        }
    }
}
