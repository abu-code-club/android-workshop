package com.codeclub.abu.chatcodeclub.activities;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.*;
import android.widget.TextView;

import com.codeclub.abu.chatcodeclub.R;
import com.codeclub.abu.chatcodeclub.models.User;
import com.firebase.ui.database.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView mUsersList;
    private DatabaseReference mUsersDb;
    private DatabaseReference mCrrUserDb;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);


        mUsersDb = FirebaseDatabase.getInstance().getReference().child("users");
        mUsersDb.keepSynced(true);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mCrrUserDb = mUsersDb.child(currentUserId);


        mUsersList = (RecyclerView) findViewById(R.id.users_list);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    protected void onStart() {
        super.onStart();

        mCrrUserDb.child("online").setValue(true);

        FirebaseRecyclerAdapter<User, UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<User, UsersViewHolder>(
                User.class,
                R.layout.users_single_layout,
                UsersViewHolder.class,
                mUsersDb
        ) {
            @Override
            protected void populateViewHolder(UsersViewHolder usersViewHolder, User users, int position) {
                final String userId = getRef(position).getKey();

                if (!userId.equals(currentUserId)) {
                    usersViewHolder.setInfo(users.getUsername(), users.getStatus());
                    usersViewHolder.setUserImage(users.getThumb_img(), getApplicationContext());
                    usersViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent profileIntent = new Intent(UsersActivity.this, ProfileActivity.class);
                            profileIntent.putExtra("userId", userId);
                            startActivity(profileIntent);
                        }
                    });
                } else {
                    usersViewHolder.mView.setVisibility(View.GONE);
                    usersViewHolder.mView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
                }
            }


        };

        mUsersList.setAdapter(firebaseRecyclerAdapter);

    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public UsersViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setInfo(String username, String status) {
            TextView usernameView = (TextView) mView.findViewById(R.id.user_single_username);
            usernameView.setText(username);
        }

        public void setUserImage(String thumb_image, Context context) {
            CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.user_single_image);
            Picasso.with(context).load(thumb_image).placeholder(R.drawable.user2).into(userImageView);

        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mCrrUserDb.child("online").setValue(ServerValue.TIMESTAMP);
    }
}