package com.example.chattingarea;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.chattingarea.model.Contact;
import com.example.chattingarea.model.GroupDto;
import com.example.chattingarea.model.UserDto;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class RealtimeDatabaseUtils {
    public static final String TAG = RealtimeDatabaseUtils.class.getSimpleName();

    private final Context mContext;

    private final FirebaseDatabase mDatabase;
    private final DatabaseReference mContactsRef;
    private final DatabaseReference mUserRef;
    private final DatabaseReference mRoomRef;
    private final DatabaseReference mGroupRef;
    private final DatabaseReference mGroupChatRef;
    private final FirebaseAuth mFirebaseAuth;
    private final String mUId;

    private ValueEventListener mListenContact;

    @SuppressLint("StaticFieldLeak")
    public static RealtimeDatabaseUtils sInstance = null;

    public synchronized static RealtimeDatabaseUtils getInstance(@NonNull Context context) {
        if (sInstance == null) {
            sInstance = new RealtimeDatabaseUtils(context);
        }
        return sInstance;
    }

    private RealtimeDatabaseUtils(@NonNull Context context) {
        this.mContext = context;
        this.mDatabase = FirebaseDatabase.getInstance();
        this.mUserRef = mDatabase.getReference(Constant.USER_REF);
        this.mRoomRef = mDatabase.getReference(Constant.ROOM_REF);
        this.mGroupRef = mDatabase.getReference(Constant.GROUP_REF);
        this.mGroupChatRef = mDatabase.getReference(Constant.GROUP_CHAT_REF);
        this.mContactsRef = mDatabase.getReference(Constant.CONTACTS_REF);
        this.mFirebaseAuth = FirebaseAuth.getInstance();
        this.mUId = mFirebaseAuth.getUid();
    }

    public void getAllUser(@NonNull IGetAllUser callback) {
        mUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) return;
                HashMap<String, HashMap<String, UserDto>> selects =
                        (HashMap) dataSnapshot.getValue();
                List<UserDto> listAllUser = new ArrayList<>();
                for (Map.Entry<String, HashMap<String, UserDto>> entry : selects.entrySet()) {
                    listAllUser.add(getUserFromDb(entry.getValue()));
                }

                callback.onCompletedGetAllUser(
                        listAllUser.size() == 0 ? Constant.StatusRequest.NO_DATA
                                : Constant.StatusRequest.SUCCESS, listAllUser, "Success");
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Timber.e(error.getMessage());
                callback.onCompletedGetAllUser(
                        Constant.StatusRequest.FAIL, null, error.getMessage());
            }
        });
    }

    public void getAllContacts(@NonNull IGetAllContacts callback) {
        mContactsRef.get().addOnSuccessListener(dataSnapshot -> {
            List<Contact> allContacts = new ArrayList<>();
            for (DataSnapshot d : dataSnapshot.getChildren()) {
                Contact contact = d.getValue(Contact.class);
                if(Contact.isMyContact(mUId,contact, allContacts)){
                    allContacts.add(contact);
                }
            }

            callback.onCompletedGetAllContacts(
                    allContacts.size() == 0 ? Constant.StatusRequest.NO_DATA
                            : Constant.StatusRequest.SUCCESS, allContacts, mUId,"Success");
        }).addOnFailureListener(e -> {
            Timber.e(e);
            callback.onCompletedGetAllContacts(Constant.StatusRequest.FAIL, null, mUId, e.getMessage());
        });
    }

    public void sendRequestContact(@NonNull String uidToSend, ISendRequestContact callback) {
        mContactsRef.get().addOnSuccessListener(dataSnapshot -> {
            boolean isExist = false;
            String id = mContactsRef.push().getKey();
            if(id == null) {
                Timber.e("ID null");
                if (callback != null) {
                    callback.onCompletedSendRequestContact(
                            Constant.StatusRequest.FAIL, "ID null");
                }
                return;
            }
            Contact contact = Contact.createRequest(id,mUId, uidToSend);

            for (DataSnapshot d : dataSnapshot.getChildren()) {
                Contact c = d.getValue(Contact.class);
                if(c == null){
                    continue;
                }
                if(c.getAuth().equals(contact.getAuth()) &&
                        c.getDestination().equals(contact.getDestination())){
                    isExist = true;
                }
            }

            if(!isExist){
                mContactsRef.child(id).setValue(Contact.createRequest(id,mUId, uidToSend))
                        .addOnSuccessListener(unused -> {
                            if (callback != null) {
                                callback.onCompletedSendRequestContact(
                                        Constant.StatusRequest.SUCCESS, "Success");
                            }
                        })
                        .addOnFailureListener(e -> {
                            Timber.e(e);
                            if (callback != null) {
                                callback.onCompletedSendRequestContact(
                                        Constant.StatusRequest.FAIL, e.getMessage());
                            }
                        });
            }else{
                if (callback != null) {
                    callback.onCompletedSendRequestContact(Constant.StatusRequest.EXIST, "???? t???n t???i");
                }
            }


        }).addOnFailureListener(e -> {
            Timber.e(e);
            if (callback != null) {
                callback.onCompletedSendRequestContact(
                        Constant.StatusRequest.FAIL, e.getMessage());
            }
        });
    }

    public void updateContact(@NonNull Contact contact){
        Log.d("TAG", "updateContact: "+contact.getId());
        mContactsRef.child(contact.getId()).setValue(contact);
    }

    public void deleteContact(@NonNull Contact contact){
        mContactsRef.child(contact.getId()).removeValue();
    }


    public void listenContact(@NonNull IListenMyContact callback){
        mListenContact = mContactsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                callback.onDataChange();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void removeListenContact(){
        if(mListenContact != null){
            mContactsRef.removeEventListener(mListenContact);
            mListenContact = null;
        }
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

    public void deleteChat(String uidDestination){
        mRoomRef.child(mUId).child(uidDestination).removeValue();
        mRoomRef.child(uidDestination).child(mUId).removeValue();
    }

    public void getAllFriend(IGetAllFriend callback){
        getAllUser(new IGetAllUser() {
            @Override
            public void onCompletedGetAllUser(Constant.StatusRequest statusRequest,
                    List<UserDto> allUsers, String message) {
                getAllContacts(new IGetAllContacts() {
                    @Override
                    public void onCompletedGetAllContacts(Constant.StatusRequest statusRequest,
                            List<Contact> allContacts, String myUid, String message) {
                        List<UserDto> listFriend = new ArrayList<>();
                        for(Contact contact : allContacts){
                            if(contact.getStatus().equals(Constant.StatusContacts.FRIEND)){
                                if(myUid.equals(contact.getAuth())){
                                    listFriend.add(getUserById(contact.getDestination(),allUsers));
                                }else{
                                    listFriend.add(getUserById(contact.getAuth(), allUsers));
                                }
                            }
                        }

                        callback.onCompletedGetAllFriend(Constant.StatusRequest.SUCCESS, listFriend, "Success");
                    }
                });
            }
        });
    }

    public void getMemberOfGroup(String groupId, IGetMemberInGroup callback){
        getAllFriend(new IGetAllFriend() {
            @Override
            public void onCompletedGetAllFriend(Constant.StatusRequest statusRequest,
                    List<UserDto> allFriend, String message) {
                mGroupRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        ArrayList<UserDto> memberInGroup = new ArrayList<>();
                        ArrayList<UserDto> memberNotInGroup = new ArrayList<>();
                        Map<String, Map<String, Object>> map = (Map<String, Map<String, Object>>) snapshot.getValue();
                        for(Map.Entry<String, Map<String, Object>> entryUser : map.entrySet()){
                            for(Map.Entry<String, Object> entryChat : entryUser.getValue().entrySet()){
                                UserDto member = getUserById(entryUser.getKey(), allFriend);
                                if(member != null && !member.getId().equals(mUId)){
                                    if(entryChat.getKey().equals(groupId)){
                                        memberInGroup.add(member);
                                    }
                                }
                            }
                        }

                        for(UserDto userDto : allFriend){
                            boolean isAdd = true;
                            for(UserDto member : memberInGroup){
                                if(member.getId().equals(userDto.getId())){
                                    isAdd = false;
                                    break;
                                }
                            }

                            if(isAdd){
                                memberNotInGroup.add(userDto);
                            }
                        }

                        callback.onCompletedGetMemberInGroup(Constant.StatusRequest.SUCCESS,memberNotInGroup,memberInGroup,"Success");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onCompletedGetMemberInGroup(Constant.StatusRequest.FAIL,null,null,error.getMessage());
                    }
                });
            }
        });
    }

    public void deleteMember(String uid, String groupId){
        mGroupRef.child(uid).child(groupId).removeValue();
    }

    public void addMember(String uid, GroupDto groupDto){
        mGroupRef.child(uid).child(groupDto.getgId()).setValue(groupDto);
    }

    public UserDto getUserById(String id,  List<UserDto> allUsers){
        for(UserDto userDto : allUsers){
            if(userDto.getId().equals(id))
                return userDto;
        }
        return null;
    }


    public interface IGetAllFriend {
        void onCompletedGetAllFriend(Constant.StatusRequest statusRequest, List<UserDto> allFriend,
                String message);
    }

    public interface IGetMemberInGroup{
        void onCompletedGetMemberInGroup(Constant.StatusRequest statusRequest, List<UserDto> usersNotInGroup,List<UserDto> usersInGroup,
                String message);
    }

    public interface IListenMyContact{
        void onDataChange();
    }

    public interface IGetAllUser {
        void onCompletedGetAllUser(Constant.StatusRequest statusRequest, List<UserDto> allUsers,
                String message);
    }

    public interface ISendRequestContact {
        void onCompletedSendRequestContact(Constant.StatusRequest statusRequest, String message);
    }

    public interface IGetAllContacts {
        void onCompletedGetAllContacts(Constant.StatusRequest statusRequest,
                List<Contact> allContacts,
                String myUid,
                String message);
    }
}
