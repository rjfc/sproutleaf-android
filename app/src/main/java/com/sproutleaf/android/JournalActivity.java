package com.sproutleaf.android;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class JournalActivity extends AppCompatActivity{
    private static final String TAG = JournalActivity.class.getName();
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseReference;
    private androidx.appcompat.widget.Toolbar mToolbar;
    private ViewPager mViewPager;
    private Context mContext;

    private CardPagerAdapter mCardAdapter;
    private ShadowTransformer mCardShadowTransformer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal);

        // Initialize Firebase variables
        mAuth = FirebaseAuth.getInstance();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        // Set member variables
        mContext = getApplicationContext();
        mToolbar = findViewById(R.id.journal_toolbar);
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mCardAdapter = new CardPagerAdapter();
        mCardShadowTransformer = new ShadowTransformer(mViewPager, mCardAdapter);
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

        // ViewPager config
        mViewPager.setAdapter(mCardAdapter);
        mViewPager.setPageTransformer(false, mCardShadowTransformer);
        mViewPager.setOffscreenPageLimit(100); // TODO: set limit on how many plants a user can make

        // TabLayout config
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabDots);
        tabLayout.setupWithViewPager(mViewPager, true);

        // If change in plant database is detected, check if there was a new plant created by current user
        mDatabaseReference.child("plants").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

               for (final DataSnapshot plantSnapshot : dataSnapshot.getChildren()) {
                    // Parse the snapshot to local model
                    Plant plant = plantSnapshot.getValue(Plant.class);
                    // Check if plant card is already in list
                    boolean alreadyInList = false;
                    final int childCount = mViewPager.getChildCount();
                    for (int i = 0; i < childCount; i++) {
                        View view = mViewPager.getChildAt(i);
                        Log.d("plant", "tag:" + (String)view.getTag());
                        if (plantSnapshot.getKey().equals((String)view.getTag())) {
                            alreadyInList = true;
                            break;
                        }
                    }
                    if (plant.getUserID().equals(currentUser.getUid()) && !alreadyInList) {
                        // Initialize a new PlantCardItem (separate object from PlantCard because we need to get the plant ID to prevent multiple of the same plant profile cards from appearing)
                        mCardAdapter.addCardItem(new PlantCardItem(plant.getPlantName(), plant.getPlantSpecies(), String.format(getString(R.string.plant_card_birthday), plant.getPlantBirthday()), currentUser.getUid(), plantSnapshot.getKey()));
                        mCardAdapter.notifyDataSetChanged();
                        // If plantCard clicked
                     /*   plantCard.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Send key as intent
                                Intent intent = new Intent(mContext, PlantProfileActivity.class);
                                intent.putExtra("capturedPhotoPath", plantSnapshot.getKey());
                                startActivity(intent);
                            }
                        });*/
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