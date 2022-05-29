package com.example.chattingarea;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.chattingarea.model.Contact;
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
    private final FirebaseAuth mFirebaseAuth;
    private final String mUId;

    private ChildEventListener mListenContact;

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
                if(Contact.isMyContact(mUId,contact)){
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
        mContactsRef.child(mUId).setValue(Contact.createRequest(mUId, uidToSend))
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
    }

    public void updateContact(@NonNull Contact contact){
        mContactsRef.child(contact.getAuth()).setValue(contact);
    }

    public void deleteContact(@NonNull Contact contact){
        mContactsRef.child(contact.getAuth()).removeValue();
    }

    public void listenMyContact(@NonNull IListenMyContact callback){
        mListenContact = mContactsRef.child(mUId).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot,
                    @Nullable String previousChildName) {
                Contact contact = snapshot.getValue(Contact.class);
                if(Contact.isMyContact(mUId,contact)){
                    callback.onNewRequest(contact,"New request");
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot,
                    @Nullable String previousChildName) {
                Contact contact = snapshot.getValue(Contact.class);
                if(Contact.isMyContact(mUId,contact)){
                    callback.onChangeRequest(contact,"Contact change");
                }

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                Contact contact = snapshot.getValue(Contact.class);
                if(Contact.isMyContact(mUId,contact)){
                    callback.onDeletedRequest(contact,"Request deleted");
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot,
                    @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void removeListenMyContact(){
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

    public interface IListenMyContact{
        void onNewRequest(Contact contact, String message);
        void onDeletedRequest(Contact contact, String message);
        void onChangeRequest(Contact contact, String message);
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