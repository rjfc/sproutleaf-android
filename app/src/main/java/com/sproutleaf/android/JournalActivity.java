package com.sproutleaf.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class JournalActivity extends AppCompatActivity implements LoadingPlantProfilesSpinnerInterface {
    private static final String TAG = JournalActivity.class.getName();
    private boolean alreadyInList;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseReference;
    private androidx.appcompat.widget.Toolbar mToolbar;
    private ImageView mDeletePlantProfileImageView;
    private static ViewPager mViewPager;
    private FirebaseStorage mStorage;
    private StorageReference mStorageReference;
    private StorageReference mStoragePlantProfileImagesReference;
    private ChildEventListener plantChildEventListener;
    private Fragment mCreatePlantDialogFragment;
    private LoadingPlantProfilesSpinnerFragment mLoadingPlantProfilesDialog;
    private Context mContext;
    private boolean imageFound;

    private static CardPagerAdapter mCardAdapter;
    private ShadowTransformer mCardShadowTransformer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal);

        // Initialize Firebase variables
        mAuth = FirebaseAuth.getInstance();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance();
        mStorageReference = mStorage.getReference();
        mStoragePlantProfileImagesReference = mStorageReference.child("user").child(mAuth.getCurrentUser().getUid()).child("plant-profile-images");

        // Set member variables
        mContext = getApplicationContext();
        mToolbar = findViewById(R.id.journal_toolbar);
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mDeletePlantProfileImageView = findViewById(R.id.delete_plant_profile_button);

        mCardAdapter = new CardPagerAdapter(this);
        mViewPager.setAdapter(mCardAdapter);

        if (savedInstanceState != null) {
            //Restore the fragment's instance
            mCreatePlantDialogFragment = getSupportFragmentManager().getFragment(savedInstanceState, "createPlantDialogFragment");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        final FirebaseUser currentUser = mAuth.getCurrentUser();
        String displayName = currentUser.getDisplayName();

        // Initialize CardPagerAdapter every time activity is started
        mCardShadowTransformer = new ShadowTransformer(mViewPager, mCardAdapter);

        // Toolbar config
        mToolbar.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.darkerGrey, null));
        mToolbar.setTitleTextColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimaryDark, null));
        mToolbar.setTitle(String.format(getString(R.string.journal_toolbar_text), displayName));
        mToolbar.setTitleTextAppearance(this, R.style.ToolbarTextAppearance);
        setSupportActionBar(mToolbar); // Set mToolbar as Action Bar

        // ViewPager config
        mViewPager.setPageTransformer(false, mCardShadowTransformer);
        mViewPager.setOffscreenPageLimit(100); // TODO: set limit on how many plants a user can make

        // Tab Dots layout
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabDots);
        tabLayout.setupWithViewPager(mViewPager, true);

        //showLoadingPlantProfilesDialog();

        // TabLayout config
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                TabLayout tabLayout = (TabLayout) findViewById(R.id.tabDots);
                tabLayout.setupWithViewPager(mViewPager, true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        // On page load
        mDatabaseReference.child("plants").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (final DataSnapshot plantSnapshot : dataSnapshot.getChildren()) {
                    // Parse the snapshot to local model
                    Plant plant = plantSnapshot.getValue(Plant.class);
                    if (plant.getUserID().equals(currentUser.getUid())) {
                        // Check if plant card is already in list
                        final int childCount = mViewPager.getChildCount();
                        alreadyInList = false;
                        for (int i = 0; i < childCount; i++) {
                            final View view = mViewPager.getChildAt(i);
                            if (plantSnapshot.getKey().equals((String) view.getTag())) {
                                alreadyInList = true;
                                break;
                            }
                        }

                        if (!alreadyInList) {
                            // Initialize a new PlantCardItem (separate object from PlantCard because we need to get the plant ID to prevent multiple of the same plant profile cards from appearing)
                            mCardAdapter.addCardItem(new PlantCardItem(plant.getPlantName(), plant.getPlantSpecies(), String.format(getString(R.string.plant_card_birthday), plant.getPlantBirthday()), currentUser.getUid(), plantSnapshot.getKey()));
                            mCardAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        // If change in plant database is detected, check if there was a new plant created by current user
        plantChildEventListener = mDatabaseReference.child("plants").addChildEventListener (new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                final Plant plant = dataSnapshot.getValue(Plant.class);
                if (plant.getUserID().equals(currentUser.getUid())) {
                    final int childCount = mViewPager.getChildCount();
                    alreadyInList = false;
                    for (int i = 0; i < childCount; i++) {
                        final View view = mViewPager.getChildAt(i);
                        if (dataSnapshot.getKey().equals((String) view.getTag())) {
                            alreadyInList = true;
                            break;
                        }
                    }

                    if (!alreadyInList) {
                        // Initialize a new PlantCardItem (separate object from PlantCard because we need to get the plant ID to prevent multiple of the same plant profile cards from appearing)
                        mCardAdapter.addCardItem(new PlantCardItem(plant.getPlantName(), plant.getPlantSpecies(), String.format(getString(R.string.plant_card_birthday), plant.getPlantBirthday()), currentUser.getUid(), dataSnapshot.getKey()));
                        mCardAdapter.notifyDataSetChanged();
                        removePlantChildEventListener();
                    }
                }
                recreate();
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                recreate();
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
           // hideLoadingPlantProfilesDialog(); // TODO: make this show after images loaded
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //Save the fragment's instance
        if (mCreatePlantDialogFragment != null) {
            getSupportFragmentManager().putFragment(outState, "createPlantDialogFragment", mCreatePlantDialogFragment);
        }
    }

    @Override
    protected void onPause() {
        //this should be before super
        super.onPause();
        removePlantChildEventListener();
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        mCardAdapter = new CardPagerAdapter(this);
        mViewPager.setAdapter(mCardAdapter);
    }

    // Remove listener for database plant children to make sure it only fires once per child
    private void removePlantChildEventListener () {
       // mDatabaseReference.child("plants").removeEventListener(plantChildEventListener);
    }

    // Create a new plant
    public void showNewPlantDialog(View view) {
        FragmentManager fm = getSupportFragmentManager();
        CreatePlantDialogFragment createPlantDialogFragment = CreatePlantDialogFragment.newInstance("New Plant Profile");
        createPlantDialogFragment.show(fm, "fragment_create_plant");
    }

    public void removePlantView(String plantID) {
        final int childCount = mViewPager.getChildCount();;
        for (int i = 0; i < childCount; i++) {
            final View view = mViewPager.getChildAt(i);
            if (plantID.equals((String) view.getTag())) {
                mCardAdapter.destroyItem(mViewPager, i, view);
                mCardAdapter.notifyDataSetChanged();
                break;
            }
        }
    }

    public void showLoadingPlantProfilesDialog() {
        FragmentManager fm = getSupportFragmentManager();
        mLoadingPlantProfilesDialog = LoadingPlantProfilesSpinnerFragment.newInstance("Loading plant profiles...");
        mLoadingPlantProfilesDialog.show(fm, "fragment_creating_plant_profile_spinner");
    }

    // Hide dialog
    public void hideLoadingPlantProfilesDialog() {
        mLoadingPlantProfilesDialog.dismissDialog();
    }

}