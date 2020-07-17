package com.example.ted;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codepath.asynchttpclient.AsyncHttpClient;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.example.ted.adapters.ArticleAdapter;
import com.example.ted.adapters.ChatActivity;
import com.example.ted.models.Article;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

public class MainActivity extends AppCompatActivity {
    List<Article> articles;
    private static final String TAG = "MainActivity";
    private static final String base_url = "https://content.guardianapis.com/search?q=";
    private static final String API_KEY = "9dc64de8-158b-4a95-8b5d-c0f520e2abd0";
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        articles = new ArrayList<>();
        RecyclerView rvArticles = findViewById(R.id.rvArticles);
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
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.d(TAG, "onFailure");
            }
        });
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
    public void goChat(MenuItem menuItem){
        Intent i = new Intent(this, ChatActivity.class);
        startActivity(i);
    }
}