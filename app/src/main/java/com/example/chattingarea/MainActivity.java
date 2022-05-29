package com.example.chattingarea;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.example.chattingarea.model.UserDto;
import com.example.chattingarea.ui.ChatGroup_Screen;
import com.example.chattingarea.ui.ChatOverviewScreen;
import com.example.chattingarea.ui.HomeScreen;
import com.example.chattingarea.ui.LoginScreen;
import com.example.chattingarea.ui.ProfileScreen;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mUserRef;
    private final static String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initAction();
        initData();
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
    public void openChatGroup(){
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.home_container, ChatGroup_Screen.class, null)
                .addToBackStack(ChatGroup_Screen.class.getSimpleName())
                .commitAllowingStateLoss();
    }
}