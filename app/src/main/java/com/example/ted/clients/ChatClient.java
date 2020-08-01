package com.example.ted.clients;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.example.ted.ChatActivity;
import com.google.api.client.json.Json;
import com.google.cloud.dialogflow.v2beta1.DetectIntentRequest;
import com.google.cloud.dialogflow.v2beta1.DetectIntentResponse;
import com.google.cloud.dialogflow.v2beta1.QueryInput;
import com.google.cloud.dialogflow.v2beta1.QueryResult;
import com.google.cloud.dialogflow.v2beta1.QueryParameters;
import com.google.cloud.dialogflow.v2beta1.SessionName;
import com.google.cloud.dialogflow.v2beta1.SessionsClient;
import com.google.cloud.dialogflow.v2beta1.KnowledgeAnswers.Answer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class ChatClient extends AsyncTask<Void, Void, String> {

    Activity activity;
    private SessionName session;
    private SessionsClient sessionsClient;
    private QueryInput queryInput;
    private QueryParameters queryParam;
    private String msg;

    public ChatClient(Activity activity, SessionName session, SessionsClient sessionsClient, QueryInput queryInput, QueryParameters queryParam, String msg) {
        this.activity = activity;
        this.session = session;
        this.sessionsClient = sessionsClient;
        this.queryInput = queryInput;
        this.queryParam = queryParam;
        this.msg = msg;
    }

    @Override
    protected String doInBackground(Void... voids) {
        try {
            DetectIntentRequest detectIntentRequest =
                    DetectIntentRequest.newBuilder()
                            .setSession(session.toString())
                            .setQueryInput(queryInput)
                            .setQueryParams(queryParam)
                            .build();
            DetectIntentResponse response = sessionsClient.detectIntent(detectIntentRequest);
            QueryResult queryResult = response.getQueryResult();
            if (queryResult.hasKnowledgeAnswers()) {
                Answer knowledgeAnswer = queryResult.getKnowledgeAnswers().getAnswersList().get(0);
                if (queryResult.getIntentDetectionConfidence() < knowledgeAnswer.getMatchConfidence()) {
                    if (knowledgeAnswer.getMatchConfidence()>.5 ){
                        return knowledgeAnswer.getAnswer();
                    }
                    return duckResponse();
                }
            }
            return queryResult.getFulfillmentText();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String response) {
        ((ChatActivity) activity).callback(response);
    }

    private String duckResponse(){
        OkHttpClient duck = new OkHttpClient();
        msg= msg.replace(" ","%20" );
        String url = "https://api.duckduckgo.com/?q="+msg  +"&format=json&pretty=1";
        Request request = new Request.Builder().url(url).build();
        try {
            Response response = duck.newCall(request).execute();
            JSONObject body = new JSONObject(response.body().string());
            if (body.get("Abstract").toString().length()==0){
                Log.d("duck", body.getJSONArray("RelatedTopics").getJSONObject(0).toString());
                return body.getJSONArray("RelatedTopics").getJSONObject(0).get("Text").toString();

            }
            return body.get("Abstract").toString();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}