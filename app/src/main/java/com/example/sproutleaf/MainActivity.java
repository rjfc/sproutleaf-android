package com.example.sproutleaf;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        String name = user.getDisplayName();
        if (name == null) {
            showEditDialog();
        }
    }
    private void showEditDialog() {
        FragmentManager fm = getSupportFragmentManager();
        GiveNameDialogFragment editNameDialogFragment = GiveNameDialogFragment.newInstance("Provide Your Name");
        editNameDialogFragment.show(fm, "fragment_give_name");
    }
}
