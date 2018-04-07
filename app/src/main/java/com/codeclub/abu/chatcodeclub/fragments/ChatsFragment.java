package com.codeclub.abu.chatcodeclub.fragments;


import android.content.*;
import android.os.Bundle;
import android.support.annotation.*;
import android.support.v4.app.Fragment;
import android.support.v7.widget.*;
import android.util.Log;
import android.view.*;
import android.widget.*;

import com.codeclub.abu.chatcodeclub.R;
import com.codeclub.abu.chatcodeclub.activities.ChatActivity;
import com.codeclub.abu.chatcodeclub.models.Chat;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.codeclub.abu.chatcodeclub.activities.SplashScreen.currentUser;

public class ChatsFragment extends Fragment {

    RecyclerView mChatsRecyclerV;
    private View mMainView;
    private DatabaseReference mChatsDb;
    private DatabaseReference mUsersDb;
    private DatabaseReference mMessagesDb;
    private String mCrrUserId;


    public ChatsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        mCrrUserId = mAuth.getCurrentUser().getUid();

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mMainView = inflater.inflate(R.layout.fragment_chats, container, false);

        mChatsRecyclerV = mMainView.findViewById(R.id.chat_recyclerView);


        mChatsDb = FirebaseDatabase.getInstance().getReference().child("chats").child(mCrrUserId);
        mChatsDb.keepSynced(true);
        mUsersDb = FirebaseDatabase.getInstance().getReference().child("users");
        mUsersDb.keepSynced(true);
        mMessagesDb = FirebaseDatabase.getInstance().getReference().child("messages").child(mCrrUserId);
        mMessagesDb.keepSynced(true);

        mChatsRecyclerV.setHasFixedSize(true);
        mChatsRecyclerV.setLayoutManager(new LinearLayoutManager(getContext()));

        // Inflate the layout for this fragment
        return mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Chat, ChatViewHolder> adapter = new FirebaseRecyclerAdapter<Chat, ChatViewHolder>(
                Chat.class,
                R.layout.chat_single_layout,
                ChatViewHolder.class,
                mChatsDb
        ) {

            @Override
            protected void populateViewHolder(final ChatViewHolder viewHolder, Chat model, int position) {
                final String crrUserInList = getRef(position).getKey();

                mChatsDb.child(crrUserInList).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        mUsersDb.child(crrUserInList).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                final String username = dataSnapshot.child("username").getValue().toString();
                                String thumbImg = dataSnapshot.child("thumb_img").getValue().toString();
                                String online = dataSnapshot.child("online").getValue().toString();

                                viewHolder.setName(username);
                                viewHolder.setUserOnline(online);
                                viewHolder.setUserImage(thumbImg, getContext());

                                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent chatIntent = new Intent(getContext(), ChatActivity.class);
                                        chatIntent.putExtra("userId", crrUserInList);
                                        chatIntent.putExtra("username", username);
                                        startActivity(chatIntent);
                                    }
                                });

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        Query messageQuery = mMessagesDb.child(crrUserInList).limitToLast(1);
                        messageQuery.addChildEventListener(new ChildEventListener() {
                            @Override
                            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                String lastMessage = String.valueOf(dataSnapshot.child("message").getValue());
                                if (lastMessage.length() > 15)
                                    lastMessage = lastMessage.substring(0, 15) + "...";
                                viewHolder.setLastMessage(lastMessage);
                            }

                            @Override
                            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                            }


                            @Override
                            public void onChildRemoved(DataSnapshot dataSnapshot) {

                            }

                            @Override
                            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }

                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        };

        mChatsRecyclerV.setAdapter(adapter);

    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public ChatViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }


        public void setName(String name) {
            TextView userNameView = (TextView) mView.findViewById(R.id.chat_single_username);
            userNameView.setText(name);
        }

        public void setLastMessage(String msg) {
            TextView lastMessage = (TextView) mView.findViewById(R.id.chat_single_last_msg);
            lastMessage.setText(msg);
        }

        public void setUserImage(String thumb_image, Context context) {
            CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.chat_single_image);
            Picasso.with(context).load(thumb_image).placeholder(R.drawable.user2).into(userImageView);
        }

        public void setUserOnline(String online_status) {
            ImageView onlineBtn = (ImageView) mView.findViewById(R.id.chat_single_online);
            if (online_status.equals("true"))
                onlineBtn.setVisibility(View.VISIBLE);
            else
                onlineBtn.setVisibility(View.INVISIBLE);
        }
    }

}
