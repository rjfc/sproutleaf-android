package com.sproutleaf.android;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.viewpager.widget.PagerAdapter;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import java.util.ArrayList;
import java.util.List;

public class CardPagerAdapter extends PagerAdapter implements CardAdapter {
    private static final String TAG = CardPagerAdapter.class.getName();

    private List<CardView> mViews;
    private List<PlantCardItem> mData;
    private float mBaseElevation;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseReference;
    private FirebaseStorage mStorage;
    private StorageReference mStorageReference;
    private StorageReference mStoragePlantProfileImagesReference;
    private StorageReference mStorageUploadedPlantProfileImageReference;

    public CardPagerAdapter() {
        mData = new ArrayList<>();
        mViews = new ArrayList<>();
    }

    public void addCardItem(PlantCardItem plant) {
        mViews.add(null);
        mData.add(plant);
    }

    public float getBaseElevation() {
        return mBaseElevation;
    }

    @Override
    public CardView getCardViewAt(int position) {
        return mViews.get(position);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        mAuth = FirebaseAuth.getInstance();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance();
        mStorageReference = mStorage.getReference();
        mStoragePlantProfileImagesReference = mStorageReference.child("user").child(mAuth.getCurrentUser().getUid()).child("plant-profile-images");

        View view = LayoutInflater.from(container.getContext())
                .inflate(R.layout.adapter, container, false);
        container.addView(view);
        bind(mData.get(position), view);
        CardView cardView = (CardView) view.findViewById(R.id.cardView);

        if (mBaseElevation == 0) {
            mBaseElevation = cardView.getCardElevation();
        }

        cardView.setMaxCardElevation(mBaseElevation * MAX_ELEVATION_FACTOR);
        mViews.set(position, cardView);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
        mViews.set(position, null);
        container.setSaveFromParentEnabled(false);
        notifyDataSetChanged();
    }

    private void bind(final PlantCardItem plant, final View view) {
        final FirebaseUser currentUser = mAuth.getCurrentUser();

        TextView plantNameTextView = (TextView) view.findViewById(R.id.plant_name);
        TextView plantSpeciesTextView = (TextView) view.findViewById(R.id.plant_species);
        TextView plantBirthdayTextView = (TextView) view.findViewById(R.id.plant_birthday);
        ImageView deletePlantProfileImageView = (ImageView) view.findViewById(R.id.delete_plant_profile_button);

        plantNameTextView.setText(plant.getPlantName());
        plantSpeciesTextView.setText(plant.getPlantSpecies());
        plantBirthdayTextView.setText(plant.getPlantBirthday());
        view.setTag(plant.getPlantID());

        mStorageUploadedPlantProfileImageReference = mStoragePlantProfileImagesReference.child(plant.getPlantID() + ".jpg");
        final ImageView plantImageView = (ImageView) view.findViewById(R.id.plant_image);

        final long ONE_MEGABYTE = 1024 * 1024;
        mStorageUploadedPlantProfileImageReference.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                plantImageView.setImageBitmap(bitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });

        // If delete plant button is clicked
        deletePlantProfileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mStorageUploadedPlantProfileImageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mDatabaseReference.child("users").child(currentUser.getUid()).child("plants").child(plant.getPlantID()).removeValue();
                        mDatabaseReference.child("plants").child(plant.getPlantID()).removeValue();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }
}
