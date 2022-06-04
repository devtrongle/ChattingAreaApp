package com.example.chattingarea.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chattingarea.Constant;
import com.example.chattingarea.R;
import com.example.chattingarea.RealtimeDatabaseUtils;
import com.example.chattingarea.Utils;
import com.example.chattingarea.adapter.UserAdapter;
import com.example.chattingarea.model.Contact;
import com.example.chattingarea.model.GroupDto;
import com.example.chattingarea.model.MessageDetailDto;
import com.example.chattingarea.model.UserChatOverview;
import com.example.chattingarea.model.UserDto;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddPeopleChatGroupScreen extends Fragment implements UserAdapter.ClickListener {
    private View view;
    private RecyclerView recyclerView;
    private TextView tvDone;
    private ImageView tvBack;
    private TextInputLayout tilName;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mUserRef;
    private DatabaseReference mGroupRef;
    private DatabaseReference mGroupChatRef;
    private DatabaseReference mContactRef;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    private UserAdapter userAdapter;
    private ArrayList<UserChatOverview> listData;
    public ArrayList<UserDto> al;
    private UserDto currentUser;
    private String keyGroup;


    public static AddPeopleChatGroupScreen newInstance(String param1, String param2) {
        AddPeopleChatGroupScreen fragment = new AddPeopleChatGroupScreen();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return view = inflater.inflate(R.layout.fragment_add_people_chat_group_screen, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
        initAction();
        initData();
    }

    private void initView() {
        recyclerView = view.findViewById(R.id.user_add_rcv);
        tvDone = view.findViewById(R.id.chat_detail_tv_done);
        tvBack = view.findViewById(R.id.back);
        tvDone.setVisibility(View.GONE);
        tilName = view.findViewById(R.id.chat_group_til_name);

        mDatabase = FirebaseDatabase.getInstance();
        mUserRef = mDatabase.getReference(Constant.USER_REF);
        mGroupRef = mDatabase.getReference(Constant.GROUP_REF);
        mContactRef = mDatabase.getReference(Constant.CONTACTS_REF);
        mGroupChatRef = mDatabase.getReference(Constant.GROUP_Chat_REF);
        mFirebaseAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        userAdapter = new UserAdapter(getContext(), listData, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(userAdapter);
    }

    private void initAction() {
        tvDone.setOnClickListener(view -> requireActivity().onBackPressed());
        tvBack.setOnClickListener(view -> requireActivity().onBackPressed());
    }

    private void initData() {
        getCurrentUserData();
        readAllUser();
        keyGroup = Utils.generateString();
    }

    private void readAllUser() {
        mUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) return;
                HashMap<String, HashMap<String, UserDto>> selects = (HashMap) dataSnapshot.getValue();
                 al = new ArrayList<>();
                ArrayList<UserDto> alHaveFriend = new ArrayList<>();
                for (Map.Entry<String, HashMap<String, UserDto>> entry : selects.entrySet()) {
                    al.add(getUserFromDb(entry.getValue()));
                }
//                for (UserDto x : al) {
//                    mContactRef.addValueEventListener(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot snapshot) {
//                            for (DataSnapshot dataSnapshot1 : snapshot.getChildren()) {
//                                Contact contact = dataSnapshot1.getValue(Contact.class);
//                                if (FirebaseAuth.getInstance().getUid().equals(contact.getAuth())) {
//                                    if (x.id.equals(contact.getDestination()) && contact.getStatus() == "friend") {
//                                        alHaveFriend.add(x);
//                                    }
//                                }
//                            }
//                            userAdapter.updateData(mapListUserChat(alHaveFriend));
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError error) {
//
//                        }
//                    });
//                }
                RealtimeDatabaseUtils.getInstance(requireContext()).getAllContacts(new RealtimeDatabaseUtils.IGetAllContacts() {
                    @Override
                    public void onCompletedGetAllContacts(Constant.StatusRequest statusRequest, List<Contact> allContacts, String myUid, String message) {
                        for(Contact contact : allContacts){
                            if(contact.getStatus().equals(Constant.StatusContacts.FRIEND)){
                                if(myUid.equals(contact.getAuth())){
                                    alHaveFriend.add(getUserByUId(contact.getDestination(),al));
                                }else{
                                    alHaveFriend.add(getUserByUId(contact.getAuth(),al));
                                }
                            }

                        }
                        userAdapter.updateData(mapListUserChat(alHaveFriend));
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w("ProfileScreen", "Failed to read value.", error.toException());
            }

        });
    }

    public UserDto getUserByUId(String uid, ArrayList<UserDto> al){
        for (UserDto u :
                al) {
            if (u.getId().equals(uid)) return u;
        }
        return null;
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

    private ArrayList<UserChatOverview> mapListUserChat(ArrayList<UserDto> al) {
        ArrayList<UserChatOverview> result = new ArrayList<>();
        UserChatOverview uc = new UserChatOverview();
        for (UserDto u : al) {
            if (!u.getId().equals(mFirebaseAuth.getUid())) {
                uc = new UserChatOverview();
                uc.setId(u.getId());
                uc.setName(u.getName());
                uc.setMessage("Đã kết nối");
                uc.setUrlAva(u.getUrlAva());
                uc.setTimestamp(null);
                result.add(uc);
            }
        }
        return result;
    }

    @Override
    public void onItemClick(String id) {
        if (!addChatDummy) {
            addChatDummy = true;
            addUserToGroup(id);
        }
        addGroup(id);
        tvDone.setVisibility(View.VISIBLE);
    }

    private boolean addChatDummy = false;

    private void addUserToGroup(String id) {
        String keyMess = Utils.generateString();
        MessageDetailDto messDummy = new MessageDetailDto(
                keyMess, "", new Date(), true, currentUser.getId(), currentUser.getName(), currentUser.getUrlAva(), Constant.TEXT
        );
        mGroupChatRef.child(keyGroup).child(keyMess).setValue(messDummy);
    }

    private void addGroup(String otherID) {
        String name = tilName.getEditText().getText().toString();
        GroupDto g = new GroupDto(keyGroup, name);
        mGroupRef.child(mFirebaseAuth.getUid()).child(keyGroup).setValue(g);
        mGroupRef.child(otherID).child(keyGroup).setValue(g);
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
}