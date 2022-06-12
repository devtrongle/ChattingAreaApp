package com.example.chattingarea.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.chattingarea.Constant;
import com.example.chattingarea.R;
import com.example.chattingarea.adapter.GroupChatOverviewAdapter;
import com.example.chattingarea.model.GroupDto;
import com.example.chattingarea.model.UserChatOverview;
import com.example.chattingarea.model.UserDto;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChatGroup_Screen extends Fragment implements GroupChatOverviewAdapter.ClickListener {
    private AppCompatImageView back;
    private View mRootView;
    private FloatingActionButton floatingActionButton;
    private RecyclerView mRcv;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mGroupRef;
    private DatabaseReference mUserRef;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    private ArrayList<GroupDto> listData = new ArrayList();
    private GroupChatOverviewAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return mRootView = inflater.inflate(R.layout.fragment_chat_gruop_screen, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
        initAction();
        initData();
    }

    private void initView() {
        back = mRootView.findViewById(R.id.back);
        floatingActionButton = mRootView.findViewById(R.id.add_float);
        mRcv = mRootView.findViewById(R.id.chat_group_rcv);

        mDatabase = FirebaseDatabase.getInstance();
        mGroupRef = mDatabase.getReference(Constant.GROUP_REF);
        mUserRef = mDatabase.getReference(Constant.USER_REF);
        mFirebaseAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        mAdapter = new GroupChatOverviewAdapter(getContext(), listData, this);
        mRcv.setLayoutManager(new LinearLayoutManager(getContext()));
        mRcv.setAdapter(mAdapter);
    }

    private void initAction() {
        back.setOnClickListener(view -> requireActivity().onBackPressed());
        floatingActionButton.setOnClickListener(view -> {
            createGroup();
        });
    }

    private void initData() {
        readAllGroup();
    }

    private void readAllGroup() {
        mGroupRef.child(mFirebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) return;
                HashMap<String, HashMap<String, GroupDto>> selects = (HashMap) dataSnapshot.getValue();
                ArrayList<GroupDto> al = new ArrayList<>();
                for (Map.Entry<String, HashMap<String, GroupDto>> entry : selects.entrySet()) {
                    al.add(getAllGroup(entry.getValue()));
                }
                mAdapter.updateData(al);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w("ProfileScreen", "Failed to read value.", error.toException());
            }
        });
    }

    private GroupDto getAllGroup(HashMap value) {
        GroupDto gDto = new GroupDto();
        gDto.setgId((String) value.get("gId"));
        gDto.setgName((String) value.get("gName"));
        return gDto;
    }

    private void createGroup() {
        AddPeopleChatGroupScreen cs = new AddPeopleChatGroupScreen();
        getFragmentManager().beginTransaction()
                .addToBackStack(AddPeopleChatGroupScreen.class.getSimpleName())
                .add(R.id.home_container, cs)
                .commitAllowingStateLoss();
    }

    @Override
    public void onItemClick(String id) {
        GroupChatDetailScreen cs = new GroupChatDetailScreen();
        Bundle bundle = new Bundle();
        bundle.putString("param1", id);
        cs.setArguments(bundle);
        getFragmentManager().beginTransaction()
                .addToBackStack(GroupChatDetailScreen.class.getSimpleName())
                .add(R.id.home_container, cs)
                .commitAllowingStateLoss();
    }

    @Override
    public void onItemLongClick(GroupDto groupDto) {
        EditGroupChatFragment editGroupChatFragment = new EditGroupChatFragment();
        Bundle bundle = new Bundle();
        bundle.putString(EditGroupChatFragment.GROUP_ID, groupDto.getgId());
        bundle.putString(EditGroupChatFragment.GROUP_NAME, groupDto.getgName());
        editGroupChatFragment.setArguments(bundle);
        requireActivity().getSupportFragmentManager().beginTransaction()
                .addToBackStack(EditGroupChatFragment.class.getSimpleName())
                .add(R.id.home_container, editGroupChatFragment)
                .commitAllowingStateLoss();
    }

}