package com.example.chattingarea.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chattingarea.R;
import com.example.chattingarea.model.GroupDto;
import com.example.chattingarea.model.UserChatOverview;
import com.github.siyamed.shapeimageview.CircularImageView;

import java.util.ArrayList;

public class GroupChatOverviewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private ArrayList<GroupDto> listData;
    private ClickListener mClickListener;

    public GroupChatOverviewAdapter(Context context, ArrayList<GroupDto> listData, ClickListener clickListener) {
        this.context = context;
        this.listData = listData;
        this.mClickListener = clickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new ChatOverviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ChatOverviewViewHolder) {
            GroupDto data = listData.get(position);
            ((ChatOverviewViewHolder) holder).tvName.setText(data.getgName());
            ((ChatOverviewViewHolder) holder).ivAdd.setVisibility(View.GONE);
            ((ChatOverviewViewHolder) holder).clContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mClickListener.onItemClick(data.getgId());
                }
            });

            ((ChatOverviewViewHolder) holder).clContainer.setOnLongClickListener(
                    new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            mClickListener.onItemLongClick(data);
                            return true;
                        }
                    });
        }
    }

    @Override
    public int getItemCount() {
        return listData == null ? 0 : listData.size();
    }

    public void updateData(ArrayList<GroupDto> listData) {
        this.listData.clear();
        this.listData.addAll(listData);
        this.notifyDataSetChanged();
    }

    static class ChatOverviewViewHolder extends RecyclerView.ViewHolder {
        public TextView tvName;
        public ConstraintLayout clContainer;
        public ImageView ivAdd;

        public ChatOverviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.nam_tv);
            clContainer = itemView.findViewById(R.id.item_group_overview_container);
            ivAdd = itemView.findViewById(R.id.add_user_btn);
        }
    }

    public interface ClickListener {
        void onItemClick(String id);
        void onItemLongClick(GroupDto groupDto);
    }

}


