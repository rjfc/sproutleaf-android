package com.sproutleaf.android;

import android.app.Activity;
import android.app.DatePickerDialog;
import androidx.fragment.app.FragmentManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;

public class CreatePlantDialogFragment extends DialogFragment {
    private static final String TAG = CreatePlantDialogFragment.class.getName();
    static final int REQUEST_TAKE_PHOTO = 1;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseReference;
    private FirebaseStorage mStorage;
    private StorageReference mStorageReference;
    private StorageReference mStoragePlantProfileImagesReference;
    private StorageReference mStorageUploadedPlantProfileImageReference;
    private EditText mPlantNameField;
    private EditText mPlantSpeciesField;
    private EditText mPlantBirthdayField;
    private ImageView mPlantTakeImageButton;
    private ImageView mPlantTakeImageThumbnail;
    private String mCurrentImagePath;
    private Button mProfileSubmit;
    private Bitmap mCurrentImageBitmap;
    private CreatingPlantProfileSpinnerFragment mCreatingPlantProfileDialog;
    private Context mContext;

    // Function for giving a name to the dialog fragment
    public static CreatePlantDialogFragment newInstance(String title) {
        CreatePlantDialogFragment frag = new CreatePlantDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView  = inflater.inflate(R.layout.fragment_create_plant, container);
        mPlantNameField = rootView.findViewById(R.id.give_plant_name_field);
        mPlantSpeciesField = rootView.findViewById(R.id.give_plant_species_field);
        mPlantBirthdayField = rootView.findViewById(R.id.give_plant_birthday_field);
        mProfileSubmit = rootView.findViewById(R.id.plant_profile_submit);
        mPlantTakeImageButton = rootView.findViewById(R.id.take_plant_image_button);
        mPlantTakeImageThumbnail = rootView.findViewById(R.id.take_plant_image_thumbnail);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance();
        mStorageReference = mStorage.getReference();
        mStoragePlantProfileImagesReference = mStorageReference.child("user").child(mAuth.getCurrentUser().getUid()).child("plant-profile-images");

        // If submit button clicked
        mProfileSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            showCreatingPlantProfileDialog();
            // Update plant in database
            String plantName = mPlantNameField.getText().toString();
            String plantSpecies = mPlantSpeciesField.getText().toString();
            String plantBirthday = mPlantBirthdayField.getText().toString();

            Log.d(TAG, "createPlant:" + plantName + "/" + plantSpecies + "/" + plantBirthday);
            if (!validateForm()) {
                return;
            }
            final FirebaseUser currentUser = mAuth.getCurrentUser();
            DatabaseReference plantsReference = mDatabaseReference.child("plants");
            Plant newPlant = new Plant(plantName, plantSpecies, plantBirthday, currentUser.getUid());
            plantsReference.push().setValue(newPlant, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError == null) {
                        String uniqueKey = databaseReference.getKey();
                        // Upload image to Firebase Storage
                        mDatabaseReference.child("users").child(currentUser.getUid()).child("plants").child(uniqueKey).setValue("");
                        mStorageUploadedPlantProfileImageReference = mStoragePlantProfileImagesReference.child(uniqueKey + ".jpg");
                        if (mCurrentImageBitmap != null) {
                            Bitmap uploadBitmap = resize(mCurrentImageBitmap, 600, 800);
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            uploadBitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
                            byte[] data = baos.toByteArray();

                            UploadTask uploadTask = mStorageUploadedPlantProfileImageReference.putBytes(data);
                            uploadTask.addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    // Handle unsuccessful uploads
                                }
                            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    hideCreatingPlantProfileDialog();
                                    getDialog().dismiss();
                                }
                            });
                        }
                        else {
                            hideCreatingPlantProfileDialog();
                            getDialog().dismiss();
                        }
                    }
                    else {
                        Log.e(TAG, databaseError.toString());
                    }
                }
            });
            }
        });

        // If birthday field button clicked launch datepicker dialog
        mPlantBirthdayField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mContext = view.getContext();
                final Calendar myCalendar = Calendar.getInstance();
                DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        // TODO Auto-generated method stub
                        myCalendar.set(Calendar.YEAR, year);
                        myCalendar.set(Calendar.MONTH, monthOfYear);
                        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        String format = "MMMM dd, yyyy";
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format, Locale.getDefault());

                        mPlantBirthdayField.setText(simpleDateFormat.format(myCalendar.getTime()));
                    }
                };
                new DatePickerDialog(mContext, R.style.DatepickerDialogAppearance, date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        // If take plant image button clicked launch camera activity
        mPlantTakeImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Ensure that there's a camera activity to handle the intent
                if (takePictureIntent.resolveActivity(getContext().getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        // Error occurred while creating the File
                        // TODO: error handling
                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        Uri photoURI = FileProvider.getUriForFile(getContext(),
                                "com.sproutleaf.android.fileprovider",
                                photoFile);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                    }
                }
            }
        });

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_TAKE_PHOTO) {
                try {
                    // Only resize when image added
                    int heightInDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 126, getResources().getDisplayMetrics());
                    int widthInDp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 90, getResources().getDisplayMetrics());
                    mPlantTakeImageThumbnail.getLayoutParams().height = heightInDp;
                    mPlantTakeImageThumbnail.getLayoutParams().width = widthInDp;
                    mPlantTakeImageThumbnail.requestLayout();

                    mCurrentImageBitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(getContext().getContentResolver(), Uri.fromFile(new File(mCurrentImagePath))));
                    mPlantTakeImageThumbnail.setImageBitmap(mCurrentImageBitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    // Create image file of captured plant
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentImagePath = image.getAbsolutePath();
        return image;
    }

    private static Bitmap resize(Bitmap image, int maxWidth, int maxHeight) {
        if (maxHeight > 0 && maxWidth > 0) {
            int width = image.getWidth();
            int height = image.getHeight();
            float ratioBitmap = (float) width / (float) height;
            float ratioMax = (float) maxWidth / (float) maxHeight;

            int finalWidth = maxWidth;
            int finalHeight = maxHeight;
            if (ratioMax > 1) {
                finalWidth = (int) ((float)maxHeight * ratioBitmap);
            } else {
                finalHeight = (int) ((float)maxWidth / ratioBitmap);
            }
            image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
            return image;
        } else {
            return image;
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Fetch arguments from bundle and set title
        String title = getArguments().getString("title", "New Plant Profile");
        getDialog().setTitle(title);
        // Show soft keyboard automatically and request focus to field
        mPlantNameField.requestFocus();
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    // Create dialog instance
    private void showCreatingPlantProfileDialog() {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        mCreatingPlantProfileDialog = CreatingPlantProfileSpinnerFragment.newInstance("Creating plant profile...");
        mCreatingPlantProfileDialog.show(fm, "fragment_creating_plant_profile_spinner");
    }

    // Hide dialog
    private void hideCreatingPlantProfileDialog() {
        mCreatingPlantProfileDialog.dismissDialog();
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = mPlantNameField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mPlantNameField.setError("Required.");
            valid = false;
        } else {
            mPlantNameField.setError(null);
        }

        return valid;
    }
}