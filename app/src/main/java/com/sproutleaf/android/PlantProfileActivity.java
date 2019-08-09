package com.sproutleaf.android;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

public class PlantProfileActivity extends AppCompatActivity {
    private String mPlantKey;
    private androidx.appcompat.widget.Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_profile);

        // Set member variables
        mToolbar = findViewById(R.id.plant_profile_toolbar);
    }

    @Override
    public void onStart() {
        super.onStart();

        // Get path intent and set image view to image
        Intent intent = getIntent();
        mPlantKey = intent.getStringExtra("plantKey");

        // Toolbar config
        mToolbar.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.darkerGrey, null));
        mToolbar.setTitleTextColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimaryDark, null));
        mToolbar.setTitle(mPlantKey);
        mToolbar.setTitleTextAppearance(this, R.style.ToolbarTextAppearance);
        setSupportActionBar(mToolbar); // Set mToolbar as Action Bar

    }
}
