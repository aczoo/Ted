package com.example.ted;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import ai.api.android.AIDataService;
import ai.api.AIServiceContext;
import ai.api.AIServiceContextBuilder;
import ai.api.android.AIConfiguration;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;

import java.util.UUID;


// removed implements AIListener
public class ChatActivity extends AppCompatActivity {
    private static final String TAG = ChatActivity.class.getSimpleName();
    private static final int USER = 10001;
    private static final int BOT = 10002;
    private final int audioRequest = 2222;
    private String uuid = UUID.randomUUID().toString();

    private AIDataService aiDataService;
    private AIServiceContext customAIServiceContext;
    private AIRequest aiRequest;
    private LinearLayout llChat;
    private EditText etQuery;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        /*int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission to record denied");
            makeRequest();
        }*/

        final ScrollView svChat = findViewById(R.id.chatScrollView);
        svChat.post(new Runnable() {
            @Override
            public void run() {
                svChat.fullScroll(View.FOCUS_DOWN);
            }
        });
        llChat = findViewById(R.id.chatLayout);
        final ImageView btnSend = findViewById(R.id.sendBtn);
        btnSend.setOnClickListener( new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                sendMessage(view);

            }

        });
        etQuery = findViewById(R.id.queryEditText);
        etQuery.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER:
                            ChatActivity.this.sendMessage(btnSend);
                            return true;
                        default:
                            break;
                    }
                }
                return false;
            }
        });


        final AIConfiguration config = new AIConfiguration("AIzaSyAK8h56gc1CvpvfgEaaRC5tr6YnPyU6UJg",
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);
        aiDataService = new AIDataService(this, config);
        customAIServiceContext = AIServiceContextBuilder.buildFromSessionId(uuid);
        aiRequest = new AIRequest();
        //aiService = AIService.getService(this, config);
        //aiService.setListener(this);

    }
    private void sendMessage(View view) {
        String msg = etQuery.getText().toString();
        if (msg.trim().isEmpty()) {
            Toast.makeText(ChatActivity.this, "Please enter your query!", Toast.LENGTH_LONG).show();
        } else {
            showTextView(msg, USER);
            etQuery.setText("");
            aiRequest.setQuery(msg);
            ChatClient request = new ChatClient(ChatActivity.this, (ai.api.android.AIDataService) aiDataService, customAIServiceContext);
            request.execute(aiRequest);

        }
    }
    public void callback(AIResponse aiResponse) {
        if (aiResponse != null) {
            // process aiResponse here
            String botReply = aiResponse.getResult().getFulfillment().getSpeech();
            Log.d(TAG, "Bot Reply: " + botReply);
            showTextView(botReply, BOT);
        } else {
            Log.d(TAG, "Bot Reply: Null");
            showTextView("There was some communication issue. Please Try again!", BOT);
        }
    }
    private void showTextView(String message, int type) {
        FrameLayout layout;
        switch (type) {
            case USER:
                layout = getUserLayout();
                break;
            case BOT:
                layout = getBotLayout();
                break;
            default:
                layout = getBotLayout();
                break;
        }
        layout.setFocusableInTouchMode(true);
        llChat.addView(layout); // move focus to text view to automatically make it scroll up if softfocus
        TextView tv = layout.findViewById(R.id.chatMsg);
        tv.setText(message);
        layout.requestFocus();
        etQuery.requestFocus(); // change focus back to edit text to continue typing
    }

    FrameLayout getUserLayout() {
        LayoutInflater inflater = LayoutInflater.from(ChatActivity.this);
        return (FrameLayout) inflater.inflate(R.layout.item_message, null);
    }

    FrameLayout getBotLayout() {
        LayoutInflater inflater = LayoutInflater.from(ChatActivity.this);
        return (FrameLayout) inflater.inflate(R.layout.item_bot_message, null);
    }



     /* protected void makeRequest() {
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
    }*/

   /* public void buttonClicked(View view) {
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

    }*/
}