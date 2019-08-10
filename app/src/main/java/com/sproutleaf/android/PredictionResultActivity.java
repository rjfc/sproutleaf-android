package com.sproutleaf.android;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.common.modeldownload.FirebaseLocalModel;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.common.modeldownload.FirebaseRemoteModel;
import com.google.firebase.ml.custom.FirebaseModelDataType;
import com.google.firebase.ml.custom.FirebaseModelInputOutputOptions;
import com.google.firebase.ml.custom.FirebaseModelInputs;
import com.google.firebase.ml.custom.FirebaseModelInterpreter;
import com.google.firebase.ml.custom.FirebaseModelOptions;
import com.google.firebase.ml.custom.FirebaseModelOutputs;

import java.io.File;
import java.io.IOException;

public class PredictionResultActivity extends AppCompatActivity {
    private static final String TAG = PredictionResultActivity.class.getName();
    private FirebaseAuth mAuth;
    private Bitmap mImageBitmap;
    private ImageView mCapturedImageView;
    private String mCapturedPhotoPath;
    private TextView mPredictionInfo;
    private PredictingSpinnerFragment mPredictingDialog;
    private float[] mProbabilities;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prediction_result);
    }

    @Override
    public void onStart() {
        mCapturedImageView = findViewById(R.id.prediction_image);
        mPredictionInfo = findViewById(R.id.prediction_info);
        super.onStart();

        showPredictingDialog();

        try{
            // Get path intent and set image view to image
            Intent intent = getIntent();
            mCapturedPhotoPath = intent.getStringExtra("capturedPhotoPath");
            mImageBitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(this.getContentResolver(), Uri.fromFile(new File(mCapturedPhotoPath))));
            mCapturedImageView.setImageBitmap(mImageBitmap);
            runInference(mImageBitmap);
        } catch (IOException e) {
            // Error
            e.printStackTrace();
        } catch (FirebaseMLException e) {
            e.printStackTrace();
        }
    }

    // Configure model hosted with Firebase
    private void configureHostedModelSource() {
        FirebaseModelDownloadConditions.Builder conditionsBuilder = new FirebaseModelDownloadConditions.Builder().requireWifi();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Enable advanced conditions on Android Nougat and newer.
            conditionsBuilder = conditionsBuilder
                    .requireCharging()
                    .requireDeviceIdle();
        }
        FirebaseModelDownloadConditions conditions = conditionsBuilder.build();

        FirebaseRemoteModel cloudSource = new FirebaseRemoteModel.Builder("houseplant-healthy-or-not-cloud")  // Assign a name to this model
                .enableModelUpdates(true)
                .setInitialDownloadConditions(conditions)
                .setUpdatesDownloadConditions(conditions)
                .build();
        FirebaseModelManager.getInstance().registerRemoteModel(cloudSource);
    }

    // Configure locally hosted model
    private void configureLocalModelSource() {
        FirebaseLocalModel localSource = new FirebaseLocalModel.Builder("houseplant-healthy-or-not-local")  // Assign a name to this model
                        .setAssetFilePath("houseplant-healthy-or-not-cnn.tflite")
                        .build();
        FirebaseModelManager.getInstance().registerLocalModel(localSource);
    }

    // Model interpreter
    private FirebaseModelInterpreter createInterpreter() throws FirebaseMLException {
        configureHostedModelSource();
        configureLocalModelSource();
        FirebaseModelOptions options = new FirebaseModelOptions.Builder()
                .setRemoteModelName("houseplant-healthy-or-not-cloud")
                .setLocalModelName("houseplant-healthy-or-not-local")
                .build();
        FirebaseModelInterpreter firebaseInterpreter =
                FirebaseModelInterpreter.getInstance(options);

        return firebaseInterpreter;
    }

    // Specifying model's input and output
    private FirebaseModelInputOutputOptions createInputOutputOptions() throws FirebaseMLException {
        FirebaseModelInputOutputOptions inputOutputOptions =
                new FirebaseModelInputOutputOptions.Builder()
                        .setInputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, 224, 224, 3})  // Input shape
                        .setOutputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, 2})  // Output shape
                        .build();

        return inputOutputOptions;
    }

    // Convert captured image bitmap to input array with bitmap as parameter
    private float[][][][] bitmapToInputArray(Bitmap bitmap) {
        bitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true);
        bitmap = bitmap.copy(Bitmap.Config.RGB_565, true);  // getPixel does not support hardware bitmaps
        int batchNum = 0;
        float[][][][] input = new float[1][224][224][3];
        for (int x = 0; x < 224; x++) {
            for (int y = 0; y < 224; y++) {
                int pixel = bitmap.getPixel(x, y);
                // Normalize channel values to [-1.0, 1.0]
                input[batchNum][x][y][0] = (Color.red(pixel) - 127) / 128.0f;
                input[batchNum][x][y][1] = (Color.green(pixel) - 127) / 128.0f;
                input[batchNum][x][y][2] = (Color.blue(pixel) - 127) / 128.0f;
            }
        }

        return input;
    }

    // Run input data through model
    private void runInference(Bitmap bitmap) throws FirebaseMLException {
        FirebaseModelInterpreter firebaseInterpreter = createInterpreter();
        float[][][][] input = bitmapToInputArray(bitmap);
        FirebaseModelInputOutputOptions inputOutputOptions = createInputOutputOptions();

        FirebaseModelInputs inputs = new FirebaseModelInputs.Builder()
                .add(input)  // Can add more if required
                .build();
        firebaseInterpreter.run(inputs, inputOutputOptions).addOnSuccessListener(
            new OnSuccessListener<FirebaseModelOutputs>() {
                @Override
                public void onSuccess(FirebaseModelOutputs result) {
                    float[][] output = result.getOutput(0);
                    mProbabilities = output[0];  // Probabilities array
                    updatePredictionInfo(mProbabilities);
                }
            }
        ).addOnFailureListener(
            new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // TODO: exception handling
                    // Error
                    e.printStackTrace();
                }
            }
        );
    }

    private void updatePredictionInfo(float[] probabilities) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String displayName = user.getDisplayName();
        Log.i("TAG", "probability:" + probabilities[0]);
        if (probabilities[0] >= 0.99999) {
            // Doing great
            mPredictionInfo.setText(String.format(getString(R.string.prediction_info_text_healthy), displayName));
            mPredictionInfo.setTextColor(getResources().getColor(R.color.healthyGreen, null));
        }
        else if (probabilities[0] >= 0.8 && probabilities[0] < 0.99999) {
            // Could be better
            mPredictionInfo.setText(getString(R.string.prediction_info_text_attention));
            mPredictionInfo.setTextColor(getResources().getColor(R.color.attentionOrange, null));
        }
        else if (probabilities[0] < 0.8) {
            // In need of urgent help
            mPredictionInfo.setText(getString(R.string.prediction_info_text_urgent));
            mPredictionInfo.setTextColor(getResources().getColor(R.color.urgentRed, null));
        }
        hidePredictingDialog();
    }

    // Create dialog instance
    private void showPredictingDialog() {
        FragmentManager fm = getSupportFragmentManager();
        mPredictingDialog = PredictingSpinnerFragment.newInstance("Predicting...");
        mPredictingDialog.show(fm, "fragment_predicting_spinner");
    }

    // Hide dialog
    private void hidePredictingDialog() {
        mPredictingDialog.dismissDialog();
    }
}
