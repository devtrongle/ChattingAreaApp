package com.example.chattingarea.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chattingarea.R;
import com.example.chattingarea.model.UserChatOverview;
import com.github.siyamed.shapeimageview.CircularImageView;

import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<UserChatOverview> listData;
    private ClickListener mClickListener;
    private String title = null;
    private Context context;

    public UserAdapter(Context context, ArrayList<UserChatOverview> listData, ClickListener clickListener) {
        this.context = context;
        this.listData = listData;
        mClickListener = clickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof UserViewHolder) {
            UserChatOverview userDto = listData.get(position);
            Glide.with(context)
                    .load(userDto.getUrlAva()) // image url
                    .placeholder(R.drawable.img) // any placeholder to load at start
                    .error(R.drawable.img)  // any image in case of error
                    .into(((UserViewHolder) holder).ciAva);
            ((UserViewHolder) holder).tvName.setText(userDto.getName());
            ((UserViewHolder) holder).imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mClickListener.onItemClick(userDto.getId());
                    ((UserViewHolder) holder).imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_check));
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return listData == null ? 0 : listData.size();
    }

    public void updateData(ArrayList<UserChatOverview> listData) {
        this.listData = listData;
        notifyDataSetChanged();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        CircularImageView ciAva;
        TextView tvName;
        ImageView imageView;

        public UserViewHolder(View itemView) {
            super(itemView);
            ciAva = itemView.findViewById(R.id.avatar_img);
            tvName = itemView.findViewById(R.id.nam_tv);
            imageView = itemView.findViewById(R.id.add_user_btn);
        }
    }

    public interface ClickListener {
        void onItemClick(String id);
    }

}
