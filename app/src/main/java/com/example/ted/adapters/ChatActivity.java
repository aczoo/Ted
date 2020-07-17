package com.example.ted.adapters;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ted.R;

import ai.api.AIListener;
import ai.api.AIServiceContextBuilder;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.model.Result;

import com.google.gson.JsonElement;

import org.w3c.dom.Text;

import java.util.Map;

public class ChatActivity extends AppCompatActivity implements AIListener {
    private static String TAG = "ChatActivity";
    private AIService aiService;
    private final int audioRequest = 2222;
    TextView tvResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission to record denied");
            makeRequest();
        }
        tvResponse = findViewById(R.id.tvResponse);
        final AIConfiguration config = new AIConfiguration("AIzaSyAK8h56gc1CvpvfgEaaRC5tr6YnPyU6UJg",
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);
        aiService = AIService.getService(this, config);
        aiService.setListener(this);

    }

    protected void makeRequest() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, audioRequest);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case audioRequest: {
                if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Permission has been denied");
                    Toast.makeText(getApplicationContext(), "Permission denied", Toast.LENGTH_SHORT).show();

                } else
                    Log.i(TAG, "Permission has been granted");
            }
             return;
        }
    }

    public void buttonClicked(View view) {
        Toast.makeText(getApplicationContext(), "Listening", Toast.LENGTH_SHORT).show();
        aiService.startListening();
    }

    @Override
    public void onResult(AIResponse response) {
        Result result = response.getResult();
        String parameterString = "";
        if (result.getParameters() != null && !result.getParameters().isEmpty()) {
            for (final Map.Entry<String, JsonElement> entry : result.getParameters().entrySet()) {
                parameterString += "(" + entry.getKey() + ", " + entry.getValue() + ") ";
            }
        }

        tvResponse.setText("Query:" + result.getResolvedQuery() +
                "\nAction: " + result.getAction() +
                "\nParameters: " + parameterString);
        //tvResponse.setText("Query: " + response.getResolvedQuery() + " action: " + response.getAction());

    }

    @Override
    public void onError(AIError error) {
        tvResponse.setText(error.toString());

    }

    @Override
    public void onAudioLevel(float level) {

    }

    @Override
    public void onListeningStarted() {

    }

    @Override
    public void onListeningCanceled() {

    }

    @Override
    public void onListeningFinished() {

    }
}