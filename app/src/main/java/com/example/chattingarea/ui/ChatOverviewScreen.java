package com.example.chattingarea.ui;

import android.os.Bundle;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.chattingarea.Constant;
import com.example.chattingarea.R;
import com.example.chattingarea.adapter.ChatOverviewAdapter;
import com.example.chattingarea.model.UserDto;
import com.example.chattingarea.model.UserChatOverview;
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

public class ChatOverviewScreen extends Fragment implements ChatOverviewAdapter.ClickListener {

    private View mRootView;
    private RecyclerView mRcv;
    private EditText mEdtSearch;
    private AppCompatImageView back;

    private FirebaseDatabase mDatabase;
    private DatabaseReference mUserRef;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    private DataFirebaseInterface dbInterface;


    private ArrayList<UserDto> listUserDto = new ArrayList();
    private ArrayList<UserChatOverview> listData = new ArrayList();
    private UserDto currentUserDto = new UserDto();
    private ChatOverviewAdapter chatOverviewAdapter;

    public ChatOverviewScreen() {
    }

    public static ChatOverviewScreen newInstance(String param1, String param2) {
        ChatOverviewScreen fragment = new ChatOverviewScreen();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_chat_overview_screen, container, false);
        initView();
        initAction();
        initData();
        return mRootView;
    }

    private void initView() {
        mRcv = mRootView.findViewById(R.id.chat_overview_rcv);
        mEdtSearch = mRootView.findViewById(R.id.chat_overview_edt_search);
        back = mRootView.findViewById(R.id.back);

        mDatabase = FirebaseDatabase.getInstance();
        mUserRef = mDatabase.getReference(Constant.USER_REF);
        mFirebaseAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        dbInterface = new DataFirebaseInterface() {
            @Override
            public void readDataSuccess(UserDto userDto) {
                currentUserDto = userDto;
            }

            @Override
            public void readListUser(ArrayList<UserDto> list) {
                chatOverviewAdapter.updateData(mapListUserChat(list));
            }
        };
        mRcv.setLayoutManager(new LinearLayoutManager(getContext()));
        chatOverviewAdapter = new ChatOverviewAdapter(getContext(), listData, this);
        mRcv.setAdapter(chatOverviewAdapter);
    }

    private void initAction() {
        back.setOnClickListener(view -> requireActivity().onBackPressed());
        mEdtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                searchFriend(mEdtSearch.getText().toString());
            }
        });
    }

    private void searchFriend(String text) {
    }

    private void initData() {
        readListFriend();
        readAllUser();
    }

    private void readListFriend() {
        mUserRef.child(mFirebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) return;
                HashMap value = (HashMap) dataSnapshot.getValue();
                dbInterface.readDataSuccess(getUserFromDb(value));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w("ProfileScreen", "Failed to read value.", error.toException());
            }
        });
    }

    private void readAllUser() {
        mUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) return;
                HashMap<String, HashMap<String, UserDto>> selects = (HashMap) dataSnapshot.getValue();
                ArrayList<UserDto> al = new ArrayList<>();
                for (Map.Entry<String, HashMap<String, UserDto>> entry : selects.entrySet()) {
                    al.add(getUserFromDb(entry.getValue()));
                }
                dbInterface.readListUser(al);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w("ProfileScreen", "Failed to read value.", error.toException());
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
        ChatDetailScreen cs = new ChatDetailScreen();
        Bundle bundle = new Bundle();
        bundle.putString("param1", id);
        cs.setArguments(bundle);
        getFragmentManager().beginTransaction()
                .addToBackStack(ChatDetailScreen.class.getSimpleName())
                .add(R.id.home_container, cs)
                .commitAllowingStateLoss();
    }

    public interface DataFirebaseInterface {
        void readDataSuccess(UserDto userDto);

        void readListUser(ArrayList<UserDto> al);
    }
}