package com.example.ted;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.facebook.login.LoginManager;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
    }
    public void logout(View view){
        //sign out of user account
        FirebaseAuth.getInstance().signOut();
        LoginManager.getInstance().logOut();
        //close out the main activity
        Intent i= new Intent("logout");
        LocalBroadcastManager.getInstance(view.getContext()).sendBroadcast(i);
        //jump back to the login activity
        i = new Intent(this, LoginActivity.class);
        startActivity(i);
        //end the profile page
        finish();
    }

}