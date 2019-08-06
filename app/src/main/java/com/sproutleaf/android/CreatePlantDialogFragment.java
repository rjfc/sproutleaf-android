package com.sproutleaf.android;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;

public class CreatePlantDialogFragment extends DialogFragment {
    private static final String TAG = CreatePlantDialogFragment.class.getName();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private EditText mPlantNameField;
    private Button mProfileSubmit;


    public void CreatePlantDialogFragment() {
    }

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
        mProfileSubmit = rootView.findViewById(R.id.plant_profile_submit);

        // If submit button clicked
        mProfileSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Todo
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
}