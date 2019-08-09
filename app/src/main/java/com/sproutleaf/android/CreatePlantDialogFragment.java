package com.sproutleaf.android;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Locale;
import java.text.SimpleDateFormat;

public class CreatePlantDialogFragment extends DialogFragment {
    private static final String TAG = CreatePlantDialogFragment.class.getName();
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseReference;
    private EditText mPlantNameField;
    private EditText mPlantSpeciesField;
    private EditText mPlantBirthdayField;
    private Button mProfileSubmit;
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

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        // If submit button clicked
        mProfileSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                        mDatabaseReference.child("users").child(currentUser.getUid()).child("plants").child(uniqueKey).setValue("");
                        getDialog().dismiss();
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

        return rootView;
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