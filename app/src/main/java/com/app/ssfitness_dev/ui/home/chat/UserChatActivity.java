package com.app.ssfitness_dev.ui.home.chat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


import com.app.ssfitness_dev.R;
import com.app.ssfitness_dev.data.models.User;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.DatabaseReference.CompletionListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask.TaskSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserChatActivity extends AppCompatActivity {

    private static final int GALLERY_PICK = 1;
    private static final int TOTAL_ITEMS_TO_LOAD = 10;
    //Retrieve Messages
    private final List<Messages> messagesList = new ArrayList<>();
    private String mChatUser, mChatUserName;
    private Toolbar mChatToolbar;
    private FirebaseAuth mAuth;
    private DatabaseReference mUserRef;
    private DatabaseReference mRootRef;
    //For sending images in chat
    private StorageReference mImageStorage;
    private FirebaseUser mCurrentUser;
    private TextView mTitleView;
    private TextView mLastSeenView;
    private CircleImageView mProfileImage;
    private String mCurrentUserId;
    private RecyclerView mMessagesList;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ImageButton mChatAdd, mChatSend;
    private EditText mChatMessage;
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;
    private int mCurrentPage = 1;

    //New solutions for loading comments
    private int itemPos = 0;
    private String mLastKey = "";
    private String mPrevKey = "";
    private int prevListsize = 0;

    // Required variables for group chat
    private boolean isGroup = false;
    private ArrayList<String> group_users;
    private ArrayList<User> grp_user_data_list;
    private boolean msg_deleted = false, child_added = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_chat);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mCurrentUserId = mCurrentUser.getUid();
        mChatUser = getIntent().getStringExtra("user_id");
        mChatUserName = getIntent().getStringExtra("user_name");

        mMessagesList = findViewById(R.id.messages_list);

        //Refresh view layout
        mSwipeRefreshLayout = findViewById(R.id.message_swipe_layout);

        mChatToolbar = findViewById(R.id.chatAppBar);
        mRootRef = FirebaseDatabase.getInstance().getReference();

        //Retrieve Messages
        mLinearLayout = new LinearLayoutManager(this);

        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);

        mAdapter = new MessageAdapter(messagesList,this,mChatUser);
        mMessagesList.setAdapter(mAdapter);
        mMessagesList.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom < oldBottom) {
                    if (messagesList != null && messagesList.size() > 0) {
                        mMessagesList.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mMessagesList.smoothScrollToPosition(messagesList.size() - 1);
                            }
                        }, 100);
                    }
                }
            }
        });

        setSupportActionBar(mChatToolbar);

        ActionBar actionBar = getSupportActionBar();

        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        mChatToolbar.setNavigationOnClickListener(view -> finish());

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar, null);

        actionBar.setCustomView(action_bar_view);

        //Custom Action bar item
        mTitleView = findViewById(R.id.custom_bar_title);
        mLastSeenView = findViewById(R.id.custom_bar_seen);
        mProfileImage = findViewById(R.id.custom_bar_image);
        //mChatAdd = findViewById(R.id.chat_add);
        mChatSend = findViewById(R.id.chat_send);
        mChatMessage = findViewById(R.id.chat_edit_text);
        mTitleView.setText(mChatUserName);

        if (mAuth.getCurrentUser() != null) {
            mUserRef = FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getCurrentUser().getUid());
        }

        //Time ago
        mRootRef.child("users").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.getChildrenCount() > 0) {
                    String online = dataSnapshot.child("online").getValue().toString();
                    String image = dataSnapshot.child("photoUrl").getValue().toString();

                    if (!image.equals("")) {
                        Glide.with(getApplicationContext()).load(image).into(mProfileImage);
                    }

                    if (online.equals("true")) {
                        mLastSeenView.setText("Online");
                    } else {
                        GetTimeAgo getTime = new GetTimeAgo();

                        long lastTime = Long.parseLong(online);

                        String lastSeenTime = getTime.getTimeAgo(lastTime);

                        mLastSeenView.setText(lastSeenTime);
                    }

                    loadMessages();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mRootRef.child("groups").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.getChildrenCount() > 0) {
                    isGroup = true;
                    mAdapter.checkForGroup(isGroup);
                    mLastSeenView.setVisibility(View.GONE);
                    String image = dataSnapshot.child("photoUrl").getValue().toString();

                    group_users = new ArrayList<String>();
                    grp_user_data_list = new ArrayList<User>();
                    group_users.clear();
                    for (DataSnapshot ds : dataSnapshot.child("group_users").getChildren()) {
                        group_users.add(ds.getKey());

                        // Read from the database
                        FirebaseDatabase.getInstance().getReference().child("users").child(ds.getKey()).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot userDS) {
                                // This method is called once with the initial value and again
                                // whenever data at this location is updated.
                                User user = userDS.getValue(User.class);
                                grp_user_data_list.add(user);

                                if (grp_user_data_list.size() == (int) dataSnapshot.child("group_users").getChildrenCount()) {
                                    loadMessages();
                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError error) {

                            }
                        });
                    }
                    if (!image.equals("")) {
                        Glide.with(getApplicationContext()).load(image).into(mProfileImage);
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        /*mChatAdd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("iamge/");

                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);
            }
        });*/


        /////////// CHAT FUNCTIONS /////////////

        mChatSend.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });

        //////REFRESH LAYOUT
        mSwipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                mCurrentPage++;

                // messagesList.clear();

                itemPos = 0;
                prevListsize = messagesList.size();
                loadMoreMessages();
            }
        });

    }


    //Only for loading more messages
    private void loadMoreMessages() {
        DatabaseReference messageRef = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser);

        Query messageQuery = messageRef.orderByKey().endAt(mLastKey).limitToLast(10);

        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                Messages messages = dataSnapshot.getValue(Messages.class);
                messages.setMessageId(dataSnapshot.getKey());
                if (isGroup){
                    for (User user : grp_user_data_list){
                        if(user.userID.equals(messages.getFrom())){
                            messages.setUserName(user.userName);
                        }
                    }
                }

                String messageKey = dataSnapshot.getKey();
                //messagesList.add(itemPos++,messages);

                //Removes repeated message
                if (!mPrevKey.equals(messageKey)) {
                    if(msg_deleted) {
                        msg_deleted = false;
                    }
                    else {
                        messagesList.add(itemPos++,messages);
                    }
                } else {
                    mPrevKey = mLastKey;
                }

                if (itemPos == 1) {
                    mLastKey = messageKey;
                }

                mAdapter.notifyDataSetChanged();

                mSwipeRefreshLayout.setRefreshing(false);

                //mLinearLayout.scrollToPositionWithOffset(10, 0);
                mMessagesList.scrollToPosition(messagesList.size()-prevListsize);

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                if(child_added){
                    child_added = false;
                    msg_deleted = false;
                }
                else {
                    msg_deleted = true;
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    //Only for loading messages
    private void loadMessages() {

        //mar 24
        DatabaseReference messageRef = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser);

        Query messageQuery = messageRef.limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD);

        //retrieve the data, coz we need to work with child remove
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                //Once we get all messages we receive it as data snapshow

                Messages messages = dataSnapshot.getValue(Messages.class);
                messages.setMessageId(dataSnapshot.getKey());
                if (isGroup){
                    for (User user : grp_user_data_list){
                        if(user.userID.equals(messages.getFrom())){
                            messages.setUserName(user.userName);
                        }
                    }
                }
                /*-- This Code was OutSide --*/
                //add new message id to db
                itemPos++;

                if (itemPos == 1) {
                    String messageKey = dataSnapshot.getKey();

                    mLastKey = messageKey;
                    mPrevKey = messageKey;
                }
                if(msg_deleted) {
                    msg_deleted = false;
                }
                else {
                    messagesList.add(messages);
                }

                mAdapter.notifyDataSetChanged();
                //bottom of recycler view
                mMessagesList.scrollToPosition(messagesList.size() - 1);

                mSwipeRefreshLayout.setRefreshing(false);
                /*-- OutSide Code Finish --*/
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                if(child_added){
                    child_added = false;
                    msg_deleted = false;
                }
                else {
                    msg_deleted = true;
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void sendMessage() {
        child_added = true;
        String message = mChatMessage.getText().toString().trim();

        if (!TextUtils.isEmpty(message)) {

            Map messageMap = new HashMap();
            messageMap.put("message", message);
            messageMap.put("seen", false);
            messageMap.put("type", "text");
            messageMap.put("time", ServerValue.TIMESTAMP);
            messageMap.put("from", mCurrentUserId);

            Map updateChatMap = new HashMap();
            updateChatMap.put("seen", false);
            updateChatMap.put("timestamp", ServerValue.TIMESTAMP);

            Map messageUserMap = new HashMap();

            if (isGroup) {

                if (group_users != null && group_users.size() > 0) {

                    DatabaseReference user_message_push = mRootRef.child("messages").child(mCurrentUserId)
                            .child(mChatUser).push();
                    String push_id = user_message_push.getKey();

                    for (String grp_user_ids : group_users) {
                        messageUserMap.put("messages/" + grp_user_ids + "/" + mChatUser + "/" + push_id, messageMap);
                        messageUserMap.put("chat/" + grp_user_ids + "/" + mChatUser, updateChatMap);
                    }
                }
            } else {
                String sender_ref = "messages/" + mCurrentUserId + "/" + mChatUser;
                String receiver_ref = "messages/" + mChatUser + "/" + mCurrentUserId;
                String sender_chat_ref = "chat/" + mCurrentUserId + "/" + mChatUser;
                String receiver_chat_ref = "chat/" + mChatUser + "/" + mCurrentUserId;

                DatabaseReference user_message_push = mRootRef.child("messages").child(mCurrentUserId)
                        .child(mChatUser).push();
                String push_id = user_message_push.getKey();

                messageUserMap.put(sender_ref + "/" + push_id, messageMap);
                messageUserMap.put(receiver_ref + "/" + push_id, messageMap);
                messageUserMap.put(sender_chat_ref, updateChatMap);
                messageUserMap.put(receiver_chat_ref, updateChatMap);
            }

            mChatMessage.setText("");
            mRootRef.updateChildren(messageUserMap, (databaseError, databaseReference) -> {
                if (databaseError != null) {
                    Log.d("CHAT_LOG", databaseError.getMessage());
                }
            });
        }

    }


    @Override
    protected void onStart() {

        super.onStart();
        mRootRef.child("users").child(mCurrentUser.getUid()).child("online").setValue("true");

    }

}
