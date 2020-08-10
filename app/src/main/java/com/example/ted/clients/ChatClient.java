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
import com.google.cloud.language.v1.LanguageServiceClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

//Backend of the chat with Ted, takes care of getting answers from Dialogflow and Duckduckgo
public class ChatClient extends AsyncTask<Void, Void, String> {

    Activity activity;
    private String TAG = "ChatClient";
    private SessionName session;
    private SessionsClient sessionsClient;
    private QueryInput queryInput;
    private QueryParameters queryParam;
    private String msg;

    //Constructor for the client
    //@param activity, details where to return the response
    //@param session, details the name for the session
    //@param sessionsClient, wrapper for the session, provides the ability to make remote calls
    //@param queryInput, message sent by the user
    //@param queryParameters, the parameters of the conversational query
    //specifies which knowledge bases to look at in addition to the intents
    public ChatClient(Activity activity, SessionName session, SessionsClient sessionsClient, QueryInput queryInput, QueryParameters queryParam, String msg) {
        this.activity = activity;
        this.session = session;
        this.sessionsClient = sessionsClient;
        this.queryInput = queryInput;
        this.queryParam = queryParam;
        this.msg = msg;
    }
    //Given the established chat client, finds the
    @Override
    protected String doInBackground(Void... voids) {
        try {
            //Instantiates the Dialogflow request
            DetectIntentRequest detectIntentRequest =
                    DetectIntentRequest.newBuilder()
                            .setSession(session.toString())
                            .setQueryInput(queryInput)
                            .setQueryParams(queryParam)
                            .build();
            //Gets the response from the session client
            DetectIntentResponse response = sessionsClient.detectIntent(detectIntentRequest);
            QueryResult queryResult = response.getQueryResult();
            //If there are results from the knowledge bases
            if (queryResult.hasKnowledgeAnswers()) {
                //Then inspect the answer with the highest confidence, which would be the first value in the sorted list
                Answer knowledgeAnswer = queryResult.getKnowledgeAnswers().getAnswersList().get(0);
                //If this answer has a confidence over 90% and is greater than the intent result
                //then return it to the chat activity to be displayed
                if (queryResult.getIntentDetectionConfidence() < knowledgeAnswer.getMatchConfidence() && knowledgeAnswer.getMatchConfidence() >= .90) {
                    return knowledgeAnswer.getAnswer();
                }
            }
            //Calls web scraper if there is no answer from the knowledge bases or intents in Dialogflow
            //Issue with the google nlp api authentication, so currently commented out
            /*if (queryResult.getIntent().getDisplayName().equals("Fallback Intent")){
                return duckResponse();
            }*/
            //If there are no suitable responses from the knowledge bases, then return the response from the intents
            return queryResult.getFulfillmentText();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    //Once a response is found, returns it to the chat activity to be displayed
    @Override
    protected void onPostExecute(String response) {
        ((ChatActivity) activity).callback(response);
    }
    //Determines the main topic of the user input and then uses Duckduckgo to search the web for a definition of said topic
    private String duckResponse() {
        //Establishes the http client
        OkHttpClient duck = new OkHttpClient();
        //Finds the main entity of the sentence/question
        msg = extractEntity().replace(" ", "%20");
        //If said entity exists, then make a request with the http client
        if (msg!=null){
        String url = "https://api.duckduckgo.com/?q=" + msg + "&format=json&pretty=1";
        Request request = new Request.Builder().url(url).build();
        try {
            Response response = duck.newCall(request).execute();
            JSONObject body = new JSONObject(response.body().string());
            //If Duckduckgo found any information, then return such
            if (body.get("Abstract").toString().length() == 0) {
                Log.d("duck", body.getJSONArray("RelatedTopics").getJSONObject(0).toString());
                return body.getJSONArray("RelatedTopics").getJSONObject(0).get("Text").toString();

            }
            return body.get("Abstract").toString();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        }
        return null;
    }
    //Finds the main topic of the user input using Google's Natural Language API
    private String extractEntity() {
        //Instantiate the language client
        try (LanguageServiceClient language = LanguageServiceClient.create()) {
            Document doc = Document.newBuilder().setContent(msg).setType(Document.Type.PLAIN_TEXT).build();
            AnalyzeEntitiesRequest request =
                    AnalyzeEntitiesRequest.newBuilder()
                            .setDocument(doc)
                            .setEncodingType(EncodingType.UTF16)
                            .build();
            AnalyzeEntitiesResponse response = language.analyzeEntities(request);
            //Logs each entity detected with its salience and metadata
            for (Entity entity : response.getEntitiesList()) {
                Log.d(TAG, "Entity: " + entity.getName());
                Log.d(TAG, "Salience: " + entity.getSalience());
                System.out.println("Metadata: ");
                for (Map.Entry<String, String> entry : entity.getMetadataMap().entrySet()) {
                    Log.d(TAG, entry.getKey() + " " + entry.getValue());
                }
            }
            //Returns the detected entity with the greatest salience (importance/confidence)
            return String.valueOf(response.getEntitiesList().get(0));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}