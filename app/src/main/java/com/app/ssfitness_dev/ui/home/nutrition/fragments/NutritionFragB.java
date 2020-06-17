package com.app.ssfitness_dev.ui.home.nutrition.fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
import java.util.List;

import belka.us.androidtoggleswitch.widgets.ToggleSwitch;


public class NutritionFragB extends Fragment {

    DatabaseReference mUserDatabase;
    String gender;
    Long height;
    Long weight;
    int age;
    String current_user;
    String goal;
    String intensity;
    String days;
    int tdee = 0;
    int cals = 0;
    double wt = 0;
    int protein = 0;
    int carbs = 0;
    int fats = 0;
    int bmr = 0;
    double weightz;


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
                   // age = dataSnapshot.child("age").getValue().toString();
                    age = 25;
                    goal = dataSnapshot.child("goal").getValue(String.class);
                    intensity = dataSnapshot.child("activitylevel").getValue(String.class);

                    //For male and femlae BMR
                    if(gender.equals("female")){
                        bmr = BMRcalcKG(age, height, weight, false);
                        weightz = weight;
                    }
                    else
                    {
                        bmr = BMRcalcKG(age, height, weight, true);
                        weightz = weight;
                    }
                    //bind ui
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        final TextView tdd = getView().findViewById(R.id.tdee);
        final TextView p = getView().findViewById(R.id.cal_protein);
        final TextView c = getView().findViewById(R.id.cal_carb);
        final TextView f = getView().findViewById(R.id.cal_fat);
        final TextView gp = getView().findViewById(R.id.gram_protein);
        final TextView gc = getView().findViewById(R.id.gram_carb);
        final TextView gf = getView().findViewById(R.id.gram_fat);
        final PieChart pie = getView().findViewById(R.id.chart);
        TextView td = getView().findViewById(R.id.tdee2);

        //Store in strings

        tdee = tdeeTest(bmr, 2, 3, 7);

        ToggleSwitch ts = getView().findViewById(R.id.goal_switch);
        td.setText(String.valueOf(tdee));
        ts.setCheckedTogglePosition(1);
        tdd.setText(String.valueOf(tdee));

        cals = tdee;

        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry((int) (wt * 1.8), "Protein"));
        entries.add(new PieEntry((int) (cals - cals * 0.3 - wt * 1.8 * 4) / 4, "Carb"));
        entries.add(new PieEntry((int) (cals * 0.3 / 9), "Fat"));

        PieDataSet set = new PieDataSet(entries, null);

        set.setColors(getResources().getColor(R.color.bluez), getResources().getColor(R.color.greenz), getResources().getColor(R.color.red));
        set.setSliceSpace(3f);
        set.setSelectionShift(9f);
        set.setValueFormatter(new PercentFormatter());

        PieData data = new PieData(set);

        pie.getDescription().setEnabled(false);
        pie.getLegend().setEnabled(false);
        pie.setUsePercentValues(true);
        pie.setHoleColor(getResources().getColor(R.color.colorGrey));
        pie.setHoleRadius(35f);
        pie.setData(data);
        pie.spin(500, 0, -360f, Easing.EasingOption.EaseInOutQuad);

        p.setText(String.valueOf(((int) (wt * 1.8 * 4))));
        f.setText(String.valueOf(((int) (cals * 0.3))));
        c.setText(String.valueOf(cals - ((int) (wt * 1.8 * 4)) - ((int) (cals * 0.3))));
        gp.setText(String.valueOf(((int) (wt * 1.8))));
        gf.setText(String.valueOf(((int) (cals * 0.3 / 9))));
        gc.setText(String.valueOf((int) (cals - (wt * 1.8 * 4) - (cals * 0.3)) / 4));


        ts.setOnToggleSwitchChangeListener(new ToggleSwitch.OnToggleSwitchChangeListener() {
            @Override
            public void onToggleSwitchChangeListener(int position, boolean isChecked) {
                switch (position) {
                    case 0:
                        cals = tdee - 250;
                        protein = ((int) (wt * 2.1 * 4));
                        fats = ((int) (wt * 9));

                        break;
                    case 2:
                        cals = tdee + 200;
                        protein = ((int) (wt * 1.8 * 4));
                        fats = ((int) (cals * 0.25));
                        break;
                    default:
                        cals = tdee;
                        protein = ((int) (wt * 1.8 * 4));
                        fats = ((int) (cals * 0.3));
                        break;
                }
               //goal = cals;
                carbs = cals - protein - fats;
                tdd.setText(String.valueOf(cals));
                p.setText(String.valueOf(protein));
                f.setText(String.valueOf(fats));
                c.setText(String.valueOf(carbs));
                gp.setText(String.valueOf(protein / 4));
                gf.setText(String.valueOf(fats / 9));
                gc.setText(String.valueOf(carbs / 4));
                List<PieEntry> entries = new ArrayList<>();
                entries.add(new PieEntry(protein / 4, "Protein"));
                entries.add(new PieEntry(carbs / 4, "Carb"));
                entries.add(new PieEntry(fats / 9, "Fat"));
                PieDataSet set = new PieDataSet(entries, null);
                set.setColors(getResources().getColor(R.color.bluez), getResources().getColor(R.color.greenz), getResources().getColor(R.color.red));
                set.setSliceSpace(3f);
                set.setSelectionShift(9f);
                set.setValueFormatter(new PercentFormatter());
                PieData data = new PieData(set);
                pie.getDescription().setEnabled(false);
                pie.getLegend().setEnabled(false);
                pie.setUsePercentValues(true);
                pie.setHoleColor(getResources().getColor(R.color.colorGrey));
                pie.setHoleRadius(35f);
                pie.setData(data);
                pie.notifyDataSetChanged();
                pie.invalidate();
            }
        });
    }


    private int tdeeTest(int bmr, int activ, int intense, int seeki) {
        double active2, intense2, seeki2;
        switch (activ) {
            case 0:
                active2 = 1.2;
                break;
            case 1:
                active2 = 1.3;
                break;
            default:
                active2 = 1.75;
                break;
        }
        switch (intense) {
            case 0:
                intense2 = active2 + 0.05;
                break;
            case 1:
                intense2 = active2 + 0.1;
                break;
            default:
                intense2 = active2 + 0.15;
                break;
        }
        seeki2 = seeki * 0.01 + intense2;

        return ((int) (bmr * seeki2));
    }


    public int BMRcalcKG(int age, long height, double weight, boolean gender) {
        if (gender) {
            return (int) ((10 * weight) + (6.25 * height) - (5 * age) + 5);
        } else {
            return (int) ((10 * weight) + (6.25 * height) - (5 * age) - 161);
        }
    }

    public int BMRcalcLB(int age, int height, int weight, boolean gender) {
        if (gender) {
            return (int) ((10 * weight / 2.2) + (6.25 * height * 2.54) - (5 * age) + 5);
        } else {
            return (int) ((10 * weight / 2.2) + (6.25 * height * 2.54) - (5 * age) - 161);
        }
    }


    //Give option to change the height, weight


        //Goes to other fragment

        //Get the calories details and show to the person in graph

}