package com.example.ted;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;


public class LoginActivity extends AppCompatActivity {
    public static final String TAG = "LoginActivity";
    private CallbackManager mCallbackManager;
    public FirebaseAuth mAuth;
    public EditText etName, etUsername, etPassword;
    public Button btnLogin;
    public LoginButton btnFacebook;
    public CheckBox checkBox;
    public TextView tvSignUp, tvError;
    public static Dictionary errorMessages = new Hashtable(){{
        put("ERROR_INVALID_CUSTOM_TOKEN", "The custom token format is incorrect. Please check the documentation.");
        put("ERROR_CUSTOM_TOKEN_MISMATCH", "The custom token corresponds to a different audience.");
        put("ERROR_INVALID_CREDENTIAL", "The supplied auth credential is malformed or has expired.");
        put("ERROR_INVALID_EMAIL", "The email address is badly formatted.");
        put("ERROR_WRONG_PASSWORD", "The password is invalid.");
        put("ERROR_USER_MISMATCH", "The supplied credentials do not correspond to the previously signed in user.");
        put("ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL", "An account already exists with the same email address but different sign-in credentials. Sign in using a provider associated with this email address.");
        put("ERROR_EMAIL_ALREADY_IN_USE", "The email address is already in use by another account.");
        put("ERROR_USER_DISABLED", "The user account has been disabled by an administrator.");
        put("ERROR_USER_NOT_FOUND", "There is no user record corresponding to this identifier. The user may have been deleted.");
        put("ERROR_WEAK_PASSWORD", "The given password is invalid. It must 6 characters at least.");
    }};

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser!=null){
        updateUI(currentUser);}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        mCallbackManager = CallbackManager.Factory.create();

        //Locate necessary components
        btnLogin = findViewById(R.id.btnLogin);
        btnFacebook = findViewById(R.id.btnFacebook);
        etName = findViewById(R.id.etName);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etPassword.setTransformationMethod(new PasswordTransformationMethod());
        checkBox = findViewById(R.id.checkBox);
        tvSignUp = findViewById(R.id.tvSignUp);
        tvError = findViewById(R.id.tvErrorMessage);

        //Continue with Facebook button
        btnFacebook.setReadPermissions("email", "public_profile");
        btnFacebook.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }
            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
            }
        });
        //Hide password checkbox
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (!isChecked) {
                    etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                } else {
                    etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());

                }
            }
        });
        //Moves over to the sign up page
        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Clicked Sign Up");
                Intent i = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(i);
            }
        });
    }
    //Upon clicking login, attempts to sign in a user with the given email address and password
    public void clicked(View view) {
        Log.i(TAG, "Clicked login");

        mAuth.signInWithEmailAndPassword(etUsername.getText().toString(), etPassword.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                            showError(errorCode);
                        }
                    }
                });
    }
    /*After authenticating with fb, we can refer back to the registered callback in line 93.
    If successful, prompts the handleFacebookAccessToken method.*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }
    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            final FirebaseUser user = mAuth.getCurrentUser();
                            if (task.getResult().getAdditionalUserInfo().isNewUser()) {
                                DatabaseReference db = FirebaseDatabase.getInstance().getReference("users");
                                HashMap<String, Object> map = new HashMap<>();
                                map.put("likes", "none");
                                db.child(user.getUid()).updateChildren(map);

                            }
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    //After login/signup, move over to the main activity
    protected void updateUI(FirebaseUser user) {
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
            overridePendingTransition(R.anim.do_not_move, R.anim.do_not_move);
            finish();
    }
    //Uses the established dictionary of error codes to display a message to users
    protected void showError(String errorCode) {
        String ec = (String) errorMessages.get(errorCode);
        if (ec != null)
            tvError.setText(ec);
    }
}