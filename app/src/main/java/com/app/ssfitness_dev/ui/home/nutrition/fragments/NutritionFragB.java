package com.app.ssfitness_dev.ui.home.nutrition.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.app.ssfitness_dev.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class NutritionFragB extends Fragment {

    DatabaseReference mUserDatabase;


    public NutritionFragB() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        return inflater.inflate(R.layout.fragment_nutrition_frag_b, container, false);
    }

    public static NutritionFragB newInstance() {
        return new NutritionFragB();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Get gender, height, weight, age from database


        //Store in strings

        //Give option to change the height, weight
        //Goes to other fragment

        //Get the calories details and show to the person in graph

    }
}