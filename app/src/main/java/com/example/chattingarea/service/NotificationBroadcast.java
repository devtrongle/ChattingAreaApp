package com.example.chattingarea.service;

import static com.example.chattingarea.MainActivity.AVA_CURRENT;
import static com.example.chattingarea.MainActivity.NAME_CURRENT;
import static com.example.chattingarea.MainActivity.U_ID_CURRENT;
import static com.example.chattingarea.MainActivity.U_ID_OTHER;
import static com.example.chattingarea.ui.ChatDetailScreen.KEY_TEXT_REPLY;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.RemoteInput;

import com.example.chattingarea.Constant;
import com.example.chattingarea.R;
import com.example.chattingarea.Utils;
import com.example.chattingarea.model.MessageDetailDto;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;

public class NotificationBroadcast extends BroadcastReceiver {

    public static int REQUEST_ID = 1;
    private DatabaseReference mRoomRef;
    private FirebaseDatabase mDatabase;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("TAG", "onReceive: " + intent.getStringExtra(U_ID_CURRENT));
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null) {
            mDatabase = FirebaseDatabase.getInstance();
            mRoomRef = mDatabase.getReference(Constant.ROOM_REF);
            String mess = remoteInput.getCharSequence(KEY_TEXT_REPLY).toString();
            String uIdCurrent = intent.getStringExtra(U_ID_CURRENT);
            String uIdOther = intent.getStringExtra(U_ID_OTHER);
            String nameCurrent = intent.getStringExtra(NAME_CURRENT);
            String avaCurrent = intent.getStringExtra(AVA_CURRENT);

            String key = Utils.generateString();
            MessageDetailDto messDto = new MessageDetailDto(
                    key,
                    mess,
                    new Date(),
                    true,
                    uIdCurrent,
                    nameCurrent,
                    avaCurrent,Constant.TEXT
            );

            mRoomRef.child(uIdCurrent).child(uIdOther).child(Utils.generateString()).setValue(messDto);
            mRoomRef.child(uIdOther).child(uIdCurrent).child(Utils.generateString()).setValue(messDto);
            Log.d("TAG", "handleIntent: " + mess);
//            addChat(remoteInput.getCharSequence(
//                    KEY_TEXT_REPLY).toString());
            String channelId = String.valueOf(R.string.default_notification_channel_id);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            // Since android Oreo notification channel is needed.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(channelId,
                        "Channel human readable title",
                        NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.cancel(channelId, REQUEST_ID);
            }
            notificationManager.cancel(REQUEST_ID);
        }
    }
}
