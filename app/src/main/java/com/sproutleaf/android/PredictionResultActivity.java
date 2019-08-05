package com.sproutleaf.android;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.io.IOException;

public class PredictionResultActivity extends AppCompatActivity {
    private Bitmap mImageBitmap;
    private ImageView mCapturedImageView;
    private String mCapturedPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prediction_result);
    }

    @Override
    public void onStart() {
        mCapturedImageView = findViewById(R.id.prediction_image);
        super.onStart();

        try{
            Intent intent = getIntent();
            mCapturedPhotoPath = intent.getStringExtra("capturedPhotoPath");
            mImageBitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(this.getContentResolver(), Uri.fromFile(new File(mCapturedPhotoPath))));
            mCapturedImageView.setImageBitmap(mImageBitmap);
        } catch (
        IOException e) {
            // Error
            e.printStackTrace();
        }
    }
}
