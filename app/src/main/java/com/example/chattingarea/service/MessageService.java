package com.example.chattingarea.service;

import static com.example.chattingarea.MainActivity.RUN_BG;
import static com.example.chattingarea.service.NotificationBroadcast.REQUEST_ID;
import static com.example.chattingarea.ui.ChatDetailScreen.KEY_TEXT_REPLY;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.RemoteInput;

import com.example.chattingarea.Constant;
import com.example.chattingarea.R;
import com.example.chattingarea.model.MessageDetailDto;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class MessageService extends Service {
    public static final String U_ID_CURRENT = "uIDCurrent";
    public static final String U_ID_OTHER = "uIdOther";
    public static final String NAME_CURRENT = "nameCurrent";
    public static final String AVA_CURRENT = "avaCurrent";

    private FirebaseDatabase mDatabase;
    private DatabaseReference mUserRef;
    private DatabaseReference mRoomRef;
    private FirebaseAuth mFirebaseAuth;

    private void sendNotificationMess(MessageDetailDto remoteMessage, String s) {
        if (!remoteMessage.getuId().equals(FirebaseAuth.getInstance().getUid())) {
            String replyLabel = getResources().getString(R.string.reply_label);
            RemoteInput remoteInput = new RemoteInput.Builder(KEY_TEXT_REPLY)
                    .setLabel(replyLabel)
                    .build();

            Intent intent = new Intent(this, NotificationBroadcast.class);
            intent.putExtra(U_ID_CURRENT, FirebaseAuth.getInstance().getUid());
            intent.putExtra(U_ID_OTHER, s);
            intent.putExtra(NAME_CURRENT, remoteMessage.getuName());
            intent.putExtra(AVA_CURRENT, remoteMessage.getuAva());
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this,
                    REQUEST_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Action replyAction =
                    new NotificationCompat.Action.Builder(
                            R.drawable.ic_launcher_background,
                            "Reply", pendingIntent)
                            .addRemoteInput(remoteInput)
                            .build();


            String channelId = getString(R.string.default_notification_channel_id);
            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(this, channelId)
                            .setSmallIcon(R.drawable.ic_lock)
                            .setContentTitle(remoteMessage.getuName())
                            .setContentText(remoteMessage.getContent())
                            .setAutoCancel(true)
                            .setSound(defaultSoundUri)
                            .setContentIntent(pendingIntent)
                            .addAction(replyAction);
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

            // Since android Oreo notification channel is needed.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(channelId,
                        "Channel human readable title",
                        NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(channel);
            }
            notificationManager.notify(REQUEST_ID, notificationBuilder.build());
        }
    }

    private void sendNotification(String uIdCurrenr) {
        mRoomRef.child(uIdCurrenr).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("TAG", "onChildAdded: %s" + snapshot.getChildren());
                for (DataSnapshot s : snapshot.getChildren()) {
                    Log.d("TAG", "DataSnapshot: %s" + s.getKey());
                    listenerChildMess1(s.getKey());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void listenerChildMess1(String s) {
        List<MessageDetailDto> mess = new ArrayList<>();
        mRoomRef.child(FirebaseAuth.getInstance().getUid()).child(s).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.e("TAG", "DataSnapshot: " + s);

                for (DataSnapshot s : snapshot.getChildren()) {
                    mess.add(s.getValue(MessageDetailDto.class));
                }
                Collections.sort(mess, new Comparator<MessageDetailDto>() {
                    @Override
                    public int compare(MessageDetailDto messageDetailDto, MessageDetailDto t1) {
                        return Long.compare(t1.getTimestamp().getTime(), messageDetailDto.getTimestamp().getTime());
                    }
                });
                sendNotificationMess(mess.get(0), s);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onCreate() {
        mDatabase = FirebaseDatabase.getInstance();
        mRoomRef = mDatabase.getReference(Constant.ROOM_REF);
        mUserRef = mDatabase.getReference(Constant.USER_REF);
        mFirebaseAuth = FirebaseAuth.getInstance();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("Service", "service starting");
        Log.d("Service", "service starting"+ intent.getStringExtra(U_ID_CURRENT));

        sendNotification(intent.getStringExtra(U_ID_CURRENT));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    RUN_BG,
                    RUN_BG,
                    NotificationManager.IMPORTANCE_LOW
            );
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
            NotificationCompat.Builder notification = new NotificationCompat.Builder(this,RUN_BG)
                    .setContentText("Chat App running")
                    .setContentTitle("Chat App enable")
                    .setSmallIcon(R.drawable.ic_lock);
            startForeground(101, notification.build());
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d("Service", "service done");
    }
}
