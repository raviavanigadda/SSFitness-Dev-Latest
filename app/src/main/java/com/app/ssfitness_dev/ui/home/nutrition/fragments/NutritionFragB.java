package com.app.ssfitness_dev.ui.home.nutrition.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.app.ssfitness_dev.R;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class NutritionFragB extends Fragment {

    DatabaseReference mUserDatabase;
    String gender;
    Long height;
    Long weight;
    int age;
    String current_user;
    String goal;
    String ActivityLevel;
    String diet_preference;
    int CURRENT_TDEE = 0,GOAL_TDEE = 0;
    double BMR = 0;
    int [] PCF_Cal_Array = new int[3], PCF_Gram_Array = new int[3];
    String [] PCF_Label = {"Protein","Carbs","Fat"};

    private TextView current_tdee_value_txt, required_tdee_value_txt,protein_txt,carb_txt,fat_txt;
    private PieChart pieChart;


    public NutritionFragB() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_nutrition_frag_b, container, false);
    }

    public static NutritionFragB newInstance() {
        return new NutritionFragB();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        current_tdee_value_txt = view.findViewById(R.id.current_tdee_value_txt);
        required_tdee_value_txt = view.findViewById(R.id.required_tdee_value_txt);
        pieChart = view.findViewById(R.id.pieChart);
        protein_txt = view.findViewById(R.id.protein_txt);
        carb_txt = view.findViewById(R.id.carb_txt);
        fat_txt = view.findViewById(R.id.fat_txt);

        current_user = FirebaseAuth.getInstance().getCurrentUser().getUid();

        //Database reference
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(current_user);

        mUserDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists())
                {
                    //Get gender, height, weight, age from database
                    gender = dataSnapshot.child("gender").getValue(String.class);
                    weight = dataSnapshot.child("weight").getValue(Long.class);
                    height = dataSnapshot.child("height").getValue(Long.class);

                    age = dataSnapshot.child("age").getValue(int.class);
                    goal = dataSnapshot.child("goal").getValue(String.class);
                    ActivityLevel = dataSnapshot.child("activitylevel").getValue(String.class);
                    diet_preference = dataSnapshot.child("diet").getValue(String.class);
                    //For male and femlae BMR
                    if(gender.equals("female")){
                        BMR = BMRcalcKG(age, height, weight, false);
                    }
                    else
                    {
                        BMR = BMRcalcKG(age, height, weight, true);
                    }
                    CURRENT_TDEE = CalcTDEE(BMR,ActivityLevel);
                    GOAL_TDEE = CalcGoalTDEE(CURRENT_TDEE,goal);
                    PCF_Cal_Array = CalcPCF(GOAL_TDEE,diet_preference);
                    PCF_Gram_Array = CalcGramPCF(PCF_Cal_Array);

                    current_tdee_value_txt.setText(CURRENT_TDEE+" Cal");
                    required_tdee_value_txt.setText(GOAL_TDEE+" Cal");
                    protein_txt.setText("Protein: "+PCF_Cal_Array[0]+" Cal / "+PCF_Gram_Array[0]+"g");
                    carb_txt.setText("Carbs: "+PCF_Cal_Array[1]+" Cal / "+PCF_Gram_Array[1]+"g");
                    fat_txt.setText("Fat: "+PCF_Cal_Array[2]+" Cal / "+PCF_Gram_Array[2]+"g");
                    setUpPieChart();
                    //bind ui
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setUpPieChart(){
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(true);
        pieChart.getLegend().setEnabled(false);
        pieChart.setDragDecelerationFrictionCoef(0.9f);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setHoleRadius(35f);
        pieChart.setTransparentCircleRadius(45f);
        pieChart.animateY(1000, Easing.EasingOption.EaseInOutCubic);
        
        ArrayList<PieEntry> entries = new ArrayList<>();
        for (int i=0;i<PCF_Cal_Array.length;i++){
            entries.add(new PieEntry(PCF_Cal_Array[i],PCF_Label[i]));
        }

        PieDataSet pieDataSet = new PieDataSet(entries,"");
        pieDataSet.setSliceSpace(3f);
        pieDataSet.setSelectionShift(5f);
        pieDataSet.setColors(getResources().getColor(R.color.bluez), getResources().getColor(R.color.greenz), getResources().getColor(R.color.red));
        pieDataSet.setValueFormatter(new PercentFormatter());

        PieData pieData = new PieData(pieDataSet);
        pieData.setValueTextSize(14f);
        pieData.setValueTextColor(Color.WHITE);
        pieChart.setData(pieData);
        pieChart.invalidate();
    }

    private int CalcTDEE(double BMR,String ActivityLevel){
        int TDEE = 0;
        switch (ActivityLevel){
            case "Not so much":
                TDEE = (int) (BMR * 1.2);
                break;
            case "Weekend Warrior":
                TDEE = (int) (BMR * 1.375);
                break;
            case "Moderately Active":
                TDEE = (int) (BMR * 1.55);
                break;
            case "Intensely Active":
                TDEE = (int) (BMR * 1.725);
                break;
            case "Very Intensely Active":
                TDEE = (int) (BMR * 1.9);
                break;
        }
        return TDEE;
    }

    private int CalcGoalTDEE(int CURRENT_TDEE,String goal){
        int GOAL_TDEE = 0;
        switch (goal){
            case "Lose Weight":
                GOAL_TDEE = (int) (CURRENT_TDEE - CURRENT_TDEE*0.15);
                break;
            case "Maintain Weight":
                GOAL_TDEE = CURRENT_TDEE;
                break;
            case "Increase Lean Body Mass":
                GOAL_TDEE = (int) (CURRENT_TDEE + CURRENT_TDEE*0.05);
                break;
            case "Postpartum Recovery":
                GOAL_TDEE = CURRENT_TDEE;
                break;
        }
        return GOAL_TDEE;
    }

    private int[] CalcPCF(int GOAL_TDEE,String diet_preference){
        int [] PCFArray = new int[3];
        switch (diet_preference){
            case "Ultra low fat / low protein":
                PCFArray[0] = (int) (GOAL_TDEE * 0.15);
                PCFArray[1] = (int) (GOAL_TDEE * 0.75);
                PCFArray[2] = (int) (GOAL_TDEE * 0.10);
                break;
            case "Classic Ketogenic":
                PCFArray[0] = (int) (GOAL_TDEE * 0.15);
                PCFArray[1] = (int) (GOAL_TDEE * 0.10);
                PCFArray[2] = (int) (GOAL_TDEE * 0.75);
                break;
            case "Performance Ketogenic":
                PCFArray[0] = (int) (GOAL_TDEE * 0.30);
                PCFArray[1] = (int) (GOAL_TDEE * 0.10);
                PCFArray[2] = (int) (GOAL_TDEE * 0.60);
                break;
            case "Moderate carb / high protein / low fat":
                PCFArray[0] = (int) (GOAL_TDEE * 0.40);
                PCFArray[1] = (int) (GOAL_TDEE * 0.40);
                PCFArray[2] = (int) (GOAL_TDEE * 0.20);
                break;
            case "High carbohydrate / modern protein":
                PCFArray[0] = (int) (GOAL_TDEE * 0.25);
                PCFArray[1] = (int) (GOAL_TDEE * 0.55);
                PCFArray[2] = (int) (GOAL_TDEE * 0.20);
                break;
            case "Balanced":
                PCFArray[0] = (int) (GOAL_TDEE * 0.30);
                PCFArray[1] = (int) (GOAL_TDEE * 0.35);
                PCFArray[2] = (int) (GOAL_TDEE * 0.35);
                break;
        }
        return PCFArray;
    }

    private int[] CalcGramPCF(int[] PCF_Cal_Array){
        int[] PCF_Gram_Array = new int[3];

        PCF_Gram_Array[0] = PCF_Cal_Array[0]/4;
        PCF_Gram_Array[1] = PCF_Cal_Array[1]/4;
        PCF_Gram_Array[2] = PCF_Cal_Array[2]/9;

        return PCF_Gram_Array;
    }


    public double BMRcalcKG(int age, long height, double weight, boolean gender) {
        if (gender) {
            return (double) ((10 * weight) + (6.25 * height) - (5 * age) + 5);
        } else {
            return (double) ((10 * weight) + (6.25 * height) - (5 * age) - 161);
        }
    }

    public long BMRcalcLB(int age, int height, int weight, boolean gender) {
        if (gender) {
            return (long) ((10 * weight / 2.2) + (6.25 * height * 2.54) - (5 * age) + 5);
        } else {
            return (long) ((10 * weight / 2.2) + (6.25 * height * 2.54) - (5 * age) - 161);
        }
    }


    //Give option to change the height, weight


        //Goes to other fragment

        //Get the calories details and show to the person in graph

}