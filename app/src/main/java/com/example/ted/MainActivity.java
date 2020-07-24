package com.example.ted;

import android.animation.Animator;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.codepath.asynchttpclient.AsyncHttpClient;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.example.ted.adapters.ArticleAdapter;
import com.example.ted.clients.ChatClient;
import com.example.ted.models.Article;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

public class MainActivity extends AppCompatActivity {
    private List<Article> articles;
    private static final String TAG = "MainActivity";
    private static final String base_url = "https://content.guardianapis.com/search?q=";
    private static final String API_KEY = "9dc64de8-158b-4a95-8b5d-c0f520e2abd0";
    private FirebaseUser user;
    private RecyclerView rvArticles;
    private ShimmerFrameLayout shimmerFrameLayout;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        user = FirebaseAuth.getInstance().getCurrentUser();
        final MenuItem profile = menu.findItem(R.id.icon_profile);
        Glide.with(this).asBitmap().load(user.getPhotoUrl()).circleCrop().into(new SimpleTarget<Bitmap>(100,100) {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                profile.setIcon(new BitmapDrawable(getResources(),resource));
            }
        });
        return true;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LocalBroadcastManager.getInstance(this).registerReceiver(updateMenu, new IntentFilter("newpfp"));
        LocalBroadcastManager.getInstance(this).registerReceiver(logoutReceiver, new IntentFilter("logout"));
        shimmerFrameLayout= findViewById(R.id.shimmerFrameLayout);
        articles = new ArrayList<>();
        rvArticles = findViewById(R.id.rvArticles);
        final ArticleAdapter aa = new ArticleAdapter(this, articles);
        rvArticles.setAdapter(aa);
        rvArticles.setLayoutManager(new LinearLayoutManager(this));
        AsyncHttpClient client = new AsyncHttpClient();
        Log.d(TAG, "Guardian url: "+ getURL(base_url));
        client.get(getURL(base_url), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.d(TAG, "onSuccess");
                JSONObject jsonObject = json.jsonObject;
                try {
                    JSONArray response = jsonObject.getJSONObject("response").getJSONArray("results");
                    articles.addAll(Article.fromJsonArray(response));
                    aa.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                shimmerFrameLayout.stopShimmerAnimation();
                shimmerFrameLayout.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.d(TAG, "onFailure");
            }
        });
        final FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, ChatActivity.class);
                i.putExtra("x", fab.getRight()-fab.getWidth()/2);
                i.putExtra("y",fab.getBottom()+fab.getHeight()/2);
                startActivity(i);
            }
        });
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

    public String getURL(String base) {
        Uri baseUri = Uri.parse(base);
        //add query parameters to the base url
        Uri.Builder uriBuilder = baseUri.buildUpon();
        uriBuilder.appendQueryParameter("section", "law");
        uriBuilder.appendQueryParameter("page-size", "20");
        uriBuilder.appendQueryParameter("order-by", "newest");
        uriBuilder.appendQueryParameter("use-date", "published");
        uriBuilder.appendQueryParameter("show-tags", "contributor,publication");
        uriBuilder.appendQueryParameter("show-fields", "thumbnail,body");
        uriBuilder.appendQueryParameter("api-key", API_KEY);
        return uriBuilder.toString().replace("&=", "");
    }
    public void goProfile(MenuItem menuItem){
        Intent i = new Intent(this, ProfileActivity.class);
        startActivity(i);
    }
    public BroadcastReceiver logoutReceiver= new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };
    public BroadcastReceiver updateMenu = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            invalidateOptionsMenu();
        }
    };
}