package com.example.chattingarea.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chattingarea.R;
import com.example.chattingarea.TimeUtils;
import com.example.chattingarea.model.Contact;
import com.example.chattingarea.model.UserDto;

import java.util.Date;
import java.util.List;

public class MyRequestAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Contact> listContacts;
    private List<UserDto> listUsers;
    private ClickListener mClickListener;
    private Context context;

    public MyRequestAdapter(Context context, List<Contact> listContacts,
            List<UserDto> listUsers,
            ClickListener clickListener) {
        this.context = context;
        this.listContacts = listContacts;
        this.listUsers = listUsers;
        this.mClickListener = clickListener;
    }

    public void setDataSource(List<Contact> listContacts, List<UserDto> listUsers){
        this.listContacts = listContacts;
        this.listUsers = listUsers;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_my_request_friend, parent, false);
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
            ((UserViewHolder) holder).tvName.setText("Bạn đã gửi lời mời kết bạn đến " + userDto.getName());
            ((UserViewHolder) holder).tvTime.setText(TimeUtils.getTimeAgo(new Date(listContacts.get(position).getTime())));

            ((UserViewHolder) holder).btnCancel.setOnClickListener(view -> {
                if(mClickListener != null){
                    mClickListener.onCancelClick(userDto, listContacts.get(position), position);
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
        Button btnCancel;

        public UserViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvTime = itemView.findViewById(R.id.tvTime);
            btnCancel = itemView.findViewById(R.id.cancel_button);
        }
    }

    public interface ClickListener {
        void onCancelClick(UserDto userDto, Contact contact, int position);
    }

}
