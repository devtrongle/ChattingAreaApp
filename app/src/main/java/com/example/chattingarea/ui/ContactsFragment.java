package com.example.chattingarea.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.chattingarea.Constant;
import com.example.chattingarea.R;
import com.example.chattingarea.RealtimeDatabaseUtils;
import com.example.chattingarea.adapter.FindFriendAdapter;
import com.example.chattingarea.adapter.RequestAdapter;
import com.example.chattingarea.model.Contact;
import com.example.chattingarea.model.UserDto;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;


public class ContactsFragment extends Fragment implements RealtimeDatabaseUtils.IGetAllUser, RealtimeDatabaseUtils.IGetAllContacts {

    private int loadBaseData = 0;

    private List<UserDto> mListAllUser;
    private List<UserDto> mListFriend;
    private List<Contact> mListAllContact;
    private List<Contact> mListRequestFriend;

    private RequestAdapter mRequestAdapter;
    private FindFriendAdapter mFindFriendAdapter;
    private FindFriendAdapter mMyFriendAdapter;

    private List<UserDto> mListFilter;

    private EditText edtName;

    private RecyclerView lvFilter;
    private RecyclerView lvFriend;
    private RecyclerView rvRequest;


    private RealtimeDatabaseUtils mRealtimeDatabaseUtils;

    private String[] friendNames;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_contacts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        edtName = view.findViewById(R.id.edtName);
        lvFilter = view.findViewById(R.id.list_name_filter);
        lvFriend = view.findViewById(R.id.list_friend);
        rvRequest = view.findViewById(R.id.list_request);

        mRealtimeDatabaseUtils = RealtimeDatabaseUtils.getInstance(requireContext());
        mListAllUser = new ArrayList<>();
        mListFriend = new ArrayList<>();
        mListAllContact = new ArrayList<>();
        mListRequestFriend = new ArrayList<>();
        mRealtimeDatabaseUtils.getAllUser(this);
        mRealtimeDatabaseUtils.getAllContacts(this);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(requireContext(),
                DividerItemDecoration.VERTICAL);

        mFindFriendAdapter = new FindFriendAdapter(true);
        lvFilter.addItemDecoration(dividerItemDecoration);
        lvFilter.setAdapter(mFindFriendAdapter);

        mMyFriendAdapter = new FindFriendAdapter( false);
        lvFriend.addItemDecoration(dividerItemDecoration);
        lvFriend.setAdapter(mMyFriendAdapter);

        view.findViewById(R.id.back).setOnClickListener(v -> requireActivity().onBackPressed());

        view.findViewById(R.id.search_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
        });
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
        loadBaseData++;
        if(loadBaseData == 2){

        }
        if(statusRequest == Constant.StatusRequest.SUCCESS){
            mListAllUser = allUsers;
            friendNames = new String[mListAllUser.size()];
            for(int i = 0; i < mListAllUser.size(); i++){
                friendNames[i] = mListAllUser.get(i).getName();
            }
        }else if(statusRequest == Constant.StatusRequest.NO_DATA){
            mListAllUser = new ArrayList<>();
            friendNames = new String[]{};
            Toast.makeText(getContext(), "Chưa có người dùng", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(getContext(), "Lỗi server!", Toast.LENGTH_SHORT).show();
            requireActivity().onBackPressed();
        }

        if(loadBaseData == 2){
            setupRequestFriend();
        }
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
        loadBaseData++;
        mListAllContact = allContacts;
        for(Contact contact : allContacts){
            if(contact.getStatus().equals(Constant.StatusContacts.FRIEND)){
                if(myUid.equals(contact.getAuth())){
                    mListFriend.add(getUserByUId(contact.getDestination()));
                }else{
                    mListFriend.add(getUserByUId(contact.getAuth()));
                }
            }else if(contact.getStatus().equals(Constant.StatusContacts.REQUEST)){
                mListRequestFriend.add(contact);
            }
        }
        if(mListFriend.size() > 0){
            mMyFriendAdapter.submitList(mListFriend);
        }

        if(loadBaseData == 2){
            setupRequestFriend();
        }
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
                        mMyFriendAdapter.submitList(mListFriend);
                        Toast.makeText(requireContext(), "Đã chấp nhận lời mời!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onDenyClick(UserDto userDto, Contact contact, int position) {
                        contact.setStatus(Constant.StatusContacts.DENY);
                        mRealtimeDatabaseUtils.updateContact(contact);
                        mRequestAdapter.removeItem(position);
                        Toast.makeText(requireContext(), "Đã từ chối lời mời!", Toast.LENGTH_SHORT).show();
                    }
                });
        rvRequest.setAdapter(mRequestAdapter);
    }
}