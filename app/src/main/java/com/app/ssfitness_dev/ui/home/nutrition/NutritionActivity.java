package com.app.ssfitness_dev.ui.home.nutrition;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;

import com.app.ssfitness_dev.R;
import com.app.ssfitness_dev.ui.home.HomeActivity;
import com.app.ssfitness_dev.ui.home.nutrition.fragments.NutritionFragB;
import com.google.android.material.appbar.MaterialToolbar;

public class NutritionActivity extends HomeActivity {

    MaterialToolbar materialToolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nutrition);

        materialToolbar = findViewById(R.id.nutToolbar);
        setSupportActionBar(materialToolbar);
        materialToolbar.setNavigationIcon(getResources()
                .getDrawable(R.drawable.ic_arrow_back));

        materialToolbar.setNavigationOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        NutritionFragB simpleFragmentB = NutritionFragB.newInstance();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content, simpleFragmentB)
                .commit();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.reset, menu);
        return super.onCreateOptionsMenu(menu);
    }


}