package com.example.ted.models;

import android.graphics.Movie;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Article {
    private final String TAG = "Article";
    String author, title, body, publishedAt, articleUrl, imageUrl;

    public Article() {}

    public Article(JSONObject jsonObject) throws JSONException {
        author =jsonObject.getJSONArray("tags").getJSONObject(0).getString("webTitle");
        title = jsonObject.getString("webTitle");
        body = jsonObject.getJSONObject("fields").getString("body");
        publishedAt = jsonObject.getString("webPublicationDate");
        articleUrl = jsonObject.getString("webUrl");
        imageUrl = jsonObject.getJSONObject("fields").getString("thumbnail");


    }
    public static List<Article> fromJsonArray(JSONArray articleJson) throws JSONException {
        List<Article> l= new ArrayList<>();
        for(int i = 0; i<articleJson.length();i++){
            l.add(new Article(articleJson.getJSONObject(i)));
        }
        return l;
    }

    public String getAuthor() {
        return author;
    }
    public String getTitle(){
        return title;
    }
    public String getImageUrl(){
        return imageUrl;
    }
}
