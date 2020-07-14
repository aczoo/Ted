package com.example.ted.models;

import android.graphics.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Article {
    private String source, author, title, description, publishedAt, articleURL, imageURL;
    public Article() {}

    public Article(JSONObject jsonObject) throws JSONException {


    }
    public static List<Article> fromJsonArray(JSONArray articleJson) throws JSONException {
        List<Article> l=new ArrayList<Article>();
        for(int i = 0; i<articleJson.length();i++){
            l.add(new Article(articleJson.getJSONObject(i)));
        }
        return l;
    }


}
