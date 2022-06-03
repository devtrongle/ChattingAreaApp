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

import com.example.chattingarea.MainActivity;
import com.example.chattingarea.R;
import com.example.chattingarea.Utils;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class LoginScreen extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextInputLayout mTilEmail;
    private TextInputLayout mTilPassword;
    private ConstraintLayout mWrapper;
    private Button mBtnLogin;
    private TextView mTvSignUp;
    private TextInputEditText edtEmail;
    private TextInputEditText edtPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);
        initView();
        initAction();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void initView() {
        mAuth = FirebaseAuth.getInstance();
        mBtnLogin = findViewById(R.id.login_btn_login);
        mTilEmail = findViewById(R.id.login_til_email);
        mTilPassword = findViewById(R.id.login_til_password);
        mWrapper = findViewById(R.id.login_wrapper);
        mTvSignUp = findViewById(R.id.login_tv_right);
        edtEmail = findViewById(R.id.login_edt_email);
        edtPassword = findViewById(R.id.login_edt_password);
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            openMainActivity();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initAction() {
        mBtnLogin.setOnClickListener(view -> {
            String email = Objects.requireNonNull(mTilEmail.getEditText()).getText().toString();
            String password = Objects.requireNonNull(mTilPassword.getEditText()).getText().toString();
            mTilEmail.clearFocus();
            mTilPassword.clearFocus();
            if (isValid()) {
                signIn(email, password);
            } else {
                Toast.makeText(LoginScreen.this, "Invalid email or password!", Toast.LENGTH_SHORT).show();
            }
        });
        mWrapper.setOnTouchListener((view, motionEvent) -> {
            mTilEmail.clearFocus();
            mTilPassword.clearFocus();
            Utils.hideKeyBoard(mWrapper);
            return true;
        });
        mTvSignUp.setOnClickListener(view -> openSignUpActivity());

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

        edtPassword.setOnFocusChangeListener((view, b) -> {
            if (b) {
                mTilPassword.setError(null);
            } else {
                if (TextUtils.isEmpty(edtPassword.getText())) {
                    mTilPassword.setError("Password must not be null !");
                }
            }
        });
    }

    private boolean isValid() {
        boolean isNotError = TextUtils.isEmpty(mTilEmail.getError())
                && TextUtils.isEmpty(mTilPassword.getError());

        boolean isNotEmpty = !TextUtils.isEmpty(mTilEmail.getEditText().getText())
                && !TextUtils.isEmpty(mTilPassword.getEditText().getText());

        return isNotError && isNotEmpty;
    }

    private void signIn(String email, String password) {
        if (!Utils.isValidEmail(email)) {
            mTilEmail.setError("Invalid Email !");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            mTilPassword.setError("Password must not be null !");
            return;
        }
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        openMainActivity();
                        task.getResult();
                        getSharedPreferences("_", MODE_PRIVATE).edit().putString(
                                "uID",
                                task.getResult().getUser().getUid()).apply();

                    } else {
                        Toast.makeText(LoginScreen.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void openMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void openSignUpActivity() {
        Intent intent = new Intent(this, SignUpScreen.class);
        startActivityForResult(intent, 9001);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == 9001) {
            final String email = data.getStringExtra(SignUpScreen.EXTRA_DATA_EMAIL);
            final String password = data.getStringExtra(SignUpScreen.EXTRA_DATA_PASSWORD);
            edtEmail.setText(email);
            edtPassword.setText(password);
        }
    }
}