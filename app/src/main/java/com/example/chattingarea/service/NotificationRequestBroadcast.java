package com.example.chattingarea.service;

import static com.example.chattingarea.MainActivity.ACTION_ACCEPT;
import static com.example.chattingarea.MainActivity.ACTION_REJECT;
import static com.example.chattingarea.MainActivity.REQUEST_ID;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationManagerCompat;

import com.example.chattingarea.Constant;
import com.example.chattingarea.R;
import com.example.chattingarea.RealtimeDatabaseUtils;
import com.example.chattingarea.model.Contact;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class NotificationRequestBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String id = intent.getStringExtra("CONTACT");
        Log.d("TAG", "updateContact: "+id);
        if (action == ACTION_ACCEPT) {
            FirebaseDatabase.getInstance().getReference(Constant.CONTACTS_REF).child(id).child("status").setValue(Constant.StatusContacts.FRIEND);
            Toast.makeText(context, "Đã chấp nhận lời mời!", Toast.LENGTH_SHORT).show();
        }
        if (action == ACTION_REJECT) {
            FirebaseDatabase.getInstance().getReference(Constant.CONTACTS_REF).child(id).child("status").setValue(Constant.StatusContacts.FRIEND);
//            RealtimeDatabaseUtils.getInstance(context).deleteContact();
            Toast.makeText(context, "Đã từ chối lời mời!", Toast.LENGTH_SHORT).show();
        }


        String channelId = String.valueOf(R.string.default_notification_channel_id_close);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.cancel(channelId, 1112312);
        }
        notificationManager.cancel(1112312);

    }
}
