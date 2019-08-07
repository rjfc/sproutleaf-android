package com.sproutleaf.android;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AuthenticationActivity extends AppCompatActivity {
    private static final String TAG = AuthenticationActivity.class.getName();
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private EditText mEmailField;
    private EditText mPasswordField;
    private TextView mStatusTextView;
    private AuthenticatingSpinnerFragment mAuthDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Set member variables
        mEmailField = findViewById(R.id.auth_email);
        mPasswordField = findViewById(R.id.auth_password);
        mStatusTextView = findViewById(R.id.auth_status);
    }

    @Override
    public void onStart() {
        super.onStart();

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError("Required.");
            valid = false;
        } else {
            mEmailField.setError(null);
        }

        String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError("Required.");
            valid = false;
        } else {
            mPasswordField.setError(null);
        }

        return valid;
    }

    public void AuthError(String error) {
        mStatusTextView.setText(error);
    }

    public void createAccount(View view) {
        String email = mEmailField.getText().toString();
        String password = mPasswordField.getText().toString();

        Log.d(TAG, "createAccount:" + email);
        if (!validateForm()) {
            return;
        }
        showAuthDialog(); // Show auth spinner
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                hideAuthDialog(); // Hide auth spinner
                if (task.isSuccessful()) {
                    // Sign up success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmailAndPassword:success");
                    FirebaseUser currentUser = mAuth.getCurrentUser();

                    // Update in database
                    mDatabase.child("users").child(currentUser.getUid()).setValue(currentUser);

                    updateUI(currentUser);
                } else {
                    // If sign up fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmailAndPassword:failure", task.getException());
                    AuthError(getString(R.string.auth_register_failed));
                    updateUI(null);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof FirebaseAuthUserCollisionException) {
                    String errorCode = ((FirebaseAuthUserCollisionException) e).getErrorCode();
                    if (errorCode.equals("ERROR_EMAIL_ALREADY_IN_USE") || errorCode.equals("ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL")) {
                        AuthError(getString(R.string.auth_account_exists));
                    }
                }
            }
        });
    }

    public void signIn(View view) {
        String email = mEmailField.getText().toString();
        String password = mPasswordField.getText().toString();

        Log.d(TAG, "signIn:" + email);
        if (!validateForm()) {
            return;
        }
        showAuthDialog(); // Show auth spinner
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                hideAuthDialog(); // Hide auth spinner
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmailAndPassword:success");
                    FirebaseUser user = mAuth.getCurrentUser();
                    updateUI(user);
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmailAndPassword:failure", task.getException());
                    AuthError(getString(R.string.auth_login_failed));
                    updateUI(null);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    AuthError(getString(R.string.auth_invalid_password));
                } else if (e instanceof FirebaseAuthInvalidUserException) {
                    String errorCode = ((FirebaseAuthInvalidUserException) e).getErrorCode();
                    if (errorCode.equals("ERROR_USER_NOT_FOUND")) {
                        AuthError(getString(R.string.auth_user_not_found));
                    } else if (errorCode.equals("ERROR_USER_DISABLED")) {
                        AuthError(getString(R.string.auth_user_disabled));
                    } else if (errorCode.equals("ERROR_USER_TOKEN_EXPIRED")) {
                        AuthError(getString(R.string.auth_password_changed));
                    } else if (errorCode.equals("ERROR_INVALID_USER_TOKEN ")) {
                        AuthError(getString(R.string.auth_invalid_token));
                    } else {
                        AuthError(e.getLocalizedMessage());
                    }
                } else if (e instanceof FirebaseAuthUserCollisionException) {
                    String errorCode = ((FirebaseAuthUserCollisionException) e).getErrorCode();
                    if (errorCode.equals("ERROR_CREDENTIAL_ALREADY_IN_USE ")) {
                        AuthError(getString(R.string.auth_error_credential));
                    }
                }
            }
        });
    }

    // Create dialog instance
    private void showAuthDialog() {
        FragmentManager fm = getSupportFragmentManager();
        mAuthDialog = AuthenticatingSpinnerFragment.newInstance("Authenticating...");
        mAuthDialog.show(fm, "fragment_authenticating_spinner");
    }

    // Hide dialog
    private void hideAuthDialog() {
        mAuthDialog.dismissDialog();
    }
}
