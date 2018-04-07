package com.codeclub.abu.chatcodeclub.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.*;
import android.util.Log;
import android.view.*;
import android.widget.*;

import com.codeclub.abu.chatcodeclub.R;
import com.codeclub.abu.chatcodeclub.models.Request;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.squareup.picasso.Picasso;

import java.util.*;

import de.hdodenhof.circleimageview.CircleImageView;

public class RequestsFragment extends Fragment {


    private View mMainView;
    private RecyclerView mRequestsRecyclerV;
    private FirebaseAuth mAuth;
    private String mCrrUserId;
    private DatabaseReference mRequestsDb;
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mFriendsDb;
    private DatabaseReference mRootDb;

    public RequestsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mMainView = inflater.inflate(R.layout.fragment_requests, container, false);

        mRequestsRecyclerV = (RecyclerView) mMainView.findViewById(R.id.request_recyclerView);
        mAuth = FirebaseAuth.getInstance();

        mCrrUserId = mAuth.getCurrentUser().getUid();

        mRequestsDb = FirebaseDatabase.getInstance().getReference().child("friends_req").child(mCrrUserId).child("sent");

        mFriendsDb = FirebaseDatabase.getInstance().getReference().child("friends");
//        mRequestsDb.keepSynced(true);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("users");
//        mUsersDatabase.keepSynced(true);

        mRootDb = FirebaseDatabase.getInstance().getReference();

        mRequestsRecyclerV.setHasFixedSize(true);
        mRequestsRecyclerV.setLayoutManager(new LinearLayoutManager(getContext()));

        // Inflate the layout for this fragment
        return mMainView;
    }


    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Request, RequestViewHolder> RequestsRecyclerViewAdapter =
                new FirebaseRecyclerAdapter<Request, RequestViewHolder>(
                        Request.class,
                        R.layout.request_single_layout,
                        RequestViewHolder.class,
                        mRequestsDb

                ) {
                    @Override
                    protected void populateViewHolder(final RequestViewHolder requestViewHolder, Request req, int i) {

                        final String mCrrUserInList = getRef(i).getKey();
                        Log.d("TAGTAG", "populateViewHolder: mCrrUserInList " + mCrrUserInList);
                        mUsersDatabase.child(mCrrUserInList).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String username = dataSnapshot.child("username").getValue().toString();
                                String thumbImg = dataSnapshot.child("thumb_img").getValue().toString();
                                requestViewHolder.setUsername(username);
                                requestViewHolder.setImage(thumbImg, getContext());

                                // accept btn
                                // create the friendship
                                // delete the reqs
                                requestViewHolder.mAcceptBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Map<String, Object> friendsMap = new HashMap<>();

                                        friendsMap.put(mCrrUserId + "/" + mCrrUserInList + "/date", ServerValue.TIMESTAMP);
                                        friendsMap.put(mCrrUserInList + "/" + mCrrUserId + "/date", ServerValue.TIMESTAMP);


                                        mFriendsDb.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                if (databaseError != null)
                                                    Log.d("CHAT", databaseError.getMessage());

                                                mRootDb.child("friends_req").child(mCrrUserId).child("sent")
                                                        .child(mCrrUserInList).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        mRootDb.child("friends_req").child(mCrrUserInList).child("sent")
                                                                .child(mCrrUserId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Toast.makeText(getActivity(), "You are now friends", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                                    }
                                                });
                                            }
                                        });
                                    }
                                });

                                // decline btn
                                // remove reqs from both sides
                                requestViewHolder.mDeclineBtn.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        mRequestsDb.child(mCrrUserInList).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                mRootDb.child("friends_req").child(mCrrUserInList).child("received")
                                                        .child(mCrrUserId).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                    }
                                                });
                                            }
                                        });

                                    }
                                });
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }
                };
        mRequestsRecyclerV.setAdapter(RequestsRecyclerViewAdapter);


    }


    public static class RequestViewHolder extends RecyclerView.ViewHolder {

        View mView;
        CircleImageView mImage;
        TextView mUsername;
        ImageButton mAcceptBtn;
        ImageButton mDeclineBtn;

        public RequestViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            mImage = (CircleImageView) mView.findViewById(R.id.request_single_image);
            mUsername = (TextView) mView.findViewById(R.id.request_single_text);
            mAcceptBtn = (ImageButton) mView.findViewById(R.id.request_single_done);
            mDeclineBtn = (ImageButton) mView.findViewById(R.id.request_single_clear);


        }


        public void setUsername(String name) {
            mUsername.setText(name);
        }

        public void setImage(String url, Context cnx) {
            CircleImageView userImageView = (CircleImageView) mView.findViewById(R.id.request_single_image);
            Picasso.with(cnx).load(url).placeholder(R.drawable.user2).into(userImageView);
        }
    }
}
