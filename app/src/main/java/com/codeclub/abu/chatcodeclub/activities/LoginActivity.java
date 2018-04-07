package com.codeclub.abu.chatcodeclub.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.*;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.*;

import com.codeclub.abu.chatcodeclub.R;
import com.google.android.gms.tasks.*;
import com.google.firebase.auth.*;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout usernameET, passwordET;
    private Button loginBtn, signupBtn;
    private ProgressDialog mLoginProgress;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.codeclub.abu.chatcodeclub.R.layout.activity_login);

        usernameET = findViewById(com.codeclub.abu.chatcodeclub.R.id.login_email);
        passwordET = findViewById(com.codeclub.abu.chatcodeclub.R.id.login_pass);

        loginBtn = findViewById(com.codeclub.abu.chatcodeclub.R.id.login_login_btn);
        signupBtn = findViewById(R.id.login_signup_btn);

        mLoginProgress = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });

        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToSignupActivity();
            }
        });

    }

    private void login() {
        String email = usernameET.getEditText().getText().toString();
        String pass = passwordET.getEditText().getText().toString();

        if (!TextUtils.isEmpty(email) || !TextUtils.isEmpty(pass)) {
            mLoginProgress.setTitle("Login");
            mLoginProgress.setMessage("Please wait..");
            mLoginProgress.setCanceledOnTouchOutside(false);
            mLoginProgress.show();
            mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        task.getResult().getUser().getUid();
                        mLoginProgress.dismiss();
                        Intent mainIntent = new Intent(LoginActivity.this, Dashboard.class);
                        //mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(mainIntent);
                        //finish();
                    }
                    else {
                        mLoginProgress.hide();
                        Toast.makeText(LoginActivity.this, "Cannot log in.. :(", Toast.LENGTH_LONG).show();
                    }
                    task.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("TAG", e.getMessage());
                        }
                    });
                }
            });
        }
    }

    private void goToSignupActivity() {
        startActivity(new Intent(this, SignUpActivity.class));
        finish();
    }
}
