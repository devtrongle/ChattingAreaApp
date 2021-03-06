package com.example.chattingarea.ui;

import static com.example.chattingarea.ui.ChatDetailScreen.REQUEST_ID;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.example.chattingarea.Constant;
import com.example.chattingarea.R;
import com.example.chattingarea.RealtimeDatabaseUtils;
import com.example.chattingarea.adapter.FindFriendAdapter;
import com.example.chattingarea.adapter.MyFriendAdapter;
import com.example.chattingarea.adapter.MyRequestAdapter;
import com.example.chattingarea.adapter.RequestAdapter;
import com.example.chattingarea.model.Contact;
import com.example.chattingarea.model.MessageDetailDto;
import com.example.chattingarea.model.UserDto;
import com.example.chattingarea.service.NotificationRequestBroadcast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import timber.log.Timber;


public class ContactsFragment extends Fragment implements RealtimeDatabaseUtils.IGetAllUser, RealtimeDatabaseUtils.IGetAllContacts {


    private int loadBaseData = 0;

    private List<UserDto> mListAllUser;
    private List<UserDto> mListFriend;
    private List<Contact> mListAllContact;
    private List<Contact> mListRequestFriend;
    private List<Contact> mListMyRequestFriend;
    private FirebaseDatabase mDatabase;
    private RequestAdapter mRequestAdapter;
    private MyRequestAdapter mMyRequestAdapter;
    private FindFriendAdapter mFindFriendAdapter;
    private MyFriendAdapter mMyFriendAdapter;
    private DatabaseReference mUserRef;

    private List<UserDto> mListFilter;

    private EditText edtName;

    private RecyclerView rvFilter;
    private RecyclerView rvMyFriend;
    private RecyclerView rvRequest;
    private RecyclerView rvMyRequest;


    private RealtimeDatabaseUtils mRealtimeDatabaseUtils;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_contacts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        edtName = view.findViewById(R.id.edtName);
        rvFilter = view.findViewById(R.id.list_filter);
        rvMyFriend = view.findViewById(R.id.list_friend);
        rvRequest = view.findViewById(R.id.list_request);
        rvMyRequest = view.findViewById(R.id.list_my_request);

        mDatabase = FirebaseDatabase.getInstance();
        mUserRef = mDatabase.getReference(Constant.USER_REF);
        mRealtimeDatabaseUtils = RealtimeDatabaseUtils.getInstance(requireContext());
        mListAllUser = new ArrayList<>();
        mListFriend = new ArrayList<>();
        mListAllContact = new ArrayList<>();
        mListRequestFriend = new ArrayList<>();
        mListMyRequestFriend = new ArrayList<>();
        mRealtimeDatabaseUtils.getAllUser(this);


        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(requireContext(),
                DividerItemDecoration.VERTICAL);

        mFindFriendAdapter = new FindFriendAdapter();
        rvFilter.addItemDecoration(dividerItemDecoration);
        rvFilter.setAdapter(mFindFriendAdapter);

        mRealtimeDatabaseUtils.listenContact(new RealtimeDatabaseUtils.IListenMyContact() {
            @Override
            public void onDataChange() {
                refreshData();
            }
        });

