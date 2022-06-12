package com.example.chattingarea.ui;

import static android.app.Activity.RESULT_OK;

import static com.example.chattingarea.Constant.VIDEO;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chattingarea.Constant;
import com.example.chattingarea.R;
import com.example.chattingarea.Utils;
import com.example.chattingarea.adapter.FriendChatAdapter;
import com.example.chattingarea.model.GroupDto;
import com.example.chattingarea.model.MessageDetailDto;
import com.example.chattingarea.model.UserDto;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class GroupChatDetailScreen extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final int OPEN_DOCUMENT_CODE = 22;

    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private static final int REQUEST_CAMERA = 211112;
    private String uIdOther;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mUserRef;
    private DatabaseReference mGroupRef;
    private DatabaseReference mGroupChatRef;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    private View mRootView;
    private RecyclerView mRcv;
    private EditText mEdtChat;
    private ImageView mBtnSend;
    private ImageView mBtnPick;
    private AppCompatImageView back;
    private TextView tvHeaderName;
    private ImageView mBtnScreenShot;

    private FriendChatAdapter friendChatAdapter;
    private UserDto currentUser;

    public GroupChatDetailScreen() {
    }

    public static GroupChatDetailScreen newInstance(String param1) {
        GroupChatDetailScreen fragment = new GroupChatDetailScreen();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            uIdOther = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_chat_detail_screen, container, false);
        initView();
        initAction();
        initData();
        return mRootView;
    }

    private void initView() {
        mDatabase = FirebaseDatabase.getInstance();
        mGroupRef = mDatabase.getReference(Constant.GROUP_REF);
        mGroupChatRef = mDatabase.getReference(Constant.GROUP_CHAT_REF);
        mUserRef = mDatabase.getReference(Constant.USER_REF);
        mFirebaseAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        mRcv = mRootView.findViewById(R.id.chat_detail_rcv);
        mEdtChat = mRootView.findViewById(R.id.chat_detail_edt_chat_box);
        mBtnSend = mRootView.findViewById(R.id.chat_detail_btn_send);
        mBtnPick = mRootView.findViewById(R.id.chat_detail_btn_pick);
        back = mRootView.findViewById(R.id.back);
        tvHeaderName = mRootView.findViewById(R.id.chat_detail_tv_title);
        mBtnScreenShot = mRootView.findViewById(R.id.chat_detail_btn_pick_screen_shot);

        friendChatAdapter = new FriendChatAdapter(getContext(), new ArrayList<>(), currentUser);
        mRcv.setLayoutManager(new LinearLayoutManager(getContext()));
        mRcv.setAdapter(friendChatAdapter);
    }

    private void initAction() {
        back.setOnClickListener(view -> requireActivity().onBackPressed());
        mBtnSend.setOnClickListener(view -> {
            String mess = mEdtChat.getText().toString();
            if (TextUtils.isEmpty(mess)) {
                return;
            } else {
                addChat(mEdtChat.getText().toString());
                mEdtChat.setText("");
            }
        });

        mBtnScreenShot.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(getContext(),
                    Manifest.permission.CAMERA) != PermissionChecker.PERMISSION_GRANTED)
            {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
            }
            else
            {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                someActivityResultLauncher.launch(cameraIntent);
            }
        });

        mBtnPick.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            startActivityForResult(intent, OPEN_DOCUMENT_CODE);
        });
    }

    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                        uploadImageFile(bitmap);
                    }
                }
            });

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (resultCode == RESULT_OK) {
            if (resultData != null) {
                Uri uri = resultData.getData();
                Bitmap bitmap = null;

                switch (requestCode){
                    case REQUEST_CAMERA:
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), uri);
                            uploadImageFile(bitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case OPEN_DOCUMENT_CODE:
                        if(getFileType(uri).equals("mp4")){
                            uploadVideo(uri);
                        }else{
                            try {
                                bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), uri);
                                uploadImageFile(bitmap);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                }
            }
        }
    }


    private String getFileType(Uri videoUri) {
        ContentResolver r = requireContext().getContentResolver();
        // get the file type ,in this case its mp4
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(r.getType(videoUri));
    }

    private void uploadVideo(Uri videoUri) {
        if (videoUri != null) {
            // save the selected video in Firebase storage
            final StorageReference reference = storageReference.child(Constant.ROOM_REF)
                    .child(System.currentTimeMillis() + "." + getFileType(videoUri));
            reference.putFile(videoUri).addOnSuccessListener(
                    new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot snapshot) {
                            snapshot.getStorage().getDownloadUrl().addOnCompleteListener(
                                    task -> {
                                        if (task.isSuccessful()) {
                                            addChatVideo(task.getResult().toString());
                                        }
                                    });
                        }
                    });
        }
    }


    public void uploadImageFile(Bitmap bitmap) {
        if (bitmap != null) {
            StorageReference imgRef = storageReference.child(Constant.ROOM_REF).child(Utils.generateString());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            byte[] data = baos.toByteArray();
            imgRef.putBytes(data).addOnSuccessListener(snapshot -> {
                snapshot.getStorage().getDownloadUrl().addOnCompleteListener(
                        task -> {
                            if (task.isSuccessful()) {
                                addChatImg(task.getResult().toString());
                            }
                        });
                Log.d("addChatImg", "upload img success!");
            }).addOnFailureListener(exception -> {
                Log.d("addChatImg", "upload img Fail!");
            });
        }else{
            Log.d("CheckApp", "bitmap == null");
        }
    }

    private void addChat(String mess) {
        String key = Utils.generateString();
        MessageDetailDto messDto = new MessageDetailDto(
                key, mess, new Date(), true, currentUser.getId(), currentUser.getName(), currentUser.getUrlAva(), Constant.TEXT
        );

        mGroupChatRef.child(uIdOther).child(Utils.generateString()).setValue(messDto);
    }

    private void addChatImg(String urlAva) {
        String key = Utils.generateString();
        MessageDetailDto messDto = new MessageDetailDto(
                key, urlAva, new Date(), false, currentUser.getId(), currentUser.getName(), currentUser.getUrlAva(), Constant.IMAGE
        );
        mGroupChatRef.child(uIdOther).child(Utils.generateString()).setValue(messDto);
    }


    private void addChatVideo(String urlAva) {
        String key = Utils.generateString();
        MessageDetailDto messDto = new MessageDetailDto(
                key, urlAva, new Date(), false, currentUser.getId(), currentUser.getName(), currentUser.getUrlAva(), VIDEO
        );
        mGroupChatRef.child(uIdOther).child(Utils.generateString()).setValue(messDto);
    }

    private void initData() {
        getCurrentUserData();
        setNameOtherUser();
        getHistoryMess();
    }

    private void getHistoryMess() {
        mGroupChatRef.child(uIdOther).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("getHistoryMess", "ok: " + snapshot);
                ArrayList<MessageDetailDto> list = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    MessageDetailDto mess = ds.getValue(MessageDetailDto.class);
                    list.add(mess);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Collections.sort(list, Comparator.comparing(MessageDetailDto::getTimestamp));
                }
                // update adapter
                friendChatAdapter.updateListData(list, currentUser);
                mRcv.scrollToPosition(list.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("getHistoryMess", "Fail ");
            }
        });
    }


    private void getCurrentUserData() {
        mUserRef.child(mFirebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserDto value = dataSnapshot.getValue(UserDto.class);
                currentUser = value;
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w("ProfileScreen", "Failed to read value.", error.toException());
            }
        });
    }

    private void setNameOtherUser() {
        mGroupRef.child(mFirebaseAuth.getUid()).child(uIdOther).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) return;
                GroupDto value = dataSnapshot.getValue(GroupDto.class);
                tvHeaderName.setText(value.getgName());
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w("ProfileScreen", "Failed to read value.", error.toException());
            }
        });
    }

}