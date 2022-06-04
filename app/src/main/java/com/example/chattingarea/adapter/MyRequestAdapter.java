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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MyRequestAdapter extends RecyclerView.Adapter<MyRequestAdapter.MyRequestViewHolder> {
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


    @NonNull
    @Override
    public MyRequestAdapter.MyRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_my_request_friend, parent, false);
        return new MyRequestViewHolder(view);
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
    public void onBindViewHolder(@NonNull MyRequestViewHolder holder, int position) {
        UserDto userDto = getUserByUId(listContacts.get(position).getDestination());
        holder.tvName.setText("Bạn đã gửi lời mời kết bạn đến " + userDto.getName());
        holder.tvTime.setText(TimeUtils.getTimeAgo(new Date(listContacts.get(position).getTime())));

        holder.btnCancel.setOnClickListener(view -> {
            if(mClickListener != null){
                mClickListener.onCancelClick(userDto, listContacts.get(position), position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listContacts == null ? 0 : listContacts.size();
    }


    class MyRequestViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvTime;
        Button btnCancel;

        public MyRequestViewHolder(View itemView) {
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
