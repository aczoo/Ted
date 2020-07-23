package com.example.ted;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.facebook.login.LoginManager;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class ProfileActivity extends AppCompatActivity {
    private FirebaseUser user;
    private ImageView ivPfp;
    private TextView tvName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        user = FirebaseAuth.getInstance().getCurrentUser();
        ivPfp = findViewById(R.id.ivPfp);
        tvName = findViewById(R.id.tvName);

        customProfile(250);
        tvName.setText(user.getDisplayName());

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
    public void customProfile(int height){
        user = FirebaseAuth.getInstance().getCurrentUser();
        ivPfp = findViewById(R.id.ivPfp);
        for(UserInfo profile : user.getProviderData()){
            if (FacebookAuthProvider.PROVIDER_ID.equals(profile.getProviderId())){
                String fbUserId = profile.getUid();
                String photoUrl = "https://graph.facebook.com/" + fbUserId + "/picture?height="+height;
                Glide.with(ProfileActivity.this).load(photoUrl).circleCrop().placeholder(R.drawable.no_result).into(ivPfp);
            }

        }
    }

}