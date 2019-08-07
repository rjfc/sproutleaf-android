package com.sproutleaf.android;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class GiveNameDialogFragment extends DialogFragment {
    private static final String TAG = GiveNameDialogFragment.class.getName();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private EditText mEditEmail;
    private Button mAuthSubmit;

    public void EditNameDialogFragment() {
    }

    // Function for giving a name to the dialog fragment
    public static GiveNameDialogFragment newInstance(String title) {
        GiveNameDialogFragment frag = new GiveNameDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setCancelable(false); // Do no cancel dialog when back button pressed
        View rootView  = inflater.inflate(R.layout.fragment_give_name, container);
        mAuthSubmit = rootView.findViewById(R.id.give_name_submit);

        // If submit button clicked
        mAuthSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitDisplayName (view);
            }
        });
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Get field from view
        mEditEmail = (EditText) view.findViewById(R.id.give_name_field);
        // Fetch arguments from bundle and set title
        String title = getArguments().getString("title", "Enter Name");
        getDialog().setTitle(title);
        // Show soft keyboard automatically and request focus to field
        mEditEmail.requestFocus();
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    public void submitDisplayName(View view) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String displayName = mEditEmail.getText().toString();

        // Update display name
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(displayName).build();
        currentUser.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
            if (task.isSuccessful()) {
                Log.d(TAG, "User display name updated.");
                getDialog().dismiss();
            }
            }
        });
    }
}