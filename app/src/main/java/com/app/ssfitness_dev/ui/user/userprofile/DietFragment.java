package com.app.ssfitness_dev.ui.user.userprofile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavArgument;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.app.ssfitness_dev.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import static com.app.ssfitness_dev.utilities.Constants.TAG_GOAL_FRAGMENT;
import static com.app.ssfitness_dev.utilities.Constants.USERS;
import static com.app.ssfitness_dev.utilities.HelperClass.logMessage;

/**
 * A simple {@link Fragment} subclass.
 */
public class DietFragment extends Fragment {


    private Button continueToFinish;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore rootRef;
    MaterialToolbar materialToolbar;
    private CollectionReference usersRef;
    private NavController navController;
    private RadioGroup radioGroup;
    private RadioButton radioButton1, radioButton2, radioButton3, radioButton4,radioButton5,radioButton6;
    private String answer;
    private NavArgument argument;


    public DietFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_diet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initOptions(view);
        saveToDb();


    }


    private void saveToDb() {
        continueToFinish.setOnClickListener(view1 -> {

            rootRef = FirebaseFirestore.getInstance();
            usersRef = rootRef.collection(USERS);

            String goal = getArguments().getString("goal");
            String activitylevel = getArguments().getString("activitylevel");

            Map<String, Object> user = new HashMap<>();

            final Bundle bdl = new Bundle();

            bdl.putString("goal", goal);
            bdl.putString("activitylevel", activitylevel);
            bdl.putString("diet", answer);

            user.put("diet",answer);

            logMessage(TAG_GOAL_FRAGMENT, goal + activitylevel + answer);
            navController.navigate(R.id.action_dietFragment_to_userProfileFragment,bdl);
        });
    }

    private void initOptions(View view) {
        continueToFinish = view.findViewById(R.id.button_continue_to_finish);
        firebaseAuth = FirebaseAuth.getInstance();
        navController = Navigation.findNavController(view);


        radioGroup = getView().findViewById(R.id.radio_group_diet);


        radioButton1 = getView().findViewById(R.id.radio_ultra_low);
        radioButton2 = getView().findViewById(R.id.radio_classic_ketogenic);
        radioButton3 = getView().findViewById(R.id.radio_performance_ketogenic);
        radioButton4 = getView().findViewById(R.id.radio_moderate);
        radioButton5 = getView().findViewById(R.id.radio_high_carb);
        radioButton6 = getView().findViewById(R.id.radio_balanced);


        materialToolbar = view.findViewById(R.id.topAppBar);

        materialToolbar.setNavigationOnClickListener(view1 -> navController.navigate(R.id.action_dietFragment_to_activeLevelFragment));


        radioGroup.setOnCheckedChangeListener((radioGroup, checkId) -> {
            continueToFinish.setVisibility(View.VISIBLE);
            if(radioButton1.isChecked()){
                radioButton1.setBackgroundResource(R.drawable.primary_bg);
                answer = radioButton1.getText().toString();
            }
            else
            {
                radioButton1.setBackgroundResource(R.drawable.outline_btn_bg);
            }

            if(radioButton2.isChecked()){
                radioButton2.setBackgroundResource(R.drawable.primary_bg);
                answer = radioButton2.getText().toString();
            }
            else
            {
                radioButton2.setBackgroundResource(R.drawable.outline_btn_bg);

            }

            if(radioButton3.isChecked()){
                radioButton3.setBackgroundResource(R.drawable.primary_bg);
                answer = radioButton3.getText().toString();
            }
            else
            {
                radioButton3.setBackgroundResource(R.drawable.outline_btn_bg);
            }

            if(radioButton4.isChecked()){
                radioButton4.setBackgroundResource(R.drawable.primary_bg);
                answer = radioButton4.getText().toString();
            }
            else
            {
                radioButton4.setBackgroundResource(R.drawable.outline_btn_bg);
            }

            if(radioButton5.isChecked()){
                radioButton5.setBackgroundResource(R.drawable.primary_bg);
                answer = radioButton5.getText().toString();
            }
            else
            {
                radioButton5.setBackgroundResource(R.drawable.outline_btn_bg);
            }

            if(radioButton6.isChecked()){
                radioButton6.setBackgroundResource(R.drawable.primary_bg);
                answer = radioButton6.getText().toString();
            }
            else
            {
                radioButton6.setBackgroundResource(R.drawable.outline_btn_bg);
            }
        });
    }
}
