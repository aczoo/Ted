package com.example.ted.clients;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.example.ted.ChatActivity;
import com.google.cloud.dialogflow.v2beta1.DetectIntentRequest;
import com.google.cloud.dialogflow.v2beta1.DetectIntentResponse;
import com.google.cloud.dialogflow.v2beta1.QueryInput;
import com.google.cloud.dialogflow.v2beta1.QueryResult;
import com.google.cloud.dialogflow.v2beta1.QueryParameters;
import com.google.cloud.dialogflow.v2beta1.SessionName;
import com.google.cloud.dialogflow.v2beta1.SessionsClient;
import com.google.cloud.dialogflow.v2beta1.KnowledgeAnswers.Answer;
import com.google.cloud.language.v1.AnalyzeEntitiesRequest;
import com.google.cloud.language.v1.AnalyzeEntitiesResponse;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.EncodingType;
import com.google.cloud.language.v1.Entity;
import com.google.cloud.language.v1.EntityMention;
import com.google.cloud.language.v1.LanguageServiceClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class ChatClient extends AsyncTask<Void, Void, String> {

    Activity activity;
    private String TAG = "ChatClient";
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
                if (queryResult.getIntentDetectionConfidence() < knowledgeAnswer.getMatchConfidence() && knowledgeAnswer.getMatchConfidence() >= .90) {
                    return knowledgeAnswer.getAnswer();
                }
            }
            if (queryResult.getIntentDetectionConfidence()<.1){
                return duckResponse();
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

    private String duckResponse() {
        OkHttpClient duck = new OkHttpClient();
        msg = extractEntity().replace(" ", "%20");
        String url = "https://api.duckduckgo.com/?q=" + msg + "&format=json&pretty=1";
        Request request = new Request.Builder().url(url).build();
        try {
            Response response = duck.newCall(request).execute();
            JSONObject body = new JSONObject(response.body().string());
            if (body.get("Abstract").toString().length() == 0) {
                Log.d("duck", body.getJSONArray("RelatedTopics").getJSONObject(0).toString());
                return body.getJSONArray("RelatedTopics").getJSONObject(0).get("Text").toString();

            }
            return body.get("Abstract").toString();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String extractEntity() {
        try (LanguageServiceClient language = LanguageServiceClient.create()) {
            Document doc = Document.newBuilder().setContent(msg).setType(Document.Type.PLAIN_TEXT).build();
            AnalyzeEntitiesRequest request =
                    AnalyzeEntitiesRequest.newBuilder()
                            .setDocument(doc)
                            .setEncodingType(EncodingType.UTF16)
                            .build();
            AnalyzeEntitiesResponse response = language.analyzeEntities(request);

            for (Entity entity : response.getEntitiesList()) {
                Log.d(TAG, "Entity: " + entity.getName());
                Log.d(TAG, "Salience: " + entity.getSalience());
                System.out.println("Metadata: ");
                for (Map.Entry<String, String> entry : entity.getMetadataMap().entrySet()) {
                    Log.d(TAG, entry.getKey() + " " + entry.getValue());
                }
            }
            return String.valueOf(response.getEntitiesList().get(0));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}