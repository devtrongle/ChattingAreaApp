package com.example.chattingarea;

import static com.example.chattingarea.ui.ChatDetailScreen.KEY_TEXT_REPLY;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.RemoteInput;

import com.example.chattingarea.model.Contact;
import com.example.chattingarea.model.MessageDetailDto;
import com.example.chattingarea.model.UserDto;
import com.example.chattingarea.service.MessageService;
import com.example.chattingarea.service.NotificationBroadcast;
import com.example.chattingarea.service.NotificationRequestBroadcast;
import com.example.chattingarea.ui.ChatGroup_Screen;
import com.example.chattingarea.ui.ChatOverviewScreen;
import com.example.chattingarea.ui.ContactsFragment;
import com.example.chattingarea.ui.HomeScreen;
import com.example.chattingarea.ui.LoginScreen;
import com.example.chattingarea.ui.ProfileScreen;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    public static final String ACTION_ACCEPT = "ACEEPT";
    public static final String ACTION_REJECT = "REJECT";
    public static final String U_ID_CURRENT = "uIDCurrent";
    public static final String RUN_BG = "BG";
    public static final String U_ID_OTHER = "uIdOther";
    public static final String NAME_CURRENT = "nameCurrent";
    public static final String AVA_CURRENT = "avaCurrent";
    private final static String TAG = MainActivity.class.getSimpleName();
    public static int REQUEST_ID = 1;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mContact;
    private DatabaseReference mUserRef;
    private DatabaseReference mRoomRef;
    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDatabase = FirebaseDatabase.getInstance();
        mRoomRef = mDatabase.getReference(Constant.ROOM_REF);
        mUserRef = mDatabase.getReference(Constant.USER_REF);
        mContact = mDatabase.getReference(Constant.CONTACTS_REF);
        mFirebaseAuth = FirebaseAuth.getInstance();
        initView();
        initAction();
        initData();
        sendNotification();
        sendRequestNoti();
        Intent pushIntent = new Intent(getApplicationContext(), MessageService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(pushIntent);
        } else {
            startService(pushIntent);
        }
    }

    private void sendRequestNoti() {
                mContact.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ArrayList<Contact> newContact = new ArrayList<>();
                        for (DataSnapshot s: snapshot.getChildren() ) {
                            newContact.add(s.getValue(Contact.class));
                        }
                        Collections.sort(newContact, new Comparator<Contact>() {
                            @Override
                            public int compare(Contact messageDetailDto,
                                               Contact t1) {
                                return Long.compare(t1.getTime(),
                                        messageDetailDto.getTime());
                            }
                        });
                                    if (newContact.size() > 0) {
                                        Log.d(TAG, "onDataChange newContact.get(0).getStatus(): " + newContact.get(0).getStatus());
                                        if (!newContact.get(0).getStatus().equals(Constant.StatusContacts.FRIEND)) {
                                            if (FirebaseAuth.getInstance().getUid().equals(newContact.get(0).getDestination())) {
                                                mUserRef.get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
                                                    @Override
                                                    public void onSuccess(DataSnapshot dataSnapshot) {
                                                        UserDto user = null;
                                                        for (DataSnapshot d : dataSnapshot.getChildren()) {
                                                            if (d.getKey().equals(newContact.get(0).getAuth())) {
                                                                user = d.getValue(UserDto.class);
                                                            }
                                                        }
                                                        String channelId = getString(R.string.default_notification_channel_id_request);

                                                        Intent accept = new Intent(getApplicationContext(), NotificationRequestBroadcast.class);
                                                        accept.setAction(ACTION_ACCEPT);
                                                        accept.putExtra("CONTACT",newContact.get(0).getId());

                                                        Intent reject = new Intent(getApplicationContext(), NotificationRequestBroadcast.class);
                                                        reject.setAction(ACTION_REJECT);
                                                        reject.putExtra("CONTACT",newContact.get(0).getId());
                                                        Log.d("TAG", "updateContact Test UsER: "+user.getName());

                                                        Log.d("TAG", "updateContact Test: "+newContact.get(0).getId());

                                                        PendingIntent pendingIntentAccept =
                                                                PendingIntent.getBroadcast(getApplicationContext(), 0, accept, PendingIntent.FLAG_UPDATE_CURRENT);

                                                        PendingIntent pendingIntentReject =
                                                                PendingIntent.getBroadcast(getApplicationContext(), 1, reject, PendingIntent.FLAG_UPDATE_CURRENT);
//                                                        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(),
//                                                                REQUEST_ID, accept,PendingIntent.FLAG_UPDATE_CURRENT);


                                                        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), channelId)
                                                                .setSmallIcon(R.drawable.ic_lock)
                                                                .setContentTitle("Lời mời kết bạn")
                                                                .setContentText("Từ " + user.getName())
                                                                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                                                                .setContentIntent(pendingIntentAccept)
                                                                .setContentIntent(pendingIntentReject)
                                                                .addAction(R.drawable.ic_lock, getString(R.string.accept),
                                                                        pendingIntentAccept)
                                                                .addAction(R.drawable.ic_check, getString(R.string.reject),
                                                                        pendingIntentReject);

                                                        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());

                                                        // Since android Oreo notification channel is needed.
                                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                            NotificationChannel channel = new NotificationChannel(channelId,
                                                                    "Channel human readable title",
                                                                    NotificationManager.IMPORTANCE_DEFAULT);
                                                            notificationManager.createNotificationChannel(channel);
                                                        }
                                                        notificationManager.notify(1112312, notificationBuilder.build());
                                                    }
                                                });


                                            }
                                        }
                                    }

                            }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private UserDto getUserFromDb(HashMap value) {
        UserDto userDto = new UserDto();
        userDto.setName((String) value.get("name"));
        userDto.setAddress((String) value.get("address"));
        userDto.setPhoneNumber((String) value.get("phoneNumber"));
        userDto.setAge((String) value.get("age"));
        userDto.setId((String) value.get("id"));
        userDto.setGender(false);
        userDto.setUrlAva((String) value.get("urlAva"));
        return userDto;
    }

    public UserDto getUserByUId(String uid, ArrayList<UserDto> al){
        for (UserDto u :
                al) {
            if (u.getId().equals(uid)) return u;
        }
        return null;
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            openLoginScreen();
            finish();
        }
    }

    private void initView() {
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mUserRef = mDatabase.getReference(Constant.USER_REF);
    }

    private void initAction() {
    }

    private void initData() {
        checkProfile();
    }

    private void sendNotification() {
        if(FirebaseAuth.getInstance().getUid() == null) return;

        mRoomRef.child(FirebaseAuth.getInstance().getUid()).addValueEventListener(
                new ValueEventListener() {
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
        mRoomRef.child(FirebaseAuth.getInstance().getUid()).child(s).addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.e("TAG", "DataSnapshot: " + s);

                        for (DataSnapshot s : snapshot.getChildren()) {
                            mess.add(s.getValue(MessageDetailDto.class));
                        }
                        Collections.sort(mess, new Comparator<MessageDetailDto>() {
                            @Override
                            public int compare(MessageDetailDto messageDetailDto,
                                    MessageDetailDto t1) {
                                return Long.compare(t1.getTimestamp().getTime(),
                                        messageDetailDto.getTimestamp().getTime());
                            }
                        });
                        sendNotificationMess(mess.get(0), s);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

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
            PendingIntent pendingIntent;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                pendingIntent = PendingIntent.getBroadcast(this,
                        REQUEST_ID, intent, PendingIntent.FLAG_MUTABLE);
            }
            else
            {
                pendingIntent = PendingIntent.getBroadcast(this,
                        REQUEST_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            }
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


    private void checkProfile() {
        mUserRef.child(mAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) {
                    openProfileScreen();
                    return;
                }
                UserDto userDto = dataSnapshot.getValue(UserDto.class);
                if (TextUtils.isEmpty(userDto.getName())) {
                    openProfileScreen();
                } else {
                    openHomeScreen();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w(TAG, "Failed to read value.", error.toException());
                openLoginScreen();
            }
        });
    }

    public void openProfileScreen() {
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.home_container, ProfileScreen.class, null)
                .addToBackStack(ProfileScreen.class.getSimpleName())
                .commitAllowingStateLoss();
    }

    public void openChatScreen() {
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.home_container, ChatOverviewScreen.class, null)
                .addToBackStack(ChatOverviewScreen.class.getSimpleName())
                .commitAllowingStateLoss();
    }

    public void openContactsScreen() {
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.home_container, ContactsFragment.class, null)
                .addToBackStack(ContactsFragment.class.getSimpleName())
                .commitAllowingStateLoss();
    }

    public void openHomeScreen() {
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.home_container, HomeScreen.class, null)
                .addToBackStack(HomeScreen.class.getSimpleName())
                .commitAllowingStateLoss();
    }

    public void openLoginScreen() {
        mAuth.signOut();
        Intent intent = new Intent(this, LoginScreen.class);
        startActivity(intent);
    }

    public void openChatGroup() {
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.home_container, ChatGroup_Screen.class, null)
                .addToBackStack(ChatGroup_Screen.class.getSimpleName())
                .commitAllowingStateLoss();
    }
}