package com.example.chattingarea.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chattingarea.Constant;
import com.example.chattingarea.R;
import com.example.chattingarea.RealtimeDatabaseUtils;
import com.example.chattingarea.model.Contact;
import com.example.chattingarea.model.UserDto;
import com.google.firebase.auth.FirebaseAuth;


import java.util.List;
import java.util.Objects;

public class FindFriendAdapter extends
        ListAdapter<UserDto, FindFriendAdapter.FriendRequestViewHolder> {

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

    private final boolean isFindFriend;

    public FindFriendAdapter(boolean isFindFriend) {
        super(diffCallback);
        this.isFindFriend = isFindFriend;
    }

    @NonNull
    @Override
    public FriendRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FriendRequestViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_find_friend, parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull FriendRequestViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class FriendRequestViewHolder extends RecyclerView.ViewHolder{
        private ImageView imvAvatar;
        private TextView tvName;
        private TextView tvPhone;
        private Button btnSendRequest;

        public FriendRequestViewHolder(@NonNull View itemView) {
            super(itemView);
            imvAvatar = itemView.findViewById(R.id.avatar_image_view);
            tvName = itemView.findViewById(R.id.tvName);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            btnSendRequest = itemView.findViewById(R.id.send_request_button);
        }

        public void bind(UserDto userDto){
            tvName.setText(userDto.getName());
            tvPhone.setText(userDto.getPhoneNumber());

            Glide.with(imvAvatar.getContext())
                    .load(userDto.getUrlAva())
                    .placeholder(R.drawable.img)
                    .into(imvAvatar);

            btnSendRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    RealtimeDatabaseUtils.getInstance(view.getContext()).sendRequestContact(
                            userDto.getId(), new RealtimeDatabaseUtils.ISendRequestContact() {
                                @Override
                                public void onCompletedSendRequestContact(
                                        Constant.StatusRequest statusRequest, String message) {
                                    if(statusRequest == Constant.StatusRequest.SUCCESS){
                                        Toast.makeText(view.getContext(), "Gửi kết bạn thành công!",
                                                Toast.LENGTH_SHORT).show();
                                    }else{
                                        Toast.makeText(view.getContext(), "Gửi kết bạn thất bại!",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            });

            if(!isFindFriend){
                btnSendRequest.setVisibility(View.GONE);
            }
        }
    }
}
