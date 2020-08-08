package com.example.ted;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityOptionsCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codepath.asynchttpclient.AsyncHttpClient;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.example.ted.models.Article;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.Query;


import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.io.ByteArrayOutputStream;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import okhttp3.Headers;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";
    private static final int PICK_IMAGE_ID = 123;
    private static final String base_url = "https://content.guardianapis.com/";
    private static final String API_KEY = "9dc64de8-158b-4a95-8b5d-c0f520e2abd0";
    private FirebaseUser user;
    private ImageView ivPfp, ivEdit;
    private TextView tvName;
    private LinearLayout llActivity;
    DatabaseReference db = FirebaseDatabase.getInstance().getReference();
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        user = FirebaseAuth.getInstance().getCurrentUser();
        ivPfp = findViewById(R.id.ivPfp);
        ivEdit = findViewById(R.id.ivEdit);
        tvName = findViewById(R.id.tvName);
        llActivity = findViewById(R.id.llActivity);

        Glide.with(ProfileActivity.this).load(user.getPhotoUrl()).circleCrop()
                .thumbnail(Glide.with(ProfileActivity.this).load(R.drawable.com_facebook_profile_picture_blank_portrait).circleCrop()).into(ivPfp);
        tvName.setText("Hello " + user.getDisplayName() + "!");
        ivEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent chooseImageIntent = ImagePicker.getPickImageIntent(ProfileActivity.this);
                startActivityForResult(chooseImageIntent, PICK_IMAGE_ID);
            }
        });
        final DatabaseReference messagesDB = FirebaseDatabase.getInstance().getReference("users").child(user.getUid()).child("activity");
        Query messageQuery = messagesDB.orderByChild("timestamp");
        messageQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    RelativeLayout session = getHistoryLayout();
                    llActivity.addView(session);
                    TextView tv = session.findViewById(R.id.tvDescription);
                    tv.setText("Nothing so far!");
                    return;
                }
                for (final DataSnapshot message : snapshot.getChildren()) {
                    RelativeLayout session = getHistoryLayout();
                    llActivity.addView(session);
                    CardView cvHistory = session.findViewById(R.id.cvHistory);
                    final ImageView ivPicture = session.findViewById(R.id.ivPicture);
                    TextView tvDescription = session.findViewById(R.id.tvDescription);
                    if (!message.hasChild("bot")) {
                        if (message.hasChild("imageUrl")) {
                            Glide.with(ProfileActivity.this).load(message.child("imageUrl").getValue().toString()).transform(new RoundedCornersTransformation(2, 2)).into(ivPicture);
                        }
                        String title = (String) message.child("title").getValue();
                        if (title.length() > 25) {
                            title = title.substring(0, title.indexOf(" ", 17)) + "...";
                        }
                        tvDescription.setText("Liked " + title + " at " + message.child("timeLiked").getValue());
                        cvHistory.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                AsyncHttpClient client = new AsyncHttpClient();
                                client.get(getURL(message.getKey().replaceAll("@", "/")), new JsonHttpResponseHandler() {
                                    @Override
                                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                                        Log.d(TAG, "Successfully extracted article");
                                        JSONObject jsonObject = json.jsonObject;
                                        try {
                                            Article article = new Article(jsonObject.getJSONObject("response").getJSONObject("content"));
                                            Intent intent = new Intent(ProfileActivity.this, ArticleDetails.class);
                                            intent.putExtra(Article.class.getSimpleName(), Parcels.wrap(article));
                                            ActivityOptionsCompat options = ActivityOptionsCompat.
                                                    makeSceneTransitionAnimation(ProfileActivity.this, ivPicture, "thumbnail");
                                            startActivity(intent, options.toBundle());

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    @Override
                                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                                        Log.d(TAG, "Could not find the article");
                                    }
                                });
                            }
                        });
                    } else {
                        Glide.with(ProfileActivity.this).load(getResources().getIdentifier("ted", "drawable", getPackageName())).into(ivPicture);
                        tvDescription.setText("Chat session started with Ted at " + message.child("sessionStart").getValue());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PICK_IMAGE_ID:
                if (resultCode == RESULT_OK) {
                    Bitmap bitmap = ImagePicker.getImageFromResult(this, resultCode, data);
                    UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder().setPhotoUri(getImageUri(this, bitmap)).build();
                    user.updateProfile(profileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.d(TAG, "complete");
                            Glide.with(ProfileActivity.this).load(user.getPhotoUrl()).circleCrop()
                                    .thumbnail(Glide.with(ProfileActivity.this).load(R.drawable.com_facebook_profile_picture_blank_portrait).circleCrop()).into(ivPfp);
                            Intent i = new Intent("newpfp");
                            LocalBroadcastManager.getInstance(ProfileActivity.this).sendBroadcast(i);
                        }
                    });
                }
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

    public RelativeLayout getHistoryLayout() {
        LayoutInflater inflater = LayoutInflater.from(ProfileActivity.this);
        return (RelativeLayout) inflater.inflate(R.layout.item_history, null);
    }

    public String getURL(String id) {
        Uri baseUri = Uri.parse(base_url + id);
        //add query parameters to the base url
        Uri.Builder uriBuilder = baseUri.buildUpon();
        uriBuilder.appendQueryParameter("use-date", "published");
        uriBuilder.appendQueryParameter("show-tags", "contributor,publication");
        uriBuilder.appendQueryParameter("show-fields", "thumbnail,body");
        uriBuilder.appendQueryParameter("api-key", API_KEY);
        Log.d(TAG, uriBuilder.toString().replace("&=", ""));
        return uriBuilder.toString().replace("&=", "");
    }


}