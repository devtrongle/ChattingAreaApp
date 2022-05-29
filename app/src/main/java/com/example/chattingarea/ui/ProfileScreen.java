package com.example.chattingarea.ui;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chattingarea.Constant;
import com.example.chattingarea.MainActivity;
import com.example.chattingarea.R;
import com.example.chattingarea.Utils;
import com.example.chattingarea.model.UserDto;
import com.github.siyamed.shapeimageview.CircularImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ProfileScreen extends Fragment {

    private static final int OPEN_DOCUMENT_CODE = 2;
    private View mRootView;
    private TextInputLayout mTilName;
    private TextInputLayout mTilAge;
    private TextInputLayout mTilPhone;
    private TextInputLayout mTilAddress;
    private TextInputEditText edtName;
    private CircularImageView mCiAva;
    private RadioButton mRdMale;
    private RadioButton mRdFemale;
    private Button mBtnUpdate;
    private LinearLayout mIvWrapper;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mUserRef;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private String urlAva = null;

    public ProfileScreen() {
    }

    public static ProfileScreen newInstance() {
        ProfileScreen fragment = new ProfileScreen();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_profile, container, false);
        initView();
        initAction();
        initData();
        return mRootView;
    }

    private void initView() {
        mTilName = mRootView.findViewById(R.id.profile_til_name);
        mTilAge = mRootView.findViewById(R.id.profile_til_age);
        mTilPhone = mRootView.findViewById(R.id.profile_til_phone_number);
        mTilAddress = mRootView.findViewById(R.id.profile_til_address);
        mBtnUpdate = mRootView.findViewById(R.id.profile_btn_update);
        mCiAva = mRootView.findViewById(R.id.profile_iv_ava);
        mRdMale = mRootView.findViewById(R.id.profile_rd_male);
        mRdFemale = mRootView.findViewById(R.id.profile_rd_female);
        edtName = mRootView.findViewById(R.id.profile_edt_name);
        mIvWrapper = mRootView.findViewById(R.id.profile_iv_ava_container);

        mDatabase = FirebaseDatabase.getInstance();
        mUserRef = mDatabase.getReference(Constant.USER_REF);
        mFirebaseAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initAction() {
        LinearLayoutCompat container = mRootView.findViewById(R.id.profile_container);
        container.setOnTouchListener((view, motionEvent) -> {
            Utils.hideKeyBoard(container);
            container.requestFocus();
            return true;
        });

        mBtnUpdate.setOnClickListener(view -> {
            if (!isValidName()) {
                return;
            } else {
                UserDto userDto = new UserDto(
                        mFirebaseAuth.getUid(),
                        mTilName.getEditText().getText().toString(),
                        mTilAge.getEditText().getText().toString(),
                        mRdMale.isChecked(),
                        mTilPhone.getEditText().getText().toString(),
                        mTilAddress.getEditText().getText().toString(),
                        urlAva);
                updateUser(userDto);
            }
        });
        edtName.setOnFocusChangeListener((view, b) -> {
            if (b) {
                mTilName.setError(null);
            } else {
                if (TextUtils.isEmpty(edtName.getText())) {
                    mTilName.setError("Name must not be null !");
                }
            }
        });
        mIvWrapper.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(intent, OPEN_DOCUMENT_CODE);
        });
    }

    private void initData() {
        readProfile();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == OPEN_DOCUMENT_CODE && resultCode == RESULT_OK) {
            if (resultData != null) {
                Uri imageUri = resultData.getData();
                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), imageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Bitmap finalBitmap = bitmap;
                mCiAva.post(() -> {
                    mCiAva.setImageBitmap(finalBitmap);
                    uploadFile(finalBitmap);
                });
            }
        }
    }

    public void uploadFile(Bitmap bitmap) {
        if (bitmap != null) {
            StorageReference imgRef = storageReference.child(Constant.USER_REF).child(mFirebaseAuth.getUid());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();
            imgRef.putBytes(data).addOnSuccessListener(snapshot -> {
                snapshot.getStorage().getDownloadUrl().addOnCompleteListener(
                        task -> {
                            if (task.isSuccessful()) {
                                urlAva = task.getResult().toString();
                            }
                        });
                Log.d("ProfileScreen", "upload img success!");
            }).addOnFailureListener(exception -> {
                urlAva = null;
                Log.d("ProfileScreen", "upload img Fail!");
            });
        }
    }

    private void readProfile() {
        mUserRef.child(mFirebaseAuth.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserDto value = dataSnapshot.getValue(UserDto.class);
                setDataProfile(value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w("ProfileScreen", "Failed to read value.", error.toException());
            }
        });
    }

    private void setDataProfile(UserDto userDto) {
        if (userDto == null) {
            return;
        }
        mTilName.getEditText().setText(userDto.getName());
        mTilAge.getEditText().setText(userDto.getAge());
        if (userDto.isGender()) {
            mRdMale.setChecked(true);
            mRdFemale.setChecked(false);
        } else {
            mRdMale.setChecked(false);
            mRdFemale.setChecked(true);
        }
        mTilPhone.getEditText().setText(userDto.getPhoneNumber());
        mTilAddress.getEditText().setText(userDto.getAddress());

        storageReference.child(Constant.USER_REF)
                .child(mFirebaseAuth.getUid())
                .getDownloadUrl()
                .addOnSuccessListener(uri -> Glide.with(getContext())
                        .load(uri)
                        .placeholder(R.drawable.img)
                        .into(mCiAva))
                .addOnFailureListener(exception -> Log.w("ProfileScreen", "Failed to load ava"));
    }

    private void updateUser(UserDto userDto) {
        mUserRef.child(userDto.getId()).setValue(userDto).addOnCompleteListener(task -> {
            ((MainActivity) getActivity()).openHomeScreen();
            Log.d("ProfileScreen", "Update userDto success!");
        }).addOnFailureListener(e -> Log.d("ProfileScreen", "Update userDto fail!"));
    }

    private boolean isValidName() {
        if (TextUtils.isEmpty(mTilName.getEditText().getText().toString())) {
            mTilName.setError("Name must not be null !");
            Toast.makeText(mRootView.getContext(), "Please update your Profile first! ", Toast.LENGTH_SHORT).show();
            return false;
        }
        mTilName.setError(null);
        return true;
    }

}