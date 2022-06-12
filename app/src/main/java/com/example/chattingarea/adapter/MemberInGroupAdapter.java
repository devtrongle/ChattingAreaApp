package com.example.chattingarea.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class MemberInGroupAdapter extends
        ListAdapter<UserDto, MemberInGroupAdapter.MemberInGroupViewHolder> {

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

    public MemberInGroupAdapter() {
        super(diffCallbackMyFriend);
    }

    public interface ClickListener {
        void onDeleteClick(UserDto userDto, int position);
    }

    @NonNull
    @Override
    public MemberInGroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MemberInGroupViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_member_in_group, parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull MemberInGroupViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class MemberInGroupViewHolder extends RecyclerView.ViewHolder{
        private ImageView imvAvatar;
        private ImageView imvDelete;
        private TextView tvName;
        private TextView tvPhone;

        public MemberInGroupViewHolder(@NonNull View itemView) {
            super(itemView);
            imvAvatar = itemView.findViewById(R.id.avatar_image_view);
            imvDelete = itemView.findViewById(R.id.delete_image_view);
            tvName = itemView.findViewById(R.id.tvName);
            tvPhone = itemView.findViewById(R.id.tvPhone);
        }

        public void bind(UserDto userDto){
            tvName.setText(userDto.getName());
            tvPhone.setText(userDto.getPhoneNumber());
            imvDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mClickListener != null)
                        mClickListener.onDeleteClick(userDto, getAdapterPosition());
                }
            });
            Glide.with(imvAvatar.getContext())
                    .load(userDto.getUrlAva())
                    .placeholder(R.drawable.img)
                    .into(imvAvatar);
        }
    }
}
