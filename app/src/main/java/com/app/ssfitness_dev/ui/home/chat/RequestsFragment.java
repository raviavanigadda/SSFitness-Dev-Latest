package com.app.ssfitness_dev.ui.home.chat;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.ssfitness_dev.R;
import com.app.ssfitness_dev.data.models.User;
import com.app.ssfitness_dev.ui.home.HomeActivity;
import com.app.ssfitness_dev.ui.home.userprofiles.UserProfileActivity;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * A simple {@link Fragment} subclass.
 */
public class RequestsFragment extends Fragment {
    FirebaseRecyclerOptions<Notifications> options,sent_options;
    int count = 0,count1 = 0;
    private DatabaseReference mNotificationsDatabase,mSentFriendRequestDatabase;
    private DatabaseReference mUserReference;
    private String mCurrentUserId;
    private FirebaseAuth mAuth;
    private RecyclerView mRequestsReceivedRecyclerView,mRequestsSentRecycleView;

    public RequestsFragment() {
        /// Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_requests, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        mCurrentUserId = mAuth.getCurrentUser().getUid();

        mNotificationsDatabase = FirebaseDatabase.getInstance().getReference().child("notifications").child(mCurrentUserId);
        //mNotificationsDatabase.keepSynced(true);

        mSentFriendRequestDatabase = FirebaseDatabase.getInstance().getReference().child("friend_requests").child(mCurrentUserId);
        //mSentFriendRequestDatabase.keepSynced(true);

        mUserReference = FirebaseDatabase.getInstance().getReference().child("users");

        mRequestsReceivedRecyclerView = view.findViewById(R.id.recycler_view_received_requests);
        mRequestsReceivedRecyclerView.setHasFixedSize(true);
        mRequestsReceivedRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mRequestsSentRecycleView = view.findViewById(R.id.recycler_view_sent_requests);
        mRequestsSentRecycleView.setHasFixedSize(true);
        mRequestsSentRecycleView.setLayoutManager(new LinearLayoutManager(getContext()));

    }

    @Override
    public void onStart() {
        super.onStart();
        getReceivedRequests();
        getSentRequests();
    }

    private void getReceivedRequests(){
        options =
                new FirebaseRecyclerOptions.Builder<Notifications>()
                        .setQuery(mNotificationsDatabase, new SnapshotParser<Notifications>() {
                            @NonNull
                            @Override
                            public Notifications parseSnapshot(@NonNull DataSnapshot snapshot) {
                                return new Notifications(snapshot.child("from").getValue().toString(), snapshot.child("type").getValue().toString(), snapshot.getKey());
                            }
                        })
                        .build();

        FirebaseRecyclerAdapter<Notifications, FriendRequestViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Notifications, FriendRequestViewHolder>(options) {

            @Override
            protected void onBindViewHolder(@NonNull FriendRequestViewHolder holder, int position, @NonNull Notifications model) {
                mUserReference.child(model.getFrom()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        holder.request_user_name.setText(user.userName);
                        holder.view_request_btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent i = new Intent(getActivity(), UserProfileActivity.class);
                                i.putExtra("user_id",model.getFrom());
                                i.putExtra("notification_id",model.getReqId());
                                startActivity(i);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }


            @NonNull
            @Override
            public RequestsFragment.FriendRequestViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                View view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.layout_requests_view, viewGroup, false);

                return new RequestsFragment.FriendRequestViewHolder(view);
            }
        };

        firebaseRecyclerAdapter.startListening();
        do {
            count++;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    firebaseRecyclerAdapter.notifyDataSetChanged();
                }
            }, 1000);
        }
        while (options == null && count == 3);
        firebaseRecyclerAdapter.notifyDataSetChanged();
        mRequestsReceivedRecyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    private void getSentRequests(){
        sent_options =
                new FirebaseRecyclerOptions.Builder<Notifications>()
                        .setQuery(mSentFriendRequestDatabase.orderByChild("request_type").equalTo("sent"), new SnapshotParser<Notifications>() {
                            @NonNull
                            @Override
                            public Notifications parseSnapshot(@NonNull DataSnapshot snapshot) {
                                return new Notifications(snapshot.getKey(), snapshot.child("request_type").getValue().toString());
                            }
                        })
                        .build();

        FirebaseRecyclerAdapter<Notifications, FriendRequestViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Notifications, FriendRequestViewHolder>(sent_options) {

            @Override
            protected void onBindViewHolder(@NonNull FriendRequestViewHolder holder, int position, @NonNull Notifications model) {
                mUserReference.child(model.getFrom()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.getValue(User.class);
                        assert user != null;
                        holder.request_user_name.setText(user.userName);
                        holder.view_request_btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent i = new Intent(getActivity(), UserProfileActivity.class);
                                i.putExtra("user_id",model.getFrom());
                                startActivity(i);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }


            @NonNull
            @Override
            public RequestsFragment.FriendRequestViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                View view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.layout_requests_view, viewGroup, false);

                return new RequestsFragment.FriendRequestViewHolder(view);
            }
        };

        firebaseRecyclerAdapter.startListening();
        do {
            count1++;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    firebaseRecyclerAdapter.notifyDataSetChanged();
                }
            }, 1000);
        }
        while (options == null && count1 == 3);
        firebaseRecyclerAdapter.notifyDataSetChanged();
        mRequestsSentRecycleView.setAdapter(firebaseRecyclerAdapter);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (getFragmentManager() != null) {

            getFragmentManager()
                    .beginTransaction()
                    .detach(this)
                    .attach(this)
                    .commit();
        }
    }

    public static class FriendRequestViewHolder extends RecyclerView.ViewHolder {

        MaterialCardView view_request_btn;
        TextView request_user_name;
        public FriendRequestViewHolder(@NonNull View itemView) {
            super(itemView);
            view_request_btn = itemView.findViewById(R.id.view_request_btn);
            request_user_name = itemView.findViewById(R.id.text_view_request);
        }
    }
}
