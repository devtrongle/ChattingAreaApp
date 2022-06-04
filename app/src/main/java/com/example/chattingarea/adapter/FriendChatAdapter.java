package com.example.chattingarea.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chattingarea.Constant;
import com.example.chattingarea.R;
import com.example.chattingarea.model.MessageDetailDto;
import com.example.chattingarea.model.UserDto;
import com.github.siyamed.shapeimageview.CircularImageView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class FriendChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final static int TYPE_CHAT_LEFT = 1;
    private final static int TYPE_CHAT_RIGHT = 2;

    private Context context;
    private ArrayList<MessageDetailDto> listData;
    private UserDto mUserDto;

    public FriendChatAdapter(Context context, ArrayList<MessageDetailDto> listData, UserDto userDto) {
        this.context = context;
        this.listData = listData;
        this.mUserDto = userDto;
    }

    public void updateListData(ArrayList<MessageDetailDto> listData, UserDto userDto) {
        this.listData = listData;
        this.mUserDto = userDto;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_CHAT_LEFT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_chat_left, parent, false);
            return new ItemMessageLeftHolder(view);
        } else if (viewType == TYPE_CHAT_RIGHT) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_chat_right, parent, false);
            return new ItemMessageRightHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemMessageLeftHolder) {
            ((ItemMessageLeftHolder) holder).bind(listData.get(position), position, context);
        } else if (holder instanceof ItemMessageRightHolder) {
            ((ItemMessageRightHolder) holder).bind(listData.get(position), position, context);
        }
    }

    @Override
    public int getItemCount() {
        return listData == null ? 0 : listData.size();
    }

    @Override
    public int getItemViewType(int position) {
        return listData.get(position).getuId().equals(mUserDto.getId()) ? TYPE_CHAT_RIGHT : TYPE_CHAT_LEFT;
    }

    static class ItemMessageLeftHolder extends RecyclerView.ViewHolder {

        CircularImageView ciAva;
        TextView tvName;
        TextView tvMess;
        TextView tvTimestamp;
        ImageView ivPick;
        ImageView ivVideo;

        public ItemMessageLeftHolder(View itemView) {
            super(itemView);
            ciAva = itemView.findViewById(R.id.item_left_ava);
            tvName = itemView.findViewById(R.id.item_left_name);
            tvMess = itemView.findViewById(R.id.item_left_tv_message);
            tvTimestamp = itemView.findViewById(R.id.item_left_tv_timestamp);
            ivPick = itemView.findViewById(R.id.item_left_iv_message);
            ivVideo = itemView.findViewById(R.id.item_left_play_video_message);
        }

        public void bind(MessageDetailDto messageDetailDto, int position, Context context) {
            if (TextUtils.isEmpty(messageDetailDto.getContent())) {
                itemView.setVisibility(View.GONE);
            } else {
                itemView.setVisibility(View.VISIBLE);
            }
            Glide.with(context)
                    .load(messageDetailDto.getuAva()) // image url
                    .placeholder(R.drawable.img) // any placeholder to load at start
                    .error(R.drawable.img)  // any image in case of error
                    .override(200, 200) // resizing
                    .centerCrop()
                    .into(ciAva);
            tvName.setText(messageDetailDto.getuName());
            if (messageDetailDto.getType().equals(Constant.TEXT)) {
                tvMess.setVisibility(View.VISIBLE);
                ivPick.setVisibility(View.GONE);
                ivVideo.setVisibility(View.GONE);
                tvMess.setText(messageDetailDto.getContent());
            } else if (messageDetailDto.getType().equals(Constant.IMAGE)){
                tvMess.setVisibility(View.GONE);
                ivVideo.setVisibility(View.GONE);
                ivPick.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(messageDetailDto.getContent()) // image url
                        .placeholder(R.drawable.img) // any placeholder to load at start
                        .error(R.drawable.img)  // any image in case of error
                        .into(ivPick);
            }else{
                tvMess.setVisibility(View.GONE);
                ivPick.setVisibility(View.VISIBLE);
                ivVideo.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(messageDetailDto.getContent()) // image url
                        .placeholder(R.drawable.img) // any placeholder to load at start
                        .error(R.drawable.img)  // any image in case of error
                        .into(ivPick);
            }

            SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("hh:mm:ss a");
            String dateStr = DATE_FORMAT.format(messageDetailDto.getTimestamp());
            tvTimestamp.setText(dateStr);
        }
    }

    static class ItemMessageRightHolder extends RecyclerView.ViewHolder {
        TextView tvMess;
        TextView tvTimestamp;
        ImageView ivPick;
        ImageView ivVideo;

        public ItemMessageRightHolder(View itemView) {
            super(itemView);
            tvMess = itemView.findViewById(R.id.item_right_tv_message);
            tvTimestamp = itemView.findViewById(R.id.item_right_tv_timestamp);
            ivPick = itemView.findViewById(R.id.item_right_iv_message);
            ivVideo = itemView.findViewById(R.id.item_right_play_video_message);
        }

        public void bind(MessageDetailDto messageDetailDto, int position, Context context) {
            if (TextUtils.isEmpty(messageDetailDto.getContent())) {
                itemView.setVisibility(View.GONE);
            } else {
                itemView.setVisibility(View.VISIBLE);
            }

            if (messageDetailDto.getType().equals(Constant.TEXT)) {
                tvMess.setVisibility(View.VISIBLE);
                ivPick.setVisibility(View.GONE);
                ivVideo.setVisibility(View.GONE);
                tvMess.setText(messageDetailDto.getContent());
            }else if (messageDetailDto.getType().equals(Constant.IMAGE)) {
                tvMess.setVisibility(View.GONE);
                ivVideo.setVisibility(View.GONE);
                ivPick.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(messageDetailDto.getContent()) // image url
                        .placeholder(R.drawable.img) // any placeholder to load at start
                        .error(R.drawable.img)  // any image in case of error
                        .into(ivPick);
            } else {
                tvMess.setVisibility(View.GONE);
                ivVideo.setVisibility(View.VISIBLE);
                ivPick.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(messageDetailDto.getContent()) // image url
                        .placeholder(R.drawable.img) // any placeholder to load at start
                        .error(R.drawable.img)  // any image in case of error
                        .into(ivPick);
            }


            SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("hh:mm:ss a");
            String dateStr = DATE_FORMAT.format(messageDetailDto.getTimestamp());
            tvTimestamp.setText(dateStr);
        }
    }
}
