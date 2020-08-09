package com.example.ted.models;


import android.text.format.DateUtils;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

@Parcel
public class Article {
    private static final String TAG = "Article";
    String id, author, title, body, publishedAt, articleUrl, imageUrl, shortPublishedAt;
    //Would have used a dictionary for the hyperlinks and indexes, but it couldn't be added to the database
    //Two lists function in a similar sense, a link and its corresponding indexes hold the same spot in their respective lists
    List<int[]> index;
    List<String> links;
    StringBuilder sb;

    //Default constructor for class to be parcelable
    public Article() {
    }
    //Creates article object from a JSONObject
    public Article(JSONObject jsonObject) throws JSONException {
        id = jsonObject.getString("id");
        if (jsonObject.getJSONArray("tags").length()!=0)
            author = jsonObject.getJSONArray("tags").getJSONObject(0).getString("webTitle");
        title = jsonObject.getString("webTitle");
        body = jsonObject.getJSONObject("fields").getString("body");
        getRelativeTimeAgo(jsonObject.getString("webPublicationDate"));
        articleUrl = jsonObject.getString("webUrl");
        if (jsonObject.getJSONObject("fields").has("thumbnail")){
            imageUrl = jsonObject.getJSONObject("fields").getString("thumbnail");
        }
        index = new ArrayList<>();
        links = new ArrayList<>();
        clean();
    }
    //Creates a list of articles, which is used in the main page recyclerview, from the given JSONArray
    public static List<Article> fromJsonArray(JSONArray articleJson) throws JSONException {
        List<Article> l = new ArrayList<>();
        for (int i = 0; i < articleJson.length(); i++) {
            Log.d(TAG ,"fromJsonArray: "+articleJson.getJSONObject(i).toString());
            l.add(new Article(articleJson.getJSONObject(i)));
        }
        return l;
    }
    //Default getters/setters for class to be parcelable
    public String getId() { return id; }
    public String getAuthor() {
        return author;
    }
    public String getTitle() {
        return title;
    }
    public String getImageUrl() {
        return imageUrl;
    }
    public String getArticleUrl() {
        return articleUrl;
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

    //Clean the supplied article body from the Guardian API
    private void clean() {
        Log.d(TAG, "clean: " + title);
        //Replace given paragraph tags with new lines
        body = body.replaceAll("<p>", "\n");
        body = body.replaceAll("</p>", "\n");
        //Delete all cases of the aside class tag, which has no visible context in the website version
        //Finds the opening and closing tag indexes, deletes the inbetween, cycles again if there is another case
        sb = new StringBuilder(body);
        int index = sb.indexOf("<aside class=");
        while(index>=0){
            int index2 =sb.indexOf("</aside>", index);
            sb.delete(index, index2+8);
            index = sb.indexOf("<aside class=");
        }
        //Extracts text from the supplied hyper text reference and adds
        //Finds the opening tag
        index = sb.indexOf("<a href=\"");
        while (index >= 0) {
            //If a href tag exists, then delete the first half of the opening
            sb.delete(index, index + 9);
            //Add the following text to the list of hyperlinks
            setLink(index);
            //Cycle through the rest of the text
            index = sb.indexOf("<a href=\"");
        }

        body = sb.toString();
    }
    //Extracts the hyperlink url from the rest of the tag
    private void setLink(int i) {
        //Locate the end of the tag
        int i2 = sb.indexOf("\">", i);
        //Notes the link and then removes it from the article body
        String link = sb.substring(i, i2);
        sb.delete(i, i2 + 2);
        //Finds the end of the href tag, where the in between phrase that will act as the anchor text for the hyperlink
        i2 = sb.indexOf("</a>");
        sb.delete(i2, i2 + 4);
        //Add the link and start/end indexes to the respective lists
        links.add(link);
        index.add(new int[]{i, i2});
    }
    //Converts the given publish timestamp from the Guardian and generates a shortened version to be displayed on the main page
    public void getRelativeTimeAgo(String rawJsonDate) {
        //Strip text of the "Z", which just means "Zulu Time"/UTC
        rawJsonDate= rawJsonDate.replace("Z","");
        //Establish the starting format, which will be used to extract a date object
        String format = "yyyy-MM-dd'T'hh:mm:ss";
        //Establish the desired format, which will be used to format the date object
        String detailedformat ="h:mm a d MMM yy";
        SimpleDateFormat sf = new SimpleDateFormat(format, Locale.ENGLISH);
        SimpleDateFormat sf2 = new SimpleDateFormat(detailedformat, Locale.ENGLISH);
        sf.setLenient(true);
        try {
            //Converts string to date
            Date dateMillis = sf.parse(rawJsonDate);
            //Calculates offset of user's timezone to that of UTC
            TimeZone tz = TimeZone.getDefault();
            int currentOffsetFromUTC = tz.getRawOffset() + (tz.inDaylightTime(dateMillis) ? tz.getDSTSavings() : 0);
            //Populates the detailed version of the timestamp
            publishedAt = sf2.format(dateMillis.getTime()+currentOffsetFromUTC);
            //Populates the relative version of the timestamp, using the user's offest from UTC
            shortPublishedAt=DateUtils.getRelativeTimeSpanString(dateMillis.getTime()+currentOffsetFromUTC,
                    System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS, DateUtils.FORMAT_ABBREV_RELATIVE).toString();
        } catch (ParseException e) {
            e.printStackTrace();
        }

}


}
