package com.sproutleaf.android;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.jakewharton.rxbinding.widget.RxTextView;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import rx.functions.Action1;

public class EditPlantProfileActivity extends AppCompatActivity {
    private String mPlantKey;
    private TextView mPlantNameTextView;
    private TextView mPlantSpeciesTextView;
    private TextView mPlantBirthdayTextView;
    private TextView mPlantNotesTextView;
    private ImageView mPlantImageView;
    private ImageView mPlantProfileSaveButton;

    private androidx.appcompat.widget.Toolbar mToolbar;

    private boolean mPlantSpeciesTextViewFocus;
    private boolean mPlantNotesTextViewFocus;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseReference;
    private DatabaseReference mDatabasePlantsReference;
    private FirebaseStorage mStorage;
    private StorageReference mStorageReference;
    private StorageReference mStoragePlantProfileImagesReference;
    private StorageReference mStorageUploadedPlantProfileImageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_plant_profile);

        // Initialize Firebase variables
        mAuth = FirebaseAuth.getInstance();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mDatabasePlantsReference = FirebaseDatabase.getInstance().getReference().child("plants");
        mStorage = FirebaseStorage.getInstance();
        mStorageReference = mStorage.getReference();
        mStoragePlantProfileImagesReference = mStorageReference.child("user").child(mAuth.getCurrentUser().getUid()).child("plant-profile-images");

        // Set member variables
        mToolbar = findViewById(R.id.edit_plant_profile_toolbar);
        mPlantNameTextView = findViewById(R.id.edit_plant_name);
        mPlantSpeciesTextView = findViewById(R.id.edit_plant_species);
        mPlantBirthdayTextView = findViewById(R.id.edit_plant_birthday);
        mPlantNotesTextView = findViewById(R.id.edit_plant_notes);
        mPlantImageView = findViewById(R.id.edit_plant_image);
        mPlantProfileSaveButton = findViewById(R.id.edit_plant_profile_save_button);

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

                mPlantNameTextView.setText(dataSnapshot.child("plantName").getValue(String.class));
                mPlantSpeciesTextView.setText(dataSnapshot.child("plantSpecies").getValue(String.class));

                // Set value of plantBirthday if set
                if (!dataSnapshot.child("plantBirthday").getValue(String.class).equals("")) {
                    mPlantBirthdayTextView.setText(String.format(getString(R.string.plant_card_birthday), dataSnapshot.child("plantBirthday").getValue(String.class)));
                }

                // Set value of plantNotes if exists
                if (dataSnapshot.hasChild("plantNotes")) {
                    if (!dataSnapshot.child("plantNotes").getValue(String.class).equals("")) {
                        mPlantNotesTextView.setText(dataSnapshot.child("plantNotes").getValue(String.class));
                    }
                }

                mStorageUploadedPlantProfileImageReference = mStoragePlantProfileImagesReference.child(mPlantKey + ".jpg");
                final long ONE_MEGABYTE = 1024 * 1024;
                mStorageUploadedPlantProfileImageReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        mPlantImageView.setImageBitmap(bitmap);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
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

        // EditText listeners to display the save edits button
        mPlantSpeciesTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(getCurrentFocus() == mPlantSpeciesTextView) {
                    mPlantProfileSaveButton.setVisibility(View.VISIBLE);
                }
            }
        });

        mPlantNotesTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(getCurrentFocus() == mPlantNotesTextView) {
                    mPlantProfileSaveButton.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    // Bug after editing plant that pressing back makes multiple random plants
    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        Intent intent = new Intent(this, JournalActivity.class);
        startActivity(intent);
        finish();

    }

    // Update plant profile on Firebase database
    public void updatePlantProfile(View view) {
        mDatabaseReference.child("plants").child(mPlantKey).child("plantSpecies").setValue(mPlantSpeciesTextView.getText().toString());
        mDatabaseReference.child("plants").child(mPlantKey).child("plantNotes").setValue(mPlantNotesTextView.getText().toString());

        Intent intent = new Intent(this, JournalActivity.class);
        startActivity(intent);
        finish();
    }
}