        mFindFriendAdapter.setOnClickListener(new FindFriendAdapter.ClickListener() {
            @Override
            public void onSendRequestClick(UserDto userDto, int position) {
                mRealtimeDatabaseUtils.sendRequestContact(
                        userDto.getId(), new RealtimeDatabaseUtils.ISendRequestContact() {
                            @Override
                            public void onCompletedSendRequestContact(
                                    Constant.StatusRequest statusRequest, String message) {
                                if(statusRequest == Constant.StatusRequest.SUCCESS){
                                    Toast.makeText(view.getContext(), "G???i k???t b???n th??nh c??ng!",
                                            Toast.LENGTH_SHORT).show();
                                }else if(statusRequest == Constant.StatusRequest.EXIST){
                                    Toast.makeText(view.getContext(), "B???n ???? g???i k???t b???n cho " + userDto.getName() + " r???i!",
                                            Toast.LENGTH_SHORT).show();
                                }else{
                                    Toast.makeText(view.getContext(), "G???i k???t b???n th???t b???i!",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        mMyFriendAdapter = new MyFriendAdapter();
        rvMyFriend.addItemDecoration(dividerItemDecoration);
        rvMyFriend.setAdapter(mMyFriendAdapter);

        view.findViewById(R.id.back).setOnClickListener(v -> requireActivity().onBackPressed());

        view.findViewById(R.id.search_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findUser();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRealtimeDatabaseUtils.removeListenContact();
    }

    private void findUser(){
        String name = edtName.getText().toString();
        if(!name.equals("")){
            mListFilter = new ArrayList<>();
            for(UserDto u : mListAllUser){
                if(u.getName().toLowerCase().contains(name.toLowerCase()) &&
                        !u.getId().equals(FirebaseAuth.getInstance().getUid()) &&
                        !isFriend(u.getId())){
                    mListFilter.add(u);
                }
            }
            if(mListFilter.size() > 0){
                mFindFriendAdapter.submitList(mListFilter);
            }else{
                Toast.makeText(requireContext(), "Kh??ng c?? d??? li???u!", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(requireContext(), "Vui l??ng nh???p t??n!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Ki???m tra uid n??y c?? ph???i l?? b???n kh??ng
     * @param uid
     * @return
     */
    private boolean isFriend(String uid){
        for(Contact c : mListAllContact){
            if(c.getDestination().equals(uid) && c.getStatus().equals(Constant.StatusContacts.FRIEND)){
                return true;
            }

            if(c.getAuth().equals(uid) && c.getStatus().equals(Constant.StatusContacts.FRIEND)){
                return  true;
            }
        }

        return false;
    }

    @Override
    public void onCompletedGetAllUser(Constant.StatusRequest statusRequest, List<UserDto> allUsers,
            String message) {
        mListAllUser = new ArrayList<>();

        if(statusRequest == Constant.StatusRequest.SUCCESS){
            mListAllUser = allUsers;
        }else if(statusRequest == Constant.StatusRequest.NO_DATA){
            Toast.makeText(getContext(), "Ch??a c?? ng?????i d??ng", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(getContext(), "L???i server!", Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
        }

        mRealtimeDatabaseUtils.getAllContacts(this);
    }

    public UserDto getUserByUId(String uid){
        for (UserDto u :
                mListAllUser) {
            if (u.getId().equals(uid)) return u;
        }
        return null;
    }

    @Override
    public void onCompletedGetAllContacts(Constant.StatusRequest statusRequest,
            List<Contact> allContacts,String myUid, String message) {

        mListAllContact = allContacts;
        mListFriend = new ArrayList<>();
        mListMyRequestFriend.clear();
        mListRequestFriend.clear();

        for(Contact contact : mListAllContact){
            if(contact.getStatus().equals(Constant.StatusContacts.FRIEND)){
                if(myUid.equals(contact.getAuth())){
                    mListFriend.add(getUserByUId(contact.getDestination()));
                }else{
                    mListFriend.add(getUserByUId(contact.getAuth()));
                }
            }

            if(contact.getStatus().equals(Constant.StatusContacts.REQUEST)){
                if(contact.getDestination().equals(myUid)){
                    addContactToList(contact, mListRequestFriend);
                }else{
                    addContactToList(contact, mListMyRequestFriend);
                }
            }
        }

        if(mListFriend.size() > 0){
            mMyFriendAdapter.submitList(mListFriend);
        }

        setupMyRequestFriend();
        setupRequestFriend();
    }

    private void addContactToList(Contact contact, List<Contact> contactList){
        for(Contact c : contactList){
            if(c.getId().equals(contact.getId()))
                return;
        }
        contactList.add(contact);
    }

    private void setupMyRequestFriend(){
        mMyRequestAdapter = new MyRequestAdapter(requireContext(), mListMyRequestFriend,
                mListAllUser, new MyRequestAdapter.ClickListener() {
            @Override
            public void onCancelClick(UserDto userDto, Contact contact, int position) {
                contact.setStatus(Constant.StatusContacts.DENY);
                mRealtimeDatabaseUtils.deleteContact(contact);
                mMyRequestAdapter.removeItem(position);
                Toast.makeText(requireContext(), "???? h???y l???i m???i!", Toast.LENGTH_SHORT).show();
            }
        });

        rvMyRequest.setAdapter(mMyRequestAdapter);
    }

    private void setupRequestFriend(){

        mRequestAdapter = new RequestAdapter(requireContext(), mListRequestFriend, mListAllUser,
                new RequestAdapter.ClickListener() {
                    @Override
                    public void onAcceptClick(UserDto userDto, Contact contact, int position) {
                        contact.setStatus(Constant.StatusContacts.FRIEND);
                        mRealtimeDatabaseUtils.updateContact(contact);
                        mRequestAdapter.removeItem(position);
                        mListFriend.add(userDto);
                        Toast.makeText(requireContext(), "???? ch???p nh???n l???i m???i!", Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onDenyClick(UserDto userDto, Contact contact, int position) {
                        mRequestAdapter.removeItem(position);
                        mRealtimeDatabaseUtils.deleteContact(contact);
                        Toast.makeText(requireContext(), "???? t??? ch???i l???i m???i!", Toast.LENGTH_SHORT).show();
                    }
                });
        rvRequest.setAdapter(mRequestAdapter);
    }

    private void refreshData(){
        new Handler(Looper.getMainLooper())
                .postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mRealtimeDatabaseUtils.getAllUser(ContactsFragment.this);
                    }
                },500);
    }
}