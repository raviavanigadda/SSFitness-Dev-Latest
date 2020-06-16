package com.app.ssfitness_dev.ui.home.chat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.ssfitness_dev.R;
import com.app.ssfitness_dev.data.models.User;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;
import static com.app.ssfitness_dev.utilities.Constants.TAG_GOAL_FRAGMENT;
import static com.app.ssfitness_dev.utilities.Constants.TAG_GROUP_PROFILE_FRAGMENT;
import static com.app.ssfitness_dev.utilities.Constants.TAG_USER_PROFILE_FRAGMENT;
import static com.app.ssfitness_dev.utilities.HelperClass.logMessage;
import static com.app.ssfitness_dev.utilities.HelperClass.makeSnackBarMessage;


/**
 * A simple {@link Fragment} subclass.
 */
public class CreateGroupFragment extends Fragment {

    FirebaseUser mCurrentUser;
    private NavController CGNavController;
    private RecyclerView cg_select_friends_recycler_view;
    private DatabaseReference mFriendsDatabase,mUserDatabase,mRootDatabase;
    private ArrayList<User> friendsArrayList,selectedFrindsArrayList;
    private FriendsRecAdapter friendsRecAdapter;
    private Context context;
    private ImageView group_icon_img_view;
    private EditText group_name_edit_text;
    private TextView cg_edit_text_error;
    private Uri resultUri;
    private String photoUrl;

    public CreateGroupFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_group, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        context = getActivity().getApplicationContext();
        mCurrentUser  = FirebaseAuth.getInstance().getCurrentUser();
        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("friends").child(mCurrentUser.getUid());
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        mRootDatabase = FirebaseDatabase.getInstance().getReference();
        CGNavController = Navigation.findNavController(getActivity(), R.id.cg_host_fragment);

        group_name_edit_text = view.findViewById(R.id.group_name_edit_text);
        group_icon_img_view = view.findViewById(R.id.group_icon_img_view);
        cg_edit_text_error = view.findViewById(R.id.cg_edit_text_error);

