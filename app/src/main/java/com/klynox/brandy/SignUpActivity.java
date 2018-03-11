package com.klynox.brandy;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.klynox.brandy.models.User;

/**
 * Created by YOBO on 1/31/2018.
 */

public class SignUpActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "SignUpActivity";

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private DatabaseReference userIdRef;

    private EditText mEmailField;
    private EditText mPasswordField;

    private EditText ffname, sstate;
   // private Button mSignInButton;
    private Button mSignUpButton;
    private Button mSubmitButton;

    //Add user trial
    private EditText mTitleField;
    private EditText mBodyField;

  //  Button btn_sing_up;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        //Add user trial

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.keepSynced(true);
        mAuth = FirebaseAuth.getInstance();

        // Views
        mEmailField = findViewById(R.id.field_email);
        mPasswordField = findViewById(R.id.field_password);

        sstate = findViewById(R.id.state);
        ffname = findViewById(R.id.fname);
      //  mSignInButton = findViewById(R.id.button_sign_in);
        mSignUpButton = findViewById(R.id.button_sign_up);
        mSubmitButton = findViewById(R.id.submit);

        mSubmitButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SignUpActivity.this, "Submit Clicked", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(SignUpActivity.this, SaveUser.class));
                finish();
            }
        });

        mSignUpButton.setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();

        // Check auth on Activity start
        if (mAuth.getCurrentUser() != null) {
            onAuthSuccess(mAuth.getCurrentUser());
        }
    }

    private void signUp() {
        Log.d(TAG, "signUp");
        if (!validateForm()) {
            return;
        }

        showProgressDialog();
        String email = mEmailField.getText().toString();
        String password = mPasswordField.getText().toString();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUser:onComplete:" + task.isSuccessful());
                        hideProgressDialog();

                        if (!task.isSuccessful()) {

                            if (SignInActivity.getInstance(SignUpActivity.this).isOnline()) {

                                // Toast.makeText(SignInActivity.this,"You are online!!!!", Toast.LENGTH_LONG).show();

                                String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                                switch (errorCode)
                                {
                                    case "ERROR_INVALID_CUSTOM_TOKEN":
                                        Toast.makeText(SignUpActivity.this, "The custom token format is incorrect. Please check the documentation.", Toast.LENGTH_LONG).show();
                                    case "ERROR_CUSTOM_TOKEN_MISMATCH":
                                        Toast.makeText(SignUpActivity.this, "The custom token corresponds to a different audience.", Toast.LENGTH_LONG).show();
                                        break;
                                    case "ERROR_INVALID_CREDENTIAL":
                                        Toast.makeText(SignUpActivity.this, "The supplied auth credential is malformed or has expired.", Toast.LENGTH_LONG).show();
                                        break;
                                    case "ERROR_INVALID_EMAIL":
                                       // Toast.makeText(SignUpActivity.this, "example@email.com", Toast.LENGTH_LONG).show();
                                        mEmailField.setError("The email address is badly formatted.");
                                        mEmailField.requestFocus();
                                        break;
                                    case "ERROR_WRONG_PASSWORD":
                                        Toast.makeText(SignUpActivity.this, "The password is invalid or the user does not have a password.", Toast.LENGTH_LONG).show();
                                        mPasswordField.setError("password is incorrect ");
                                        mPasswordField.requestFocus();
                                        mPasswordField.setText("");
                                        break;
                                    case "ERROR_USER_MISMATCH":
                                        Toast.makeText(SignUpActivity.this, "The supplied credentials do not correspond to the previously signed in user.", Toast.LENGTH_LONG).show();
                                        break;
                                    case "ERROR_REQUIRES_RECENT_LOGIN":
                                        Toast.makeText(SignUpActivity.this, "This operation is sensitive and requires recent authentication. Log in again before retrying this request.", Toast.LENGTH_LONG).show();
                                        break;
                                    case "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL":
                                        Toast.makeText(SignUpActivity.this, "An account already exists with the same email address but different sign-in credentials. Sign in using a provider associated with this email address.", Toast.LENGTH_LONG).show();
                                        break;
                                    case "ERROR_EMAIL_ALREADY_IN_USE":
                                        Toast.makeText(SignUpActivity.this, "The email address is already in use by another account.   ", Toast.LENGTH_LONG).show();
                                        mEmailField.setError("The email address is already in use by another account.");
                                        mEmailField.requestFocus();
                                        break;
                                    case "ERROR_CREDENTIAL_ALREADY_IN_USE":
                                        Toast.makeText(SignUpActivity.this, "This credential is already associated with a different user account.", Toast.LENGTH_LONG).show();
                                        break;
                                    case "ERROR_USER_DISABLED":
                                        Toast.makeText(SignUpActivity.this, "The user account has been disabled by Joka. Contact Support.", Toast.LENGTH_LONG).show();
                                        break;
                                    case "ERROR_USER_TOKEN_EXPIRED":
                                        Toast.makeText(SignUpActivity.this, "The user\\'s credential is no longer valid. The user must sign in again.", Toast.LENGTH_LONG).show();
                                        break;
                                    case "ERROR_USER_NOT_FOUND":
                                        Toast.makeText(SignUpActivity.this, "Couldn't find your Joka account.", Toast.LENGTH_LONG).show();
                                        break;
                                    case "ERROR_INVALID_USER_TOKEN":
                                        Toast.makeText(SignUpActivity.this, "The user\\'s credential is no longer valid. The user must sign in again.", Toast.LENGTH_LONG).show();
                                        break;
                                    case "ERROR_OPERATION_NOT_ALLOWED":
                                        Toast.makeText(SignUpActivity.this, "This operation is not allowed. You must enable this service in the console.", Toast.LENGTH_LONG).show();
                                        break;
                                    case "ERROR_WEAK_PASSWORD":
                                        Toast.makeText(SignUpActivity.this, "The given password is invalid.", Toast.LENGTH_LONG).show();
                                        mPasswordField.setError("The password is invalid it must be 6 characters at least");
                                        mPasswordField.requestFocus();
                                        break;
                                }

                            }
                            else {

                                Toast.makeText(SignUpActivity.this,"Sign Up Failed. Check your internet Connection.", Toast.LENGTH_LONG).show();
                                // Log.v("Home", "You are not online!!!!");
                            }

                        }
                        else{
                            onAuthSuccess(task.getResult().getUser());
                            sendVerificationEmail();
                            userIdRef = mDatabase.child(mAuth.getCurrentUser().getUid());
                            userIdRef.child("name").setValue(ffname.getText().toString());

                            // [START single_value_read] for adding user details
                            final String userId = getUid();
                            mDatabase.child("users").child(userId).addListenerForSingleValueEvent(
                                    new ValueEventListener() {

                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            // Get user value
                                            User user = dataSnapshot.getValue(User.class);

                                            // [START_EXCLUDE]
                                            if (user == null) {
                                                // User is null, error out
                                                Log.e(TAG, "User " + userId + " is unexpectedly null");
                                                Toast.makeText(SignUpActivity.this,
                                                        "Error: could not fetch user.",
                                                        Toast.LENGTH_SHORT).show();
                                            } else {
                                                // Write new post
                                               // writeNewPost(userId, user.username, title, body);
                                                Toast.makeText(SignUpActivity.this, "User exist", Toast.LENGTH_SHORT).show();
                                            }

                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                            // [END single_value_read]

                        }
                    }
                });

    }

    // Send Verification Email
    private void sendVerificationEmail()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // email sent

                            // after email is sent just logout the user and finish this activity
                            FirebaseAuth.getInstance().signOut();
                            startActivity(new Intent(SignUpActivity.this, SaveUser.class));
                            finish();
                        }
                        else {
                            // email not sent, so display message and restart the activity or do whatever you wish to do

                            //restart this activity
                            overridePendingTransition(0, 0);
                            finish();
                            overridePendingTransition(0, 0);
                            startActivity(getIntent());
                            toastMessage("Email Verification not sent");
                        }
                    }
                });
    }

    private void onAuthSuccess(FirebaseUser user) {
        String username = usernameFromEmail(user.getEmail());

        // Write new user
        writeNewUser(user.getUid(), username, user.getEmail());

//        // Go to MainActivity
//        startActivity(new Intent(SignInActivity.this, MainActivity.class));
//        finish();
    }

    private String usernameFromEmail(String email) {
        if (email.contains("@")) {
            return email.split("@")[0];
        } else {
            return email;
        }
    }

    private boolean validateForm() {
        boolean result = true;
        if (TextUtils.isEmpty(mEmailField.getText().toString())) {
            mEmailField.setError("Required");
            result = false;
        } else {
            mEmailField.setError(null);
        }

        if (TextUtils.isEmpty(mPasswordField.getText().toString())) {
            mPasswordField.setError("Required");
            result = false;
        } else {
            mPasswordField.setError(null);
        }

        return result;
    }

    // [START basic_write]
    private void writeNewUser(String userId, String name,  String email) {
        User user = new User(name,  email);
        mDatabase.child("users").child(userId).setValue(user);
        userIdRef = mDatabase.child(mAuth.getCurrentUser().getUid());
        userIdRef.child("users").setValue(ffname.getText().toString());
    }
    // [END basic_write]


    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.button_sign_in) {
           // signIn();
        } else if (i == R.id.button_sign_up) {
            signUp();
        }
    }

    //add a toast to show when successfully signed in
    /**
     * customizable toast
     * @param message
     */
    private void toastMessage(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }
}
