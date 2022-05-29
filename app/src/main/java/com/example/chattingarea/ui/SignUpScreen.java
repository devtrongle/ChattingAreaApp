package com.example.chattingarea.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chattingarea.R;
import com.example.chattingarea.Utils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class SignUpScreen extends AppCompatActivity {

    private final String TAG = SignUpScreen.class.getSimpleName();
    public static final String EXTRA_DATA_EMAIL = "EXTRA_DATA_EMAIL";
    public static final String EXTRA_DATA_PASSWORD = "EXTRA_DATA_PASSWORD";

    private TextInputLayout mTilEmail;
    private TextInputLayout mTilPassword;
    private TextInputLayout mTilConfirmPassword;
    private ConstraintLayout mWrapper;
    private Button mBtnSignUp;
    private FirebaseAuth mAuth;
    private TextInputEditText edtEmail;
    private TextInputEditText edtPassword;
    private TextInputEditText edtConfirmPassword;
    private TextView tvLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_screen);
        initView();
        initAction();
    }

    private void initView() {
        mAuth = FirebaseAuth.getInstance();
        mTilEmail = findViewById(R.id.sign_up_til_email);
        mTilPassword = findViewById(R.id.sign_up_til_password);
        mTilConfirmPassword = findViewById(R.id.sign_up_til_confirm_password);
        mBtnSignUp = findViewById(R.id.sign_up_btn_confirm);
        mWrapper = findViewById(R.id.sing_up_wrapper);
        edtEmail = findViewById(R.id.sign_up_edt_email);
        edtPassword = findViewById(R.id.sign_up_edt_password);
        edtConfirmPassword = findViewById(R.id.sign_up_edt_confirm_password);
        tvLogin = findViewById(R.id.sign_up_tv_right);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initAction() {
        mWrapper.setOnTouchListener((view, motionEvent) -> {
            mTilEmail.clearFocus();
            mTilPassword.clearFocus();
            mTilConfirmPassword.clearFocus();
            Utils.hideKeyBoard(mWrapper);
            return true;
        });
        mBtnSignUp.setOnClickListener(view -> {
            String email = Objects.requireNonNull(mTilEmail.getEditText()).getText().toString();
            String password = Objects.requireNonNull(mTilPassword.getEditText()).getText().toString();
            mTilEmail.clearFocus();
            mTilPassword.clearFocus();
            mTilConfirmPassword.clearFocus();
            if (isValid()) {
                signUp(email, password);
            } else {
                Toast.makeText(SignUpScreen.this, "Invalid email or password!", Toast.LENGTH_SHORT).show();
            }
        });

        // valid email
        edtEmail.setOnFocusChangeListener((view, b) -> {
            if (b) {
                mTilEmail.setError(null);
            } else {
                if (TextUtils.isEmpty(edtEmail.getText())) {
                    mTilEmail.setError(null);
                } else if (!Utils.isValidEmail(edtEmail.getText())) {
                    mTilEmail.setError("Invalid Email !");
                }
            }
        });

        // valid password
        edtPassword.setOnFocusChangeListener((view, b) -> {
            if (b) {
                mTilPassword.setError(null);
            } else {
                if (TextUtils.isEmpty(edtPassword.getText())) {
                    mTilPassword.setError("Password must not be null !");
                }
            }
        });

        // valid Confirm password
        edtConfirmPassword.setOnFocusChangeListener((view, b) -> {
            if (b) {
                mTilConfirmPassword.setError(null);
            } else {
                if (TextUtils.isEmpty(edtPassword.getText()) ||
                        !edtConfirmPassword.getText().toString().equals(edtPassword.getText().toString())) {
                    mTilConfirmPassword.setError("Password not match !");
                }
            }
        });

        tvLogin.setOnClickListener(view -> onBackPressed());
    }

    private boolean isValid() {
        boolean isNotError = TextUtils.isEmpty(mTilEmail.getError())
                && TextUtils.isEmpty(mTilPassword.getError())
                && TextUtils.isEmpty(mTilConfirmPassword.getError());

        boolean isNotEmpty = !TextUtils.isEmpty(mTilEmail.getEditText().getText())
                && !TextUtils.isEmpty(mTilPassword.getEditText().getText())
                && !TextUtils.isEmpty(mTilConfirmPassword.getEditText().getText());

        return isNotError && isNotEmpty;
    }

    private void signUp(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        final Intent data = new Intent();
                        data.putExtra(EXTRA_DATA_EMAIL, edtEmail.getText().toString());
                        data.putExtra(EXTRA_DATA_PASSWORD, edtPassword.getText().toString());
                        setResult(Activity.RESULT_OK, data);
                        finish();
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        Toast.makeText(SignUpScreen.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(Activity.RESULT_CANCELED);
    }
}