package com.example.ted.models;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Movie;
import android.net.Uri;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

@Parcel
public class Article {
    private static final String TAG = "Article";
    String author, title, body, publishedAt, articleUrl, imageUrl, shortPublishedAt;
    List<int[]> index;
    List<String> links;
    StringBuilder sb;

    public Article() {
    }

    public Article(JSONObject jsonObject) throws JSONException {
        if (jsonObject.getJSONArray("tags").length()!=0)
            author = jsonObject.getJSONArray("tags").getJSONObject(0).getString("webTitle");
        title = jsonObject.getString("webTitle");
        body = jsonObject.getJSONObject("fields").getString("body");
        getRelativeTimeAgo(jsonObject.getString("webPublicationDate"));
        articleUrl = jsonObject.getString("webUrl");
        imageUrl = jsonObject.getJSONObject("fields").getString("thumbnail");
        index = new ArrayList<>();
        links = new ArrayList<>();
        clean();


    }

    public static List<Article> fromJsonArray(JSONArray articleJson) throws JSONException {
        List<Article> l = new ArrayList<>();
        for (int i = 0; i < articleJson.length(); i++) {
            Log.d(TAG ,"fromJsonArray: "+articleJson.getJSONObject(i).toString());
            l.add(new Article(articleJson.getJSONObject(i)));
        }
        return l;
    }

    public String getAuthor() {
        return author;
    }
    public String getTitle() {
        return title;
    }
    public String getImageUrl() {
        return imageUrl;
    }
    public String getBody() {
        return body;
    }
    public String getTime(){ return publishedAt;}
    public String getTimeAgo(){return shortPublishedAt; }
    public List getLinks() {
        return links;
    }
    public List getIndex() {
        return index;
    }
    private void clean() {
        Log.d(TAG, "clean: " + title);
        body = body.replaceAll("<p>", "\n");
        body = body.replaceAll("</p>", "\n");
        sb = new StringBuilder(body);
        int index = sb.indexOf("<a href=\"");
        while (index >= 0) {
            sb.delete(index, index + 9);
            setLink(index);
            index = sb.indexOf("<a href=\"");
        }
        body = sb.toString();
    }

    private void setLink(int i) {
        int i2 = sb.indexOf("\">", i);
        String link = sb.substring(i, i2);
        sb.delete(i, i2 + 2);
        i2 = sb.indexOf("</a>");
        sb.delete(i2, i2 + 4);
        links.add(link);
        index.add(new int[]{i, i2});
    }
    public void getRelativeTimeAgo(String rawJsonDate) {
        rawJsonDate= rawJsonDate.replace("T"," ");
        rawJsonDate= rawJsonDate.replace("Z","");
        String format = "yyyy-MM-dd hh:mm:ss";
        String detailedformat ="h:mm a d MMM yy";
        SimpleDateFormat sf = new SimpleDateFormat(format, Locale.ENGLISH);
        SimpleDateFormat sf2 = new SimpleDateFormat(detailedformat, Locale.ENGLISH);
        sf.setLenient(true);
        try {
            Date dateMillis = sf.parse(rawJsonDate);
            TimeZone tz = TimeZone.getDefault();
            int currentOffsetFromUTC = tz.getRawOffset() + (tz.inDaylightTime(dateMillis) ? tz.getDSTSavings() : 0);
            publishedAt = sf2.format(dateMillis.getTime()+currentOffsetFromUTC);
            shortPublishedAt=DateUtils.getRelativeTimeSpanString(dateMillis.getTime()+currentOffsetFromUTC,
                    System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE).toString();
        } catch (ParseException e) {
            e.printStackTrace();
        }

}


}
