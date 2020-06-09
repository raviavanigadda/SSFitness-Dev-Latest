package com.app.ssfitness_dev.ui.home.chat;

import android.content.Context;
import android.content.Intent;
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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.ssfitness_dev.R;
import com.app.ssfitness_dev.data.models.User;
import com.app.ssfitness_dev.ui.home.userprofiles.UserProfileActivity;
import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class AvailableUsers extends Fragment {

    private RecyclerView mUsersRecyclerList;
    FirebaseRecyclerOptions<User> options;
    FirestoreRecyclerOptions<User> firestoreRecyclerOptions;
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mCurrentUserDatabase;
    private FirebaseAuth mAuth;
    private com.google.firebase.database.Query mQueryRef;
    private ArrayList<User> userArrayList;

    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private Query mUsersRef;
    private String searchQuery="";
    String current_user_name;
    UserRecAdapter userRecAdapter;

    public AvailableUsers() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            searchQuery = bundle.getString("searchName");
        }

        return inflater.inflate(R.layout.fragment_available_users, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAuth = FirebaseAuth.getInstance();

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("users");

        mUsersRecyclerList = view.findViewById(R.id.recyclerview_users);
        mUsersRecyclerList.setHasFixedSize(true);
        mUsersRecyclerList.setLayoutManager(new LinearLayoutManager(getContext()));
        userArrayList = new ArrayList<User>();
        userRecAdapter = new UserRecAdapter(userArrayList,getContext());
        mUsersRecyclerList.setAdapter(userRecAdapter);

    }

    @Override
    public void onStart() {
        super.onStart();

        /*
        firestoreRecyclerOptions = new FirestoreRecyclerOptions.Builder<User>().setQuery(mUsersRef, User.class).build();
        FirestoreRecyclerAdapter<User, UsersViewHolder> firestoreRecyclerAdapter = new FirestoreRecyclerAdapter<User, UsersViewHolder>(firestoreRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull UsersViewHolder holder, int position, @NonNull User model) {
                holder.setDetails(model.getUserName(), model.getActivitylevel(), model.getPhotoUrl());
            }

            @NonNull
            @Override
            public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                View view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.users_search_single_item, viewGroup, false);

                return new UsersViewHolder(view);
            }
        };

        firestoreRecyclerAdapter.startListening();
        mUsersRecyclerList.setAdapter(firestoreRecyclerAdapter);
    */

        /*mUsersDatabase.child(mAuth.getCurrentUser().getUid()).child("userName").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                current_user_name =dataSnapshot.getValue().toString();
                if(!searchQuery.equals( current_user_name)) {
                    mQueryRef = mUsersDatabase.orderByChild("userName").startAt(searchQuery).endAt(searchQuery + "\\uf8ff");
                    //mUsersRef = firebaseFirestore.collection("users");//.whereEqualTo("userEmail", "ravi.gamer95@gmail.com");;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });*/

        getSearchResults();
    }

    private void getSearchResults(){
        if(!searchQuery.equals("")) {

            if(searchQuery.contains(" ")){
                String[] search_array = searchQuery.split(" ");
                String firstName = search_array[0].substring(0,1).toUpperCase()+search_array[0].substring(1).toLowerCase();
                String lastName = search_array[1].substring(0,1).toUpperCase()+search_array[1].substring(1).toLowerCase();
                searchQuery = firstName+" "+lastName;
            }
            else {
                searchQuery = searchQuery.substring(0,1).toUpperCase()+searchQuery.substring(1).toLowerCase();
            }
            //Toast.makeText(getContext(),searchQuery,Toast.LENGTH_LONG).show();
            mUsersDatabase.orderByChild("userName").startAt(searchQuery).endAt(searchQuery+"\uf8ff").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    userArrayList.clear();
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {

                        if (!ds.getKey().equals(mAuth.getCurrentUser().getUid())) {
                            User user = ds.getValue(User.class);
                            userArrayList.add(user);
                        }
                    }
                    userRecAdapter.notifyDataSetChanged();

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(getContext(), databaseError.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
        /*options = new FirebaseRecyclerOptions.Builder<User>().setQuery(mUsersDatabase,User.class).build();

        FirebaseRecyclerAdapter<User, UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<User, UsersViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull UsersViewHolder holder, int position, @NonNull User model) {

                holder.setDetails(model.getUserName(), model.getActivitylevel(), model.getPhotoUrl());
                String userID;

                userID = getRef(position).getKey();

                holder.mView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent profileIntent = new Intent(getContext(), UserProfileActivity.class);
                        profileIntent.putExtra("user_id", userID);
                        startActivity(profileIntent);
                    }
                });


            }

            @NonNull
            @Override
            public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
                View view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.users_search_single_item, viewGroup, false);

                return new UsersViewHolder(view);
            }
        };

        firebaseRecyclerAdapter.startListening();
        mUsersRecyclerList.setAdapter(firebaseRecyclerAdapter);*/
    }


    //Adapter

    public class UserRecAdapter extends RecyclerView.Adapter<UserRecAdapter.UsersViewHolder>  {

        private ArrayList<User> UserArrayList;
        private Context context;
        private View.OnClickListener UserListener;

        public UserRecAdapter(ArrayList<User> UserArrayList, Context context) {
            this.UserArrayList = UserArrayList;
            this.context = context;
        }

        @NonNull
        @Override
        public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_search_single_item,parent,false);

            return new UsersViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull UsersViewHolder holder, int position) {
            holder.userNameView.setText(UserArrayList.get(position).getUserName());
            holder.userActivityView.setText(UserArrayList.get(position).getActivitylevel());
            if(!UserArrayList.get(position).getPhotoUrl().equals("")){
                Glide.with(context).load(UserArrayList.get(position).getPhotoUrl()).into(holder.userProfileView);
            }


            String userID = UserArrayList.get(position).getUserID();
            holder.itemView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent profileIntent = new Intent(getContext(), UserProfileActivity.class);
                    profileIntent.putExtra("user_id", userID);
                    startActivity(profileIntent);
                }
            });


        }

        public void setOnClickListener(View.OnClickListener clickListener){
            UserListener = clickListener;
        }

        @Override
        public int getItemCount() {
            return UserArrayList.size();
        }

        public class UsersViewHolder extends RecyclerView.ViewHolder{

            TextView userNameView, userActivityView;
            ImageView userProfileView;

            public UsersViewHolder(@NonNull View itemView) {
                super(itemView);
                userNameView = itemView.findViewById(R.id.user_single_name);
                userActivityView = itemView.findViewById(R.id.user_single_activity_level);
                userProfileView = itemView.findViewById(R.id.user_single_profile);

                itemView.setTag(this);

                itemView.setOnClickListener(UserListener);

            }
        }


    }

}
