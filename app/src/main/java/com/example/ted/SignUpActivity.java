package com.example.ted;

import androidx.annotation.NonNull;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;

public class SignUpActivity extends LoginActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        btnLogin.setText("Sign Up");
        btnFacebook.setVisibility(View.GONE);
        etName.setVisibility(View.VISIBLE);
        tvSignUp.setOnClickListener(null);
        tvSignUp.setVisibility(View.GONE);

    }

    @Override
    public void clicked(View view) {
        Log.i(TAG, "Clicked sign up");
        //saying mAuth is error
        mAuth.createUserWithEmailAndPassword(etUsername.getText().toString(),etPassword.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(etName.getText().toString()).build();
                            user.updateProfile(profileUpdates);
                            DatabaseReference db = FirebaseDatabase.getInstance().getReference("users");
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("likes","none");
                            db.child(user.getUid()).updateChildren(map);
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.d(TAG, "createUserWithEmail:failure", task.getException());
                            String errorCode = ((FirebaseAuthException)task.getException()).getErrorCode();
                            showError(errorCode);
                            updateUI(null);
                        }
                    }
                });





    }
}