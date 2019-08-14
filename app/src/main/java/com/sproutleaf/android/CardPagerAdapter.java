package com.sproutleaf.android;

import android.content.Context;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


import java.util.ArrayList;
import java.util.List;


public class CardPagerAdapter extends PagerAdapter implements CardAdapter {
    private static final String TAG = CardPagerAdapter.class.getName();

    private List<CardView> mViews;
    private List<PlantCardItem> mData;
    private float mBaseElevation;

    private LoadingPlantProfilesSpinnerInterface LoadingPlantProfilesSpinnerInterfaceListener;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseReference;
    private FirebaseStorage mStorage;
    private StorageReference mStorageReference;
    private StorageReference mStoragePlantProfileImagesReference;
    private StorageReference mStorageUploadedPlantProfileImageReference;
    private boolean refresh;

    public CardPagerAdapter(LoadingPlantProfilesSpinnerInterface LoadingPlantProfilesSpinnerInterfaceListener) {
    //    LoadingPlantProfilesSpinnerInterfaceListener.showLoadingPlantProfilesDialog();
        mData = new ArrayList<>();
        mViews = new ArrayList<>();
        this.LoadingPlantProfilesSpinnerInterfaceListener = LoadingPlantProfilesSpinnerInterfaceListener;
    }

    public void addCardItem(PlantCardItem plant) {
        mData.add(plant);
        mViews.add(null);
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

    @Override public int getItemPosition(Object object) {
        if(refresh){
            refresh = false;
            return POSITION_NONE;
        }else{
            return super.getItemPosition(object);
        }
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        // Scroll onto a different Card before deleting
        if (container.getChildCount() > 1) {
            if (position > 0) {
                ((ViewPager) container).setCurrentItem(position - 1);
            } else {
                ((ViewPager) container).setCurrentItem(position + 1);
            }
        }
        container.removeView((View) object);
        mData.remove(position);
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
                /*mDatabaseReference.child("users").child(currentUser.getUid()).child("plants").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            if (child.getKey().equals(plant.getPlantID())) {
                                LoadingPlantProfilesSpinnerInterfaceListener.hideLoadingPlantProfilesDialog();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });*/
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });

        // If delete plant button is clicked
        deletePlantProfileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                Log.d("card", "clicked");
                mStorageUploadedPlantProfileImageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mDatabaseReference.child("users").child(currentUser.getUid()).child("plants").child(plant.getPlantID()).removeValue();
                        mDatabaseReference.child("plants").child(plant.getPlantID()).removeValue();
                        JournalActivity ja = new JournalActivity();
                        ja.removePlantView(plant.getPlantID()); // Temporary way to access method
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

    public void refreshAdapter(){
        refresh = true;
        notifyDataSetChanged();
    }
}