        group_icon_img_view.setOnClickListener(view1 -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent,"SELECT IMAGE"), 1);
        });

        cg_select_friends_recycler_view = view.findViewById(R.id.cg_select_friends_recycler_view);
        cg_select_friends_recycler_view.setHasFixedSize(true);
        cg_select_friends_recycler_view.setLayoutManager(new LinearLayoutManager(getContext()));
        friendsArrayList = new ArrayList<User>();
        selectedFrindsArrayList = new ArrayList<User>();
        friendsRecAdapter = new FriendsRecAdapter(friendsArrayList,getContext(),new FriendsRecAdapter.OnItemCheckListener(){

            @Override
            public void onItemCheck(User user) {
                selectedFrindsArrayList.add(user);
            }

            @Override
            public void onItemUnCheck(User user) {
                selectedFrindsArrayList.remove(user);
            }
        });
        cg_select_friends_recycler_view.setAdapter(friendsRecAdapter);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //if the intent from activity code is 1
        if (requestCode == 1 && resultCode == RESULT_OK) {

            final Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setAspectRatio(1,1)
                    .start(getContext(),this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri mResultUri = result.getUri();
                resultUri = mResultUri;
                group_icon_img_view.setImageURI(resultUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.cg_create_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.cg_create_btn){
            cg_edit_text_error.setVisibility(View.GONE);
            if(TextUtils.isEmpty(group_name_edit_text.getText().toString().trim())){
                cg_edit_text_error.setVisibility(View.VISIBLE);
            }
            else if(selectedFrindsArrayList.size() == 0){
                makeSnackBarMessage(getView(),"Please Select Atleast One Friend!");
            }
            else {
                CreateGroup(item);
            }

        }
        return super.onOptionsItemSelected(item);
    }

    private void CreateGroup(MenuItem menuItem){
        menuItem.setEnabled(false);
        String group_creation_date = DateFormat.getDateInstance().format(new Date());
        String group_name = group_name_edit_text.getText().toString().trim();
        group_name = group_name.substring(0,1).toUpperCase()+group_name.substring(1).toLowerCase();

        DatabaseReference newGroupRef = mRootDatabase.child("groups").push();
        String newGroupId = newGroupRef.getKey();

        Map groupData = new HashMap<>();
        groupData.put("group_id",newGroupId);
        groupData.put("group_name", group_name);
        groupData.put("admin", mCurrentUser.getUid());
        groupData.put("created_on",group_creation_date);
        groupData.put("photoUrl","");

        Map chatMap = new HashMap();
        chatMap.put("seen", false);
        chatMap.put("timestamp", ServerValue.TIMESTAMP);

        if (resultUri != null) {
            StorageReference filepath = FirebaseStorage.getInstance().getReference()
                    .child("groupphotos").child(newGroupId);

            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getActivity().getApplication().getContentResolver(), resultUri);

            } catch (IOException e) {
                e.printStackTrace();
            }

            //tomake the image small size
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, baos);

            byte[] data = baos.toByteArray();

            //uploading the image
            UploadTask uploadTask = filepath.putBytes(data);

            uploadTask.addOnFailureListener(e -> {
                logMessage(TAG_GROUP_PROFILE_FRAGMENT,e.toString());
            });

            uploadTask.addOnSuccessListener(taskSnapshot -> {
                if (taskSnapshot.getMetadata() != null) {
                    if (taskSnapshot.getMetadata().getReference() != null) {
                        Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                        result.addOnSuccessListener(uri -> {
                            photoUrl = uri.toString();
                            groupData.put("photoUrl", photoUrl);

                            Map user_date_map = new HashMap();
                            user_date_map.put("date",DateFormat.getDateInstance().format(new Date()));

                            Map group_user_map = new HashMap();
                            group_user_map.put(mCurrentUser.getUid(),user_date_map);

                            Map rootMap = new HashMap();
                            rootMap.put("chat/"+mCurrentUser.getUid()+"/"+newGroupId,chatMap);
                            for (User user: selectedFrindsArrayList){
                                rootMap.put("chat/"+user.userID+"/"+newGroupId,chatMap);
                                group_user_map.put(user.userID,user_date_map);
                            }
                            groupData.put("group_users",group_user_map);
                            rootMap.put("groups/"+newGroupId,groupData);


                            mRootDatabase.updateChildren(rootMap, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                    if(databaseError != null) {
                                        menuItem.setEnabled(true);
                                        makeSnackBarMessage(getView(),databaseError.getMessage());
                                    }
                                    else {
                                        menuItem.setEnabled(true);
                                        makeSnackBarMessage(getView(),"Group Created Successfully!");
                                        getActivity().finish();
                                    }
                                }
                            });

                        });
                    }
                } });
        }
        else {

            Map user_date_map = new HashMap();
            user_date_map.put("date",DateFormat.getDateInstance().format(new Date()));

            Map group_user_map = new HashMap();
            group_user_map.put(mCurrentUser.getUid(),user_date_map);

            Map rootMap = new HashMap();
            rootMap.put("chat/"+mCurrentUser.getUid()+"/"+newGroupId,chatMap);
            for (User user: selectedFrindsArrayList){
                rootMap.put("chat/"+user.userID+"/"+newGroupId,chatMap);
                group_user_map.put(user.userID,user_date_map);
            }
            groupData.put("group_users",group_user_map);
            rootMap.put("groups/"+newGroupId,groupData);


            rootMap.put("groups/"+newGroupId,groupData);
            rootMap.put("chat/"+mCurrentUser.getUid()+"/"+newGroupId,chatMap);
            for (User user: selectedFrindsArrayList){
                rootMap.put("chat/"+user.userID+"/"+newGroupId,chatMap);
            }
            mRootDatabase.updateChildren(rootMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    if(databaseError != null) {
                        menuItem.setEnabled(true);
                        makeSnackBarMessage(getView(),databaseError.getMessage());
                    }
                    else {
                        menuItem.setEnabled(true);
                        makeSnackBarMessage(getView(),"Group Created Successfully!");
                        getActivity().finish();
                    }
                }
            });
        }
    }
    @Override
    public void onStart() {
        super.onStart();
        getFriendsList();
    }

    private void getFriendsList(){

        mFriendsDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                friendsArrayList.clear();
                Long dataCount = new Long(dataSnapshot.getChildrenCount());
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    mUserDatabase.child(ds.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);
                            friendsArrayList.add(user);
                            if(friendsArrayList.size()==dataCount.intValue()){
                                friendsRecAdapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    //Recycler Adapter For Select Friends List

    public static class FriendsRecAdapter extends RecyclerView.Adapter<FriendsRecAdapter.FriendsViewHolder>  {

        private ArrayList<User> FriendsArrayList;
        private Context context;


        interface OnItemCheckListener{
            void onItemCheck(User user);
            void onItemUnCheck(User user);
        }

        public FriendsRecAdapter(ArrayList<User> FriendsArrayList, Context context,@NonNull OnItemCheckListener onItemCheckListener) {
            this.FriendsArrayList = FriendsArrayList;
            this.context = context;
            this.onItemClick = onItemCheckListener;
        }

        @NonNull
        @Override
        public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.select_friends_list_item,parent,false);

            return new FriendsViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull FriendsViewHolder holder, int position) {
            holder.userNameView.setText(FriendsArrayList.get(position).getUserName());
            if(!FriendsArrayList.get(position).getPhotoUrl().equals("")){
                Glide.with(context).load(FriendsArrayList.get(position).getPhotoUrl()).into(holder.userProfileView);
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.userItemCheckbox.setChecked(!holder.userItemCheckbox.isChecked());
                    if(holder.userItemCheckbox.isChecked()){
                        onItemClick.onItemCheck(FriendsArrayList.get(position));
                    }
                    else {
                        onItemClick.onItemUnCheck(FriendsArrayList.get(position));
                    }
                }
            });

        }

        @NonNull
        private OnItemCheckListener onItemClick;

        @Override
        public int getItemCount() {
            return FriendsArrayList.size();
        }

        public class FriendsViewHolder extends RecyclerView.ViewHolder{

            TextView userNameView;
            ImageView userProfileView;
            CheckBox userItemCheckbox;

            public FriendsViewHolder(@NonNull View itemView) {
                super(itemView);
                userNameView = itemView.findViewById(R.id.sf_user_item_name);
                userProfileView = itemView.findViewById(R.id.sf_user_item_icon);
                userItemCheckbox = itemView.findViewById(R.id.sf_user_item_checkbox);

            }
        }


    }

}
