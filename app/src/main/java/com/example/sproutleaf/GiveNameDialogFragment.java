package com.example.sproutleaf;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class GiveNameDialogFragment extends DialogFragment {
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private EditText mEditText;

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
        this.getDialog().setCanceledOnTouchOutside(false); // Do not cancel dialog when outside is touched
        return inflater.inflate(R.layout.fragment_give_name, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Get field from view
        mEditText = (EditText) view.findViewById(R.id.give_name_field);
        // Fetch arguments from bundle and set title
        String title = getArguments().getString("title", "Enter Name");
        getDialog().setTitle(title);
        // Show soft keyboard automatically and request focus to field
        mEditText.requestFocus();
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    public void submitDisplayName (View view) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String displayName = mEditText.getText().toString();

        // Update display name
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(displayName).build();
        user.updateProfile(profileUpdates);

        // If display name has been set, dismiss the dialog
        if (user.getDisplayName() != "") {
            getDialog().dismiss();
        }
    }
}