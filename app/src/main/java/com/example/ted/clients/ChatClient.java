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


public class ChatClient extends AsyncTask<Void, Void, String> {

    Activity activity;
    private SessionName session;
    private SessionsClient sessionsClient;
    private QueryInput queryInput;
    private QueryParameters queryParam;

    public ChatClient(Activity activity, SessionName session, SessionsClient sessionsClient, QueryInput queryInput, QueryParameters queryParam) {
        this.activity = activity;
        this.session = session;
        this.sessionsClient = sessionsClient;
        this.queryInput = queryInput;
        this.queryParam = queryParam;
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
                    return knowledgeAnswer.getAnswer();
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
}