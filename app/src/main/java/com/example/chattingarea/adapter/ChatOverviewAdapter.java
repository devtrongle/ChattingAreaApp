package com.example.chattingarea.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chattingarea.R;
import com.example.chattingarea.model.UserChatOverview;
import com.github.siyamed.shapeimageview.CircularImageView;

import java.util.ArrayList;

public class ChatOverviewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private ArrayList<UserChatOverview> listData;
    private ClickListener mClickListener;

    public ChatOverviewAdapter(Context context, ArrayList<UserChatOverview> listData, ClickListener clickListener) {
        this.context = context;
        this.listData = listData;
        mClickListener = clickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_overview, parent, false);
        return new ChatOverviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ChatOverviewViewHolder) {
            UserChatOverview data = listData.get(position);
            ((ChatOverviewViewHolder) holder).tvName.setText(data.getName());
            ((ChatOverviewViewHolder) holder).tvMessage.setText(data.getMessage());
            ((ChatOverviewViewHolder) holder).tvTimestamp.setText(data.getTimestamp());
            if (data.getUrlAva() != null) {
                Glide.with(context)
                        .load(data.getUrlAva()) // image url
                        .placeholder(R.drawable.img) // any placeholder to load at start
                        .error(R.drawable.img)  // any image in case of error
                        .override(200, 200) // resizing
                        .centerCrop()
                        .into(((ChatOverviewViewHolder) holder).ivAva);  // imageview object
            }
            ((ChatOverviewViewHolder) holder).clContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mClickListener.onItemClick(data.getId());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return listData == null ? 0 : listData.size();
    }


    public void updateData(ArrayList<UserChatOverview> listData) {
        this.listData.clear();
        this.listData.addAll(listData);
        this.notifyDataSetChanged();
    }

    static class ChatOverviewViewHolder extends RecyclerView.ViewHolder {
        public TextView tvName;
        public TextView tvMessage;
        public TextView tvTimestamp;
        public CircularImageView ivAva;
        public ConstraintLayout clContainer;

        public ChatOverviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.item_chat_overview_tv_name);
            tvMessage = itemView.findViewById(R.id.item_chat_overview_tv_detail);
            tvTimestamp = itemView.findViewById(R.id.item_chat_overview_tv_time);
            ivAva = itemView.findViewById(R.id.item_chat_overview_iv_ava);
            clContainer = itemView.findViewById(R.id.item_chat_overview_container);
        }
    }

    public interface ClickListener {
        void onItemClick(String id);
    }

}


