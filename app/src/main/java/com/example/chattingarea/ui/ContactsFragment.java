package com.example.chattingarea.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.example.chattingarea.model.UserDto;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;


public class ContactsFragment extends Fragment implements RealtimeDatabaseUtils.IGetAllUser, RealtimeDatabaseUtils.IGetAllContacts {

    private int loadBaseData = 0;

    private List<UserDto> mListAllUser;
    private List<UserDto> mListFriend;
    private List<Contact> mListAllContact;
    private List<Contact> mListRequestFriend;
    private List<Contact> mListMyRequestFriend;

    private RequestAdapter mRequestAdapter;
    private MyRequestAdapter mMyRequestAdapter;
    private FindFriendAdapter mFindFriendAdapter;
    private MyFriendAdapter mMyFriendAdapter;

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
                                    Toast.makeText(view.getContext(), "Gửi kết bạn thành công!",
                                            Toast.LENGTH_SHORT).show();
                                }else if(statusRequest == Constant.StatusRequest.EXIST){
                                    Toast.makeText(view.getContext(), "Bạn đã gửi kết bạn cho " + userDto.getName() + " rồi!",
                                            Toast.LENGTH_SHORT).show();
                                }else{
                                    Toast.makeText(view.getContext(), "Gửi kết bạn thất bại!",
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
                Toast.makeText(requireContext(), "Không có dữ liệu!", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(requireContext(), "Vui lòng nhập tên!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Kiểm tra uid này có phải là bạn không
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
            Toast.makeText(getContext(), "Chưa có người dùng", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(getContext(), "Lỗi server!", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(requireContext(), "Đã hủy lời mời!", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(requireContext(), "Đã chấp nhận lời mời!", Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onDenyClick(UserDto userDto, Contact contact, int position) {
                        mRequestAdapter.removeItem(position);
                        mRealtimeDatabaseUtils.deleteContact(contact);
                        Toast.makeText(requireContext(), "Đã từ chối lời mời!", Toast.LENGTH_SHORT).show();
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