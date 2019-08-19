package com.sproutleaf.android;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PlantProfileActivity extends AppCompatActivity {
    private String mPlantKey;
    private androidx.appcompat.widget.Toolbar mToolbar;

    private DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plant_profile);

        // Initialize Firebase variables
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        // Set member variables
        mToolbar = findViewById(R.id.plant_profile_toolbar);
    }

    @Override
    public void onStart() {
        super.onStart();

        // Get path intent and set image view to image
        Intent intent = getIntent();
        mPlantKey = intent.getStringExtra("plantKey");
        mDatabaseReference.child("plants").child(mPlantKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (Character.toLowerCase(dataSnapshot.child("plantName").getValue(String.class).charAt(dataSnapshot.child("plantName").getValue(String.class).length() - 1)) == 's') {
                    mToolbar.setTitle(String.format(getString(R.string.plant_profile_toolbar_text), (dataSnapshot.child("plantName").getValue(String.class) + "'")));
                }
                else {
                    mToolbar.setTitle(String.format(getString(R.string.plant_profile_toolbar_text), (dataSnapshot.child("plantName").getValue(String.class) + "'s")));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // Toolbar config
        mToolbar.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.darkerGrey, null));
        mToolbar.setTitleTextColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimaryDark, null));
        mToolbar.setTitleTextAppearance(this, R.style.ToolbarTextAppearance);
        setSupportActionBar(mToolbar); // Set mToolbar as Action Bar

    }
}
