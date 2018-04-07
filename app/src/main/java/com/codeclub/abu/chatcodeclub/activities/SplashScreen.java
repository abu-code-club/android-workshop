package com.codeclub.abu.chatcodeclub.activities;

import android.content.Intent;
import android.os.*;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.codeclub.abu.chatcodeclub.*;
import com.codeclub.abu.chatcodeclub.R;
import com.google.firebase.auth.*;
import com.google.firebase.database.*;

public class SplashScreen extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mUserDb;
    public static FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.codeclub.abu.chatcodeclub.R.layout.activity_splash_screen);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        final ImageView splash_screen = findViewById(R.id.img_splash_screen);
        splash_screen.postDelayed(new Runnable() {
            @Override
            public void run() {
//                splash_screen.setVisibility(View.GONE);
                goToLogin();
            }
        }, 3000);


    }

    private void goToLogin() {
        if (currentUser == null) {
            // login or sign up
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
        else {
            mAuth = FirebaseAuth.getInstance();
            mUserDb = FirebaseDatabase.getInstance().getReference()
                    .child("users").child(
                            currentUser.getUid());

            mUserDb.addValueEventListener(new ValueEventListener() {
                @Override

                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot != null)
                        mUserDb.child("online").onDisconnect().setValue(ServerValue.TIMESTAMP);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            startActivity(new Intent(this, Dashboard.class));
            finish();
        }
    }
}
