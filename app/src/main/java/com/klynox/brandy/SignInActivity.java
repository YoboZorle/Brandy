package com.klynox.brandy;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.klynox.brandy.models.User;

/**
 * Created by YOBO on 1/31/2018.
 */

public class SignInActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "SignInActivity";

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    private EditText mEmailField;
    private EditText mPasswordField;
    private Button mSignInButton;
    private Button mSignUpButton;

   // Trying to get App Network Online / Offline status
    private static SignInActivity instance = new SignInActivity();
    static Context context;
    ConnectivityManager connectivityManager;
    NetworkInfo wifiInfo, mobileInfo;
    boolean connected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);


        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.keepSynced(true);
        mAuth = FirebaseAuth.getInstance();

        // Keep user on MainActivity if Signed In
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (mAuth.getCurrentUser() != null){  // if (user != null){
            // User is Signed In
            Intent i = new Intent(SignInActivity.this, MainActivity.class);
            startActivity(i);
        } else{
            // User is Signed Out
            Toast.makeText(SignInActivity.this,"User Signed Out onCreate.", Toast.LENGTH_LONG).show();
        }
        // Views
        mEmailField = findViewById(R.id.field_email);
        mPasswordField = findViewById(R.id.field_password);
        mSignInButton = findViewById(R.id.button_sign_in);
        mSignUpButton = findViewById(R.id.button_sign_up);

        Button forgot = (Button) findViewById(R.id.fgt_pass_btn);
        forgot.setOnClickListener(this); // calling onClick() for Forgot PassWord Activity

        Button newuser = (Button) findViewById(R.id.btn_reg);
        newuser.setOnClickListener(this); // calling onClick() for New User Activity

        // Click listeners
        mSignInButton.setOnClickListener(this);
        mSignUpButton.setOnClickListener(this);
    }

    public static SignInActivity getInstance(Context ctx) {
        context = ctx.getApplicationContext();
        return instance;
    }

    public boolean isOnline() {
        try {
            connectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            connected = networkInfo != null && networkInfo.isAvailable() &&
                    networkInfo.isConnected();
            return connected;

        } catch (Exception e) {
            System.out.println("CheckConnectivity Exception: " + e.getMessage());
            Log.v("connectivity", e.toString());
        }
        return connected;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Check auth on Activity start
        if (mAuth.getCurrentUser() != null) {
            onAuthSuccess(mAuth.getCurrentUser());
        }
    }

    private void signIn() {
        Log.d(TAG, "signIn");
        if (!validateForm()) {
            return;
        }

        showProgressDialog();
        String email = mEmailField.getText().toString();
        String password = mPasswordField.getText().toString();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signIn:onComplete:" + task.isSuccessful());
                        hideProgressDialog();

                        if (!task.isSuccessful()) {

//                            AlertDialog.Builder builder = new AlertDialog.Builder(SignInActivity.this);
//                            builder.setMessage("You need internet connection for this app. Please turn on mobile network or Wi-Fi in Settings.")
//                                    .setTitle("Unable to connect")
//                                    .setCancelable(false)
//                                    .setPositiveButton("Settings",
//                                            new DialogInterface.OnClickListener() {
//                                                public void onClick(DialogInterface dialog, int id) {
//                                                    Intent i = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
//                                                    startActivity(i);
//                                                }
//                                            }
//                                    )
//                                    .setNegativeButton("Cancel",
//                                            new DialogInterface.OnClickListener() {
//                                                public void onClick(DialogInterface dialog, int id) {
//                                                    SignInActivity.this.finish();
//                                                }
//                                            }
//                                    );
//                            AlertDialog alert = builder.create();
//                            alert.show();

                            if (SignInActivity.getInstance(SignInActivity.this).isOnline()) {

                               // Toast.makeText(SignInActivity.this,"You are online!!!!", Toast.LENGTH_LONG).show();

                                String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                                switch (errorCode)
                                {
                                    case "ERROR_INVALID_CUSTOM_TOKEN":
                                        Toast.makeText(SignInActivity.this, "The custom token format is incorrect. Please check the documentation.", Toast.LENGTH_LONG).show();
                                    case "ERROR_CUSTOM_TOKEN_MISMATCH":
                                        Toast.makeText(SignInActivity.this, "The custom token corresponds to a different audience.", Toast.LENGTH_LONG).show();
                                        break;
                                    case "ERROR_INVALID_CREDENTIAL":
                                        Toast.makeText(SignInActivity.this, "The supplied auth credential is malformed or has expired.", Toast.LENGTH_LONG).show();
                                        break;
                                    case "ERROR_INVALID_EMAIL":
                                        //Toast.makeText(SignInActivity.this, "example@email.com", Toast.LENGTH_LONG).show();
                                        mEmailField.setError("The email address is badly formatted.");
                                        mEmailField.requestFocus();
                                        break;
                                    case "ERROR_WRONG_PASSWORD":
                                        Toast.makeText(SignInActivity.this, "The password is invalid or the user does not have a password.", Toast.LENGTH_LONG).show();
                                        mPasswordField.setError("password is incorrect ");
                                        mPasswordField.requestFocus();
                                        mPasswordField.setText("");
                                        break;
                                    case "ERROR_USER_MISMATCH":
                                        Toast.makeText(SignInActivity.this, "The supplied credentials do not correspond to the previously signed in user.", Toast.LENGTH_LONG).show();
                                        break;
                                    case "ERROR_REQUIRES_RECENT_LOGIN":
                                        Toast.makeText(SignInActivity.this, "This operation is sensitive and requires recent authentication. Log in again before retrying this request.", Toast.LENGTH_LONG).show();
                                        break;
                                    case "ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL":
                                        Toast.makeText(SignInActivity.this, "An account already exists with the same email address but different sign-in credentials. Sign in using a provider associated with this email address.", Toast.LENGTH_LONG).show();
                                        break;
                                    case "ERROR_EMAIL_ALREADY_IN_USE":
                                        Toast.makeText(SignInActivity.this, "The email address is already in use by another account.   ", Toast.LENGTH_LONG).show();
                                        mEmailField.setError("The email address is already in use by another account.");
                                        mEmailField.requestFocus();
                                        break;
                                    case "ERROR_CREDENTIAL_ALREADY_IN_USE":
                                        Toast.makeText(SignInActivity.this, "This credential is already associated with a different user account.", Toast.LENGTH_LONG).show();
                                        break;
                                    case "ERROR_USER_DISABLED":
                                        Toast.makeText(SignInActivity.this, "The user account has been disabled by Joka. Contact Support.", Toast.LENGTH_LONG).show();
                                        break;
                                    case "ERROR_USER_TOKEN_EXPIRED":
                                        Toast.makeText(SignInActivity.this, "The user\\'s credential is no longer valid. The user must sign in again.", Toast.LENGTH_LONG).show();
                                        break;
                                    case "ERROR_USER_NOT_FOUND":
                                        Toast.makeText(SignInActivity.this, "There is no user record corresponding to this identifier.", Toast.LENGTH_LONG).show();
                                        break;
                                    case "ERROR_INVALID_USER_TOKEN":
                                        Toast.makeText(SignInActivity.this, "The user\\'s credential is no longer valid. The user must sign in again.", Toast.LENGTH_LONG).show();
                                        break;
                                    case "ERROR_OPERATION_NOT_ALLOWED":
                                        Toast.makeText(SignInActivity.this, "This operation is not allowed. You must enable this service in the console.", Toast.LENGTH_LONG).show();
                                        break;
                                    case "ERROR_WEAK_PASSWORD":
                                        Toast.makeText(SignInActivity.this, "The given password is invalid.", Toast.LENGTH_LONG).show();
                                        mPasswordField.setError("The password is invalid it must 6 characters at least");
                                        mPasswordField.requestFocus();
                                        break;
                                }

                            } else {

                                Toast.makeText(SignInActivity.this,"Sign In Failed. Check your internet Connection.", Toast.LENGTH_LONG).show();
                               // Log.v("Home", "You are not online!!!!");
                            }

                        } else{

                            onAuthSuccess(task.getResult().getUser());
                            checkIfEmailVerified();

                        }
                    }
                });
    }

    // Check if Email is Verified
    private void checkIfEmailVerified()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user.isEmailVerified())
        {
            // user is verified, so you can finish this activity or send user to activity which you want.
            finish();
            Toast.makeText(SignInActivity.this, "You are Welcome to Joka", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(SignInActivity.this, MainActivity.class));
        }
        else
        {
            // email is not verified, so just prompt the message to the user and restart this activity.
            // NOTE: don't forget to log out the user.

            FirebaseAuth.getInstance().signOut();
            //startActivity(new Intent(this, SignInActivity.class));
//            Intent intent = getIntent();
//            finish();
//            startActivity(intent);

            AlertDialog.Builder ab = new AlertDialog.Builder(SignInActivity.this);

            ab.setTitle("Email not Verified!");
            ab.setIcon(R.mipmap.ic_launcher);
            ab.setMessage("Verify the link sent to your email to complete your registration.");

            ab.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int i) {
                   // Toast.makeText(SignInActivity.this, "ok", Toast.LENGTH_SHORT).show();
                }
            });

            ab.show(); // end of Dialog Notif

           // Toast.makeText(SignInActivity.this, "Verify the email sent to you", Toast.LENGTH_SHORT).show();

            //restart this activity

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

                        if (task.isSuccessful()) {
                            sendVerificationEmail();
                            onAuthSuccess(task.getResult().getUser());
                        } else {
                            Toast.makeText(SignInActivity.this, "Sign Up Failed",
                                    Toast.LENGTH_SHORT).show();
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
                            startActivity(new Intent(SignInActivity.this, SignInActivity.class));
                            finish();
                        }
                        else
                        {
                            // email not sent, so display message and restart the activity or do whatever you wish to do

                            //restart this activity
                            overridePendingTransition(0, 0);
                            finish();
                            overridePendingTransition(0, 0);
                            startActivity(getIntent());

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
    private void writeNewUser(String userId, String name, String email) {
        User user = new User(name, email);
        mDatabase.child("users").child(userId).setValue(user);
    }
    // [END basic_write]

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.button_sign_in) {
            signIn();
        }
        else if(i == R.id.fgt_pass_btn){
            startActivity(new Intent(SignInActivity.this, ForgotPassword.class));
        }
        else if(i == R.id.btn_reg){
            startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
        }
        else if (i == R.id.button_sign_up) {
            signUp();
        }
    }
}
