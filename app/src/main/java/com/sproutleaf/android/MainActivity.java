package com.sproutleaf.android;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    static final int REQUEST_TAKE_PHOTO = 1;
    private String mCurrentImagePath;
    private androidx.appcompat.widget.Toolbar mToolbar;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onStart(){
        super.onStart();

        // Set member variables
        mToolbar = findViewById(R.id.main_toolbar);

        FirebaseUser user = mAuth.getCurrentUser();
        String displayName = user.getDisplayName();

        // Toolbar config
        mToolbar.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.darkerGrey, null));
        mToolbar.setTitleTextColor(ResourcesCompat.getColor(getResources(), R.color.colorPrimaryDark, null));
        mToolbar.setTitle(getResources().getString(R.string.main_toolbar_text));
        mToolbar.setTitleTextAppearance(this, R.style.ToolbarTextAppearance);
        setSupportActionBar(mToolbar); // Set mToolbar as Action Bar

        if (displayName == null) {
            showGiveNameDialog();
        }
    }

    // Show give name dialog
    private void showGiveNameDialog(){
        FragmentManager fm = getSupportFragmentManager();
        GiveNameDialogFragment giveNameDialogFragment = GiveNameDialogFragment.newInstance("Provide Your Name");
        giveNameDialogFragment.show(fm, "fragment_give_name");
    }

    // Function to call when diagnose plant button clicked
    public void capturePlant(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
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
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.sproutleaf.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    // Create image file of captured plant
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentImagePath = image.getAbsolutePath();
        return image;
    }

    // On result of activity launched from intent
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // If requestCode = camera app launched
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            // Send path as intent
            Intent intent = new Intent(this, PredictionResultActivity.class);
            intent.putExtra("capturedPhotoPath", mCurrentImagePath);
            startActivity(intent);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mCurrentImagePath != null) {
            outState.putString("cameraImageUri", mCurrentImagePath);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey("cameraImageUri")) {
            mCurrentImagePath = Uri.parse(savedInstanceState.getString("cameraImageUri")).toString();
        }
    }

    public void logOut(View view) {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, AuthenticationActivity.class);
        startActivity(intent);
    }

    // Launch activity_journal.xml
    public void launchJournal(View view) {
        Intent intent = new Intent(this, JournalActivity.class);
        startActivity(intent);
    }
}