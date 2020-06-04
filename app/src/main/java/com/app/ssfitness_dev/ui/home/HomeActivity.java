package com.app.ssfitness_dev.ui.home;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.ssfitness_dev.R;
import com.app.ssfitness_dev.data.models.User;
import com.app.ssfitness_dev.ui.authentication.login.LoginActivity;
import com.app.ssfitness_dev.ui.home.nutrition.NutritionActivity;
import com.app.ssfitness_dev.ui.home.nutrition.NutritionFragment;
import com.app.ssfitness_dev.ui.home.profile.ProfileSettingsActivity;
import com.app.ssfitness_dev.ui.user.userprofile.UserProfile;
import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.app.ssfitness_dev.utilities.Constants.USER;

public class HomeActivity extends AppCompatActivity implements FirebaseAuth.AuthStateListener, MenuItem.OnMenuItemClickListener {

    private static final String TAG = "HOME ACTIVITY";
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference mUserRef;
    private DatabaseReference mRootRef;
    private GoogleSignInClient googleSignInClient;
    NavController navController;
    BottomNavigationView bottomNavigationView;
    MaterialToolbar mainToolbar;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    Dialog aboutDialog;
    private LinearLayout view_stub;
    private static Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        HomeActivity.context = getApplicationContext();

        mRootRef = FirebaseDatabase.getInstance().getReference();

        // To check for Offline and online status
        if(mAuth.getCurrentUser()!=null){
            mUserRef = mRootRef.child("users").child(mAuth.getCurrentUser().getUid());
        }

        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        bottomNavigationView = findViewById(R.id.bottom_nav);
        mainToolbar = findViewById(R.id.toolbar);

        view_stub = findViewById(R.id.root_layout_home);
        mDrawerLayout = findViewById(R.id.drawer);
        NavigationView navigation_view = findViewById(R.id.navigation_view);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, 0, 0);
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        //Setting up drawer layout
        Menu drawerMenu = navigation_view.getMenu();
        for (int i = 0; i < drawerMenu.size(); i++) {
            drawerMenu.getItem(i).setOnMenuItemClickListener(this);
        }

        //Setting up header
        View header = navigation_view.getHeaderView(0);
        TextView name = header.findViewById(R.id.user_name);
        TextView email = header.findViewById(R.id.user_email);
        CircleImageView img = header.findViewById(R.id.profile_image);

        //Set Name, email and image from db
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        for (UserInfo userInfo : user.getProviderData()) {
            String providerId = userInfo.getProviderId();
            Log.d(TAG, "providerId = " + userInfo.getProviderId());
            if (providerId.equals("google.com")) {
                email.setText(user.getEmail());
                name.setText(user.getDisplayName());
                String photo = user.getPhotoUrl().toString();
                Glide.with(this).load(photo).into(img);

            } else {
                String shorty = mAuth.getCurrentUser().getEmail();
                String shorty2 = shorty.substring(0, shorty.indexOf("@"));
                name.setText(shorty2);
                email.setText(mAuth.getCurrentUser().getEmail());
            }
        }
        aboutDialog = new Dialog(this);
        setSupportActionBar(mainToolbar);
       NavigationUI.setupWithNavController(mainToolbar, navController);
       NavigationUI.setupWithNavController(bottomNavigationView, navController);

        initGoogleSignInClient();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        if (view_stub != null) {
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            View stubView = inflater.inflate(layoutResID, view_stub, false);
            view_stub.addView(stubView, lp);
        }

    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        if (view_stub != null) {
            ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            view_stub.addView(view, lp);
        }
    }

    @Override
    public void setContentView(View view, LayoutParams params) {
        super.setContentView(view, params);
        if (view_stub != null) {
            view_stub.addView(view, params);
        }
    }

    private void initGoogleSignInClient() {
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            goToAuthInActivity();
        }
    }

    private void goToAuthInActivity() {
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private boolean connected(){
        ConnectivityManager connectivityManager=(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo !=null && activeNetworkInfo.isConnected();
    }

    private void signOut() {
        Toast.makeText(context, "Logged out successfully!", Toast.LENGTH_SHORT).show();
        mAuth.signOut();
        googleSignInClient.signOut();
    }


    @Override
    protected void onStart() {
        super.onStart();
        if(connected()) {
            mAuth.addAuthStateListener(this);
                mRootRef.child("users").child(mAuth.getCurrentUser().getUid()).child("online").setValue("true");
        }
        else {
            goToAuthInActivity();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(this);

    }



   @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem menuItem) {
       return true;
    }


    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {

        switch (menuItem.getItemId()) {
            case R.id.workout:
              /*  Intent intent2 = new Intent(this, BuilderActivity.class);
                startActivity(intent2);*/
                Toast.makeText(this, "Work outs!", Toast.LENGTH_SHORT).show();
                mDrawerLayout.closeDrawers();
                break;
            case R.id.nutrition:
                Intent intent3 = new Intent(HomeActivity.this, NutritionActivity.class);
                startActivity(intent3);
                mDrawerLayout.closeDrawers();
                break;
            case R.id.progress:
               /* Intent intent4 = new Intent(this, ProgressActivity.class);
                startActivity(intent4);*/
                mDrawerLayout.closeDrawers();
                break;
            case R.id.info:
                mDrawerLayout.closeDrawers();
                break;
            case R.id.FAQ:
               /* Intent intent5 = new Intent(this, FAQActivity.class);
                startActivity(intent5);*/
                mDrawerLayout.closeDrawers();
                break;

            case R.id.settings:
                Intent intent = new Intent(HomeActivity.this, ProfileSettingsActivity.class);
                startActivity(intent);
                return true;

            case R.id.sign_out:
                signOut();
                finish();
                break;
        }
        return false;
    }
}
