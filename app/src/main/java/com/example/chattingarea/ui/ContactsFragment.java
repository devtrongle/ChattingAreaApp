package com.example.chattingarea.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.chattingarea.Constant;
import com.example.chattingarea.R;
import com.example.chattingarea.RealtimeDatabaseUtils;
import com.example.chattingarea.adapter.RequestAdapter;
import com.example.chattingarea.model.Contact;
import com.example.chattingarea.model.UserDto;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ContactsFragment extends Fragment implements RealtimeDatabaseUtils.IGetAllUser, RealtimeDatabaseUtils.IGetAllContacts {

    private int loadBaseData = 0;

    private List<UserDto> mListAllUser;
    private List<UserDto> mListFriend;
    private List<Contact> mListContact;

    private RequestAdapter mRequestAdapter;

    private List<UserDto> mListFilter;

    private EditText edtName;

    private ListView lvFilter;
    private ListView lvFriend;
    private RecyclerView rvRequest;


    private RealtimeDatabaseUtils mRealtimeDatabaseUtils;

    private String[] friendNames;

    private String[] friendFilter;

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
        mListContact = new ArrayList<>();
        mRealtimeDatabaseUtils.getAllUser(this);
        mRealtimeDatabaseUtils.getAllContacts(this);

        view.findViewById(R.id.back).setOnClickListener(v -> requireActivity().onBackPressed());

        view.findViewById(R.id.search_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = edtName.getText().toString();
                if(!name.equals("")){
                    mListFilter = new ArrayList<>();
                    for(UserDto u : mListAllUser){
                        if(u.getName().toLowerCase().contains(name.toLowerCase()) && !u.getId().equals(
                                FirebaseAuth.getInstance().getUid())){
                            mListFilter.add(u);
                        }
                    }
                    if(mListFilter.size() > 0){
                        setupListFilter();
                    }else{
                        Toast.makeText(requireContext(), "Không có dữ liệu!", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(requireContext(), "Vui lòng nhập tên!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setupListFilter(){
        friendFilter = new String[mListFilter.size()];
        for(int i = 0; i < mListFilter.size(); i++){
            friendFilter[i] = mListFilter.get(i).getName();
        }
        BaseAdapter adapter = new ArrayAdapter<String>(requireContext(),android.R.layout.simple_list_item_1 , friendFilter);
        lvFilter.setAdapter(adapter);
    }

    private void setupListFriend(){
        friendNames = new String[mListFriend.size()];
        for(int i = 0; i < mListFriend.size(); i++){
            friendNames[i] = mListFriend.get(i).getName();
        }
        BaseAdapter adapter = new ArrayAdapter<String>(requireContext(),android.R.layout.simple_list_item_1 , friendNames);
        lvFriend.setAdapter(adapter);
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
        mListContact = allContacts;
        for(Contact contact : allContacts){
            if(contact.getStatus().equals(Constant.StatusContacts.FRIEND)){
                if(myUid.equals(contact.getAuth())){
                    mListFriend.add(getUserByUId(contact.getDestination()));
                }else{
                    mListFriend.add(getUserByUId(contact.getAuth()));
                }
            }
        }
        if(mListFriend.size() > 0){
            setupListFriend();
        }

        if(loadBaseData == 2){
            setupRequestFriend();
        }
    }

    private void setupRequestFriend(){
        mRequestAdapter = new RequestAdapter(requireContext(), mListContact, mListAllUser,
                new RequestAdapter.ClickListener() {
                    @Override
                    public void onAcceptClick(UserDto userDto, Contact contact) {
                        contact.setStatus(Constant.StatusContacts.FRIEND);
                        mRealtimeDatabaseUtils.updateContact(contact);
                    }

                    @Override
                    public void onDenyClick(UserDto userDto, Contact contact) {
                        contact.setStatus(Constant.StatusContacts.DENY);
                        mRealtimeDatabaseUtils.updateContact(contact);
                    }
                });
        rvRequest.setAdapter(mRequestAdapter);
    }
}