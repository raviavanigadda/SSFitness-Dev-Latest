package com.app.ssfitness_dev.ui.home.chat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.app.ssfitness_dev.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class CreateGroupActivity extends AppCompatActivity {


    private MaterialToolbar cg_topAppBar;
    private FirebaseUser mCurrentUser;
    private NavController CGNavController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        cg_topAppBar = findViewById(R.id.cg_topAppBar);
        setSupportActionBar(cg_topAppBar);
        CGNavController = Navigation.findNavController(this, R.id.cg_host_fragment);
        //NavigationUI.setupWithNavController(cg_topAppBar, CGNavController);

        cg_topAppBar.setNavigationOnClickListener(view -> {
            finish();
        });
    }
}
