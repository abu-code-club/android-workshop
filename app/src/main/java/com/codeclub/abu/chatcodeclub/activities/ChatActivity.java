package com.codeclub.abu.chatcodeclub.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.*;
import android.support.v7.widget.*;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.widget.*;

import com.codeclub.abu.chatcodeclub.R;
import com.codeclub.abu.chatcodeclub.adapters.MessageAdapter;
import com.codeclub.abu.chatcodeclub.helpers.TimeAgo;
import com.codeclub.abu.chatcodeclub.models.Message;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.squareup.picasso.Picasso;

import java.util.*;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String mCrrUserId;
    private String mOtherUserId;
    private DatabaseReference mRootRef;

    private FirebaseAuth mAuth;
    private TextView mUsername;
    private TextView mLastSeen;
    private CircleImageView mProfileImage;

    private ImageView mChatAddBtn;
    private ImageView mChatSendBtn;
    private EditText mChatMessage;

    private RecyclerView mMessagesRecyclerV;

    private List<Message> mMessagesList;
    private MessageAdapter mAdapter;

    private final int MSGS_TO_LOAD = 10;
    private int crrPage = 1;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mOtherUserId = getIntent().getStringExtra("userId");
        String username = getIntent().getStringExtra("username");
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowCustomEnabled(true);
        }


        // Setting custom action bar
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_appbar, null);
        if (actionBar != null)
            actionBar.setCustomView(action_bar_view);


        mRootRef = FirebaseDatabase.getInstance().getReference();
        mCrrUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mUsername = (TextView) findViewById(R.id.chat_appbar_username);
        mLastSeen = (TextView) findViewById(R.id.chat_appbar_lastseen);
        mProfileImage = (CircleImageView) findViewById(R.id.single_chat_appbar_image);

        mChatAddBtn = (ImageButton) findViewById(R.id.single_chat_add);
        mChatSendBtn = (ImageButton) findViewById(R.id.single_chat_send);
        mChatMessage = (EditText) findViewById(R.id.single_chat_message);


        mMessagesList = new ArrayList<>();

        mAdapter = new MessageAdapter(mMessagesList);

        mMessagesRecyclerV = (RecyclerView) findViewById(R.id.single_chat_messages_list);
        mMessagesRecyclerV.setLayoutManager(new LinearLayoutManager(this));
        mMessagesRecyclerV.setHasFixedSize(true);
        mMessagesRecyclerV.setAdapter(mAdapter);

        mUsername.setText(username);
        loadMessages();

        mRootRef.child("users").child(mOtherUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String lastSeen = String.valueOf(dataSnapshot.child("online").getValue());
                String imageUrl = String.valueOf(dataSnapshot.child("image").getValue());
                setUserImage(imageUrl);

                if (lastSeen.equals("true")) {
                    mLastSeen.setText("Online");
                }
                else {
                    long time = Long.parseLong(lastSeen);
                    mLastSeen.setText(TimeAgo.getTimeAgo(time, getApplicationContext()));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mChatSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });

    }

    private void loadMessages() {
        DatabaseReference messageRef = mRootRef.child("messages").child(mCrrUserId).child(mOtherUserId);

        messageRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Message m = dataSnapshot.getValue(Message.class);

                mMessagesList.add(m);
                mAdapter.notifyDataSetChanged();

                mMessagesRecyclerV.scrollToPosition(mAdapter.getItemCount() - 1);


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

    private void sendMessage() {
        String msg = mChatMessage.getText().toString();
        if (!TextUtils.isEmpty(msg)) {
            mChatMessage.setText("");
            String crrUserRef = "messages/" + mCrrUserId + "/" + mOtherUserId;
            String otherUserRef = "messages/" + mOtherUserId + "/" + mCrrUserId;

            DatabaseReference userMessagePush = mRootRef
                    .child("messages").child(mCrrUserId).child(mOtherUserId).push();

            String pushId = userMessagePush.getKey();

            Map<String, Object> messageMap = new HashMap<>();
            messageMap.put("message", msg);
            messageMap.put("from", mCrrUserId);
            messageMap.put("seen", false);
            messageMap.put("type", "text");
            messageMap.put("time", ServerValue.TIMESTAMP);

            HashMap<String, Object> messageUserMap = new HashMap<>();
            messageUserMap.put(crrUserRef + "/" + pushId, messageMap);
            messageUserMap.put(otherUserRef + "/" + pushId, messageMap);

            // Updating chat part
            Map<String, Object> userMap = new HashMap<>();

            Map<String, Object> chatMap1 = new HashMap<>();
            chatMap1.put("seen", true);
            chatMap1.put("timestamp", ServerValue.TIMESTAMP);

            Map<String, Object> chatMap2 = new HashMap<>();
            chatMap2.put("seen", false);
            chatMap2.put("timestamp", ServerValue.TIMESTAMP);

            userMap.put("chats/" + mCrrUserId + "/" + mOtherUserId, chatMap1);
            userMap.put("chats/" + mOtherUserId + "/" + mCrrUserId, chatMap2);

            mRootRef.updateChildren(userMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null)
                        Log.d("CHAT", databaseError.getMessage());
                }
            });


            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null)
                        Log.d("CHAT", databaseError.getMessage());
                }
            });
            mAdapter.notifyDataSetChanged();
        }
    }

    public void setUserImage(String imageUrl) {
        Picasso.with(ChatActivity.this).load(imageUrl).into(mProfileImage);
    }
}
