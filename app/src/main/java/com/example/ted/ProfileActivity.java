package com.example.ted;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityOptionsCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

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
    private static final String base_url = "https://content.guardianapis.com/";
    private static final String API_KEY = "9dc64de8-158b-4a95-8b5d-c0f520e2abd0";
    private static final int PICK_IMAGE_ID = 123;
    private ImageView ivPfp, ivEdit;
    private TextView tvName;
    private LinearLayout llActivity;
    private FirebaseUser user;

    //Back arrow in action bar, directs back to main page
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        //Sets up the title and back button for the action bar
        getSupportActionBar().setTitle("Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //Locate necessary components
        ivPfp = findViewById(R.id.ivPfp);
        tvName = findViewById(R.id.tvName);
        ivEdit = findViewById(R.id.ivEdit);
        llActivity = findViewById(R.id.llActivity);
        user = FirebaseAuth.getInstance().getCurrentUser();
        //Fills in user's name and pfp
        Glide.with(ProfileActivity.this).load(user.getPhotoUrl()).circleCrop()
                .thumbnail(Glide.with(ProfileActivity.this).load(R.drawable.com_facebook_profile_picture_blank_portrait).circleCrop()).into(ivPfp);
        tvName.setText("Hello " + user.getDisplayName() + "!");
        //If the add image is clicked, direct users to the image picker activity
        ivEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent chooseImageIntent = ImagePicker.getPickImageIntent(ProfileActivity.this);
                startActivityForResult(chooseImageIntent, PICK_IMAGE_ID);
            }
        });

        //Populate the activity section with the user's liked articles and chat sessions, sorted by the server time when added to firebase
        final DatabaseReference activityDB = FirebaseDatabase.getInstance().getReference("users").child(user.getUid()).child("activity");
        Query messageQuery = activityDB.orderByChild("timestamp");
        messageQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //If the user hasn't done anything yet, add a quick note of it
                if (!snapshot.exists()) {
                    RelativeLayout session = getHistoryLayout();
                    llActivity.addView(session);
                    TextView tv = session.findViewById(R.id.tvDescription);
                    tv.setText("Nothing so far!");
                    return;
                }
                //If the user has some activity on the app, then for each action, display a quick blurb about it.
                for (final DataSnapshot message : snapshot.getChildren()) {
                    //Inflate the history item layout and locate necessary components
                    RelativeLayout session = getHistoryLayout();
                    CardView cvHistory = session.findViewById(R.id.cvHistory);
                    TextView tvDescription = session.findViewById(R.id.tvDescription);
                    final ImageView ivPicture = session.findViewById(R.id.ivPicture);
                    //If the given activity item is a liked article and not a chat conversation, then add the relevant description,
                    // article thumbnail, and event listener for when clicked.
                    if (!message.hasChild("bot")) {
                        //Update the description with the article title and time liked
                        String title = (String) message.child("title").getValue();
                        //Shorten the title if necessary
                        if (title.length() > 25) {
                            title = title.substring(0, title.indexOf(" ", 17)) + "...";
                        }
                        tvDescription.setText("Liked " + title + " at " + message.child("timeLiked").getValue());
                        //Update the filler image with the article thumbnail
                        if (message.hasChild("imageUrl")) {
                            Glide.with(ProfileActivity.this).load(message.child("imageUrl").getValue().toString()).transform(new RoundedCornersTransformation(2, 2)).into(ivPicture);
                        }
                        //If this item is clicked, then request article information from the guardian api with the supplied id and
                        //then open the article details page.
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
                                            //Shared Element Activity Transition, uses a shared element between both activities to emphasize continuity
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
                    }
                    //if the given activity item is a chat conversation, then add the relevant description and image.
                    else {
                        Glide.with(ProfileActivity.this).load(getResources().getIdentifier("ted", "drawable", getPackageName())).into(ivPicture);
                        tvDescription.setText("Talked to Ted at " + message.child("sessionStart").getValue());
                    }
                    llActivity.addView(session,0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    //After opening the image selector activity and returning to the profile page
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            //if users intended on picking an image...
            case PICK_IMAGE_ID:
                //and if an image was successfully selected,
                if (resultCode == RESULT_OK) {
                    //then update the user's profile on firebase
                    Bitmap bitmap = ImagePicker.getImageFromResult(this, resultCode, data);
                    UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder().setPhotoUri(getImageUri(this, bitmap)).build();
                    /*Once complete, the profile picture displayed at the top will be updated and a notification will be sent out
                     via the LocalBroadcastManager to the main page*/
                    user.updateProfile(profileChangeRequest).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Log.d(TAG, "complete");
                            Glide.with(ProfileActivity.this).load(user.getPhotoUrl()).circleCrop()
                                    .thumbnail(Glide.with(ProfileActivity.this).load(R.drawable.com_facebook_profile_picture_blank_portrait).circleCrop()).into(ivPfp);
                            Intent i = new Intent("newpfp");
                            //Tells the main page to update the displayed user pfp
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
    //If the user clicks logout
    public void logout(View view) {
        //Signs out of user account
        FirebaseAuth.getInstance().signOut();
        LoginManager.getInstance().logOut();
        //Broadcasts a notification that the user has signed out, main activity will receive it and close itself
        Intent i = new Intent("logout");
        LocalBroadcastManager.getInstance(view.getContext()).sendBroadcast(i);
        //Direct users back to the login activity and closes the current page
        i = new Intent(this, LoginActivity.class);
        startActivity(i);
        finish();
    }

    //Converts bitmap of selected profile picture to a Uri
    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    //Not in use, but gets a higher definition version of the facebook authenticated user's profile picture
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

    //Inflates the history layout, which will show the user's activity
    public RelativeLayout getHistoryLayout() {
        LayoutInflater inflater = LayoutInflater.from(ProfileActivity.this);
        return (RelativeLayout) inflater.inflate(R.layout.item_history, null);
    }

    // Builds off of the basic guardian single item call
    public String getURL(String id) {
        //Specifies which article to pull
        Uri baseUri = Uri.parse(base_url + id);
        //Add query parameters to the base url
        Uri.Builder uriBuilder = baseUri.buildUpon();
        //Asks for date of publish not last update
        uriBuilder.appendQueryParameter("use-date", "published");
        //Asks for the author and publication
        uriBuilder.appendQueryParameter("show-tags", "contributor,publication");
        //Asks for the thumbnail image and the body of text
        uriBuilder.appendQueryParameter("show-fields", "thumbnail,body");
        uriBuilder.appendQueryParameter("api-key", API_KEY);
        return uriBuilder.toString().replace("&=", "");
    }


}