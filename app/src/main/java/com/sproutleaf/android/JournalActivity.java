package com.sproutleaf.android;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class JournalActivity extends AppCompatActivity {
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private TextView mJournalHeaderTextView;
    private androidx.appcompat.widget.Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Set member variables
        mToolbar = findViewById(R.id.journal_toolbar);
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        String displayName = user.getDisplayName();

        // Toolbar config
        mToolbar.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.darkerGrey, null));
        mToolbar.setTitleTextColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimaryDark, null));
        mToolbar.setTitle(String.format(getString(R.string.journal_toolbar_text), displayName));
        mToolbar.setTitleTextAppearance(this, R.style.ToolbarTextAppearance);
        setSupportActionBar(mToolbar); // Set mToolbar as Action Bar
    }

    // Create a new plant
    public void showNewPlantDialog(View view) {
        FragmentManager fm = getSupportFragmentManager();
        CreatePlantDialogFragment createPlantDialogFragment = CreatePlantDialogFragment.newInstance("New Plant Profile");
        createPlantDialogFragment.show(fm, "fragment_create_plant");
    }
}
