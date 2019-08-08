package com.sproutleaf.android;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import  android.view.ContextThemeWrapper;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class JournalActivity extends AppCompatActivity {
    private static final String TAG = JournalActivity.class.getName();
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseReference;
    private Context mContext;
    private androidx.appcompat.widget.Toolbar mToolbar;
    private LinearLayout mLinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal);

        // Initialize Firebase variables
        mAuth = FirebaseAuth.getInstance();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        // Set member variables
        mContext = getApplicationContext();
        mLinearLayout = (LinearLayout) findViewById(R.id.LinearLayout);
        mToolbar = findViewById(R.id.journal_toolbar);
    }

    @Override
    public void onStart() {
        super.onStart();
        final FirebaseUser currentUser = mAuth.getCurrentUser();
        String displayName = currentUser.getDisplayName();

        // Toolbar config
        mToolbar.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.darkerGrey, null));
        mToolbar.setTitleTextColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimaryDark, null));
        mToolbar.setTitle(String.format(getString(R.string.journal_toolbar_text), displayName));
        mToolbar.setTitleTextAppearance(this, R.style.ToolbarTextAppearance);
        setSupportActionBar(mToolbar); // Set mToolbar as Action Bar

        // If change in plant database is detected, check if there was a new plant created by current user
        mDatabaseReference.child("plants").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot plantSnapshot : dataSnapshot.getChildren()) {
                    // Parse the snapshot to local model
                    Plant plant = plantSnapshot.getValue(Plant.class);

                    // Check if plant card is already in list
                    boolean alreadyInList = false;
                    final int childCount = mLinearLayout.getChildCount();
                    for (int i = 0; i < childCount; i++) {
                        View view = mLinearLayout.getChildAt(i);
                        if (plantSnapshot.getKey() == view.getTag()) {
                            alreadyInList = true;
                        }
                    }

                    if (plant.uid.equals(currentUser.getUid()) && !alreadyInList) {
                        // Initialize a new CardView
                        CardView plantCard = new CardView(new ContextThemeWrapper(JournalActivity.this, R.style.PlantCardViewAppearance), null, 0);
                        // Set tag to check if already in list
                        plantCard.setTag(plantSnapshot.getKey());
                        LinearLayout plantCardInner = new LinearLayout(new ContextThemeWrapper(JournalActivity.this, R.style.PlantInnerCardViewAppearance));
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        int margin = 8;
                        params.setMargins(margin, margin, margin, margin);
                        plantCard.setLayoutParams(params);

                        // Initialize a new TextView and put in CardView
                        TextView plantNameView = new TextView(mContext);
                        plantNameView.setText(plant.plantName);
                        TextViewCompat.setTextAppearance(plantNameView, R.style.PlantNameTextViewAppearance);
                        plantCardInner.addView(plantNameView);

                        // Initialize a new TextView and put in CardView
                        if (plant.plantSpecies.length() > 0) {
                            TextView plantSpeciesView = new TextView(mContext);
                            plantSpeciesView.setText(plant.plantSpecies);
                            TextViewCompat.setTextAppearance(plantSpeciesView, R.style.PlantSpeciesTextViewAppearance);
                            plantCardInner.addView(plantSpeciesView);
                        }

                        // Initialize a new TextView and put in CardView
                        if (plant.plantSpecies.length() > 0) {
                            // Initialize a new TextView and put in CardView
                            TextView plantBirthdayView = new TextView(mContext);
                            plantBirthdayView.setText(String.format(getString(R.string.plant_card_birthday), plant.plantBirthday));
                            TextViewCompat.setTextAppearance(plantBirthdayView, R.style.PlantBirthdayTextViewAppearance);
                            plantCardInner.addView(plantBirthdayView);
                        }

                        plantCard.addView(plantCardInner);
                        // Finally, add the CardView in root layout
                        mLinearLayout.addView(plantCard);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "onCancelled", databaseError.toException());
            }
        });


    }

    // Create a new plant
    public void showNewPlantDialog(View view) {
        FragmentManager fm = getSupportFragmentManager();
        CreatePlantDialogFragment createPlantDialogFragment = CreatePlantDialogFragment.newInstance("New Plant Profile");
        createPlantDialogFragment.show(fm, "fragment_create_plant");
    }

}
