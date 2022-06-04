package com.example.chattingarea.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chattingarea.R;
import com.example.chattingarea.model.Contact;
import com.example.chattingarea.model.UserChatOverview;
import com.example.chattingarea.model.UserDto;
import com.github.siyamed.shapeimageview.CircularImageView;

import java.util.ArrayList;
import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Contact> listContacts;
    private List<UserDto> listUsers;
    private ClickListener mClickListener;
    private Context context;

    public RequestAdapter(Context context, List<Contact> listContacts,
            List<UserDto> listUsers,
            ClickListener clickListener) {
        this.context = context;
        this.listContacts = listContacts;
        this.listUsers = listUsers;
        this.mClickListener = clickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_request_friend, parent, false);
        return new UserViewHolder(view);
    }

    private UserDto getUserByUId(String uid){
        for (UserDto u : listUsers) {
            if (u.getId().equals(uid)) return u;
        }
        return null;
    }

    public void removeItem(int position){
        listContacts.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof UserViewHolder) {
            UserDto userDto = getUserByUId(listContacts.get(position).getAuth());
            ((UserViewHolder) holder).tvName.setText(userDto.getName() + " đã gửi lời mời kết bạn!");
            ((UserViewHolder) holder).btnAccept.setOnClickListener(view -> {
                if(mClickListener != null){
                    mClickListener.onAcceptClick(userDto, listContacts.get(position), position);
                }
            });

            ((UserViewHolder) holder).btnDeny.setOnClickListener(view -> {
                if(mClickListener != null){
                    mClickListener.onDenyClick(userDto, listContacts.get(position), position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return listContacts == null ? 0 : listContacts.size();
    }


    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvTime;
        Button btnAccept;
        Button btnDeny;

        public UserViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvTime = itemView.findViewById(R.id.tvTime);
            btnDeny = itemView.findViewById(R.id.deny_button);
            btnAccept = itemView.findViewById(R.id.accept_button);
        }
    }

    public interface ClickListener {
        void onAcceptClick(UserDto userDto, Contact contact, int position);
        void onDenyClick(UserDto userDto, Contact contact, int position);
    }

}
