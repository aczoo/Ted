package com.example.ted;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.io.ByteArrayOutputStream;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";
    private static final int PICK_IMAGE_ID = 123;
    private FirebaseUser user;
    private ImageView ivPfp, ivEdit;
    private TextView tvName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        user = FirebaseAuth.getInstance().getCurrentUser();
        ivPfp = findViewById(R.id.ivPfp);
        ivEdit = findViewById(R.id.ivEdit);
        tvName = findViewById(R.id.tvName);

        Glide.with(ProfileActivity.this).load(user.getPhotoUrl()).circleCrop().placeholder(R.drawable.blankpfp).into(ivPfp);
        tvName.setText("Hello " + user.getDisplayName());
        ivEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent chooseImageIntent = ImagePicker.getPickImageIntent(ProfileActivity.this);
                startActivityForResult(chooseImageIntent, PICK_IMAGE_ID);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PICK_IMAGE_ID:
                Bitmap bitmap = ImagePicker.getImageFromResult(this, resultCode, data);
                UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder().setPhotoUri(getImageUri(this, bitmap)).build();
                user.updateProfile(profileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d(TAG, "complete");
                        Glide.with(ProfileActivity.this).load(user.getPhotoUrl()).circleCrop().placeholder(R.drawable.blankpfp).into(ivPfp);
                        Intent i = new Intent("newpfp");
                        LocalBroadcastManager.getInstance(ProfileActivity.this).sendBroadcast(i);
                    }
                });
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    public void logout(View view) {
        //sign out of user account
        FirebaseAuth.getInstance().signOut();
        LoginManager.getInstance().logOut();
        //close out the main activity
        Intent i = new Intent("logout");
        LocalBroadcastManager.getInstance(view.getContext()).sendBroadcast(i);
        //jump back to the login activity
        i = new Intent(this, LoginActivity.class);
        startActivity(i);
        //end the profile page
        finish();
    }


    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
    public void customProfile(FirebaseUser user, int height) {
        for (UserInfo profile : user.getProviderData()) {
            if (FacebookAuthProvider.PROVIDER_ID.equals(profile.getProviderId())) {
                String fbUserId = profile.getUid();
                String photoUrl = "https://graph.facebook.com/" + fbUserId + "/picture?height=" + height;
                UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder().setPhotoUri(Uri.parse(photoUrl)).build();
                user.updateProfile(profileChangeRequest);
            }

        }
    }


}