package com.example.ted;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.codepath.asynchttpclient.AsyncHttpClient;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.example.ted.adapters.ArticleAdapter;
import com.example.ted.models.Article;
import com.facebook.shimmer.ShimmerFrameLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String base_url = "https://content.guardianapis.com/search?q=";
    //private static final String court_url = "https://www.courtlistener.com/api/rest/v3/opinions/?court__id=cafc&court__id=scotus/?order_by=-date_created";
    private static final String API_KEY = "9dc64de8-158b-4a95-8b5d-c0f520e2abd0";
    private FirebaseUser user;
    private RecyclerView rvArticles;
    private ShimmerFrameLayout shimmerFrameLayout;
    private FloatingActionButton fab;
    private List<Article> articles;

    //Adds profile image to tool bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        user = FirebaseAuth.getInstance().getCurrentUser();
        final MenuItem profile = menu.findItem(R.id.icon_profile);
        Glide.with(this).asBitmap().load(user.getPhotoUrl()).circleCrop()
                .thumbnail(Glide.with(this).asBitmap().load(R.drawable.com_facebook_profile_picture_blank_portrait).circleCrop())
                .into(new SimpleTarget<Bitmap>(100,100) {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                profile.setIcon(new BitmapDrawable(getResources(),resource));
            }
        });
        return true;
    }
    //If user clicks on their pfp, they are directed to the profile activity
    public void goProfile(MenuItem menuItem){
        Intent i = new Intent(this, ProfileActivity.class);
        startActivity(i);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Locate necessary components
        rvArticles = findViewById(R.id.rvArticles);
        fab = findViewById(R.id.fab);
        shimmerFrameLayout= findViewById(R.id.shimmerFrameLayout);
        //Sets up LocalBroadcastManagers which watch for notifications on the events that the user updates their pfp or logs out
        //Registering observers, updateMenu and logoutReceiver, to receive intents with the specified action names
        LocalBroadcastManager.getInstance(this).registerReceiver(updateMenu, new IntentFilter("newpfp"));
        LocalBroadcastManager.getInstance(this).registerReceiver(logoutReceiver, new IntentFilter("logout"));
        //Set up for the recycler view
        articles = new ArrayList<>();
        final ArticleAdapter aa = new ArticleAdapter(this, articles);
        rvArticles.setAdapter(aa);
        rvArticles.setLayoutManager(new LinearLayoutManager(this));
        //Establishes HTTP client
        AsyncHttpClient client = new AsyncHttpClient();
        Log.d(TAG, "Guardian url: "+ getURL());
        client.get(getURL(), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.d(TAG, "Success in Guardian data");
                /*If data is received, then take the json file, add all articles into the list,
                and update the recycler view that data has been added*/
                JSONObject jsonObject = json.jsonObject;
                try {
                    JSONArray response = jsonObject.getJSONObject("response").getJSONArray("results");
                    articles.addAll(Article.fromJsonArray(response));
                    aa.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                /*Once the recycler view has been populated, we can stop displaying the placeholder main page
                (shimmerframelayout from fb open source)*/
                shimmerFrameLayout.stopShimmerAnimation();
                shimmerFrameLayout.setVisibility(View.GONE);

            }
            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.d(TAG, "Failure in Guardian data");
            }
        });
        //Upon clicking the floating action bubble, users enter the chat activity
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, ChatActivity.class);
                //specifies starting coordinates for circular reveal animation, middle of the floating action bubble
                i.putExtra("x", fab.getRight()-fab.getWidth()/2);
                i.putExtra("y",fab.getBottom()+fab.getHeight()/2);
                startActivity(i);
            }
        });
        // When the user is scrolling through articles, the floating action will be hidden
        rvArticles.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 || dy < 0 && fab.isShown())
                    fab.hide();
            }
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE)
                    fab.show();
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
    }
    @Override
    public void onResume() {
        super.onResume();
        shimmerFrameLayout.startShimmerAnimation();
    }

    @Override
    protected void onPause() {
        shimmerFrameLayout.stopShimmerAnimation();
        super.onPause();
    }
    // Builds off of the basic guardian search call
    public String getURL() {
        Uri baseUri = Uri.parse(base_url);
        //Add query parameters to the base url
        Uri.Builder uriBuilder = baseUri.buildUpon();
        //Limits to legal news
        uriBuilder.appendQueryParameter("section", "law");
        //Expands request to 20 articles
        uriBuilder.appendQueryParameter("page-size", "20");
        //Orders articles by publishing date
        uriBuilder.appendQueryParameter("order-by", "newest");
        //Asks for date of publish not last update
        uriBuilder.appendQueryParameter("use-date", "published");
        //Asks for the author and publication
        uriBuilder.appendQueryParameter("show-tags", "contributor,publication");
        //Asks for the thumbnail image and the body of text
        uriBuilder.appendQueryParameter("show-fields", "thumbnail,body");
        uriBuilder.appendQueryParameter("api-key", API_KEY);
        return uriBuilder.toString().replace("&=", "");
    }
    /*Handler for broadcasted actions under the name "newpfp"
    If the user uploads a new pfp in the profile activity, we want to invalidate the options menu such that upon
    reentering the main activity, users can see that the update has been made*/
    public BroadcastReceiver updateMenu = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            invalidateOptionsMenu();
        }
    };
    /*Handler for broadcasted actions under the name "logout"
    If the user logs out in the profile activity, we want to also close out this activity such that upon
    being redirected to the login activity, users can not go back to the main page*/
    public BroadcastReceiver logoutReceiver= new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };
}