package com.sproutleaf.android;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    static final int REQUEST_TAKE_PHOTO = 1;
    private ImageDecoder.Source mImageDecoderSource;
    private String mCurrentPhotoPath;
    private ImageView mImageView;

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
            showGiveNameDialog();
        }
    }

    // Show give name dialog
    private void showGiveNameDialog() {
        FragmentManager fm = getSupportFragmentManager();
        GiveNameDialogFragment editNameDialogFragment = GiveNameDialogFragment.newInstance("Provide Your Name");
        editNameDialogFragment.show(fm, "fragment_give_name");
    }

    // Function to call when diagnose plant button clicked
    public void capturePlant (View view) {
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
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    // On result of activity launched from intent
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // If requestCode = camera app launched
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            // Send path as intent
            Log.d("image", mCurrentPhotoPath);
            Intent intent = new Intent(this, PredictionResultActivity.class);
            intent.putExtra("capturedPhotoPath", mCurrentPhotoPath);
            startActivity(intent);
        }
    }
}
