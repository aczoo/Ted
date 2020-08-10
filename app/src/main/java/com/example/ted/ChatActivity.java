package com.example.ted;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.animation.Animator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.example.ted.clients.ChatClient;
import com.example.ted.models.ChatMessage;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.dialogflow.v2beta1.QueryInput;
import com.google.cloud.dialogflow.v2beta1.QueryParameters;
import com.google.cloud.dialogflow.v2beta1.SessionName;
import com.google.cloud.dialogflow.v2beta1.SessionsClient;
import com.google.cloud.dialogflow.v2beta1.SessionsSettings;
import com.google.cloud.dialogflow.v2beta1.TextInput;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

// removed implementation of AIListener, the speech to text component
public class ChatActivity extends AppCompatActivity {
    private static final String TAG = ChatActivity.class.getSimpleName();
    //Layout for all messages
    private LinearLayout llChat;
    //Displays user's ongoing message
    private EditText etQuery;
    private View chatLayout;
    //Coordinates of the floating action button
    private int fabx, faby;
    //Defines whether the message sent was from the user or Ted
    private static final int USER = 101;
    private static final int BOT = 102;
    private Date sessionStart;
    private SessionsClient sessionsClient;
    private SessionName session;
    private FirebaseUser user;
    private DatabaseReference userDB;
    //List of knowledge bases to be used in the query parameter
    private List<String> knowledgebases = Arrays.asList(
            "projects/" + session.getProject() + "/knowledgeBases/MzEwNTg4OTQ1MTAyNTM2NzA0MA",
            "projects/" + session.getProject() + "/knowledgeBases/NjE2MDk4MzY2Mzg3MDczODQzMg",
            "projects/" + session.getProject() + "/knowledgeBases/MTQ3NDg4MjY5ODQ3NTQ3MDg0OA");
    //Defines what knowledge bases to look into during the chat client call to dialogflow
    private QueryParameters queryParam=QueryParameters.newBuilder().addAllKnowledgeBaseNames(knowledgebases).build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        //Override the slide transition
        overridePendingTransition(R.anim.do_not_move, R.anim.do_not_move);
        chatLayout = findViewById(R.id.parentChat);
        //Take in the x and y coordinates of the fab from the intent, with the bottom right being the default value
        fabx = getIntent().getIntExtra("x", chatLayout.getRight());
        faby = getIntent().getIntExtra("y", chatLayout.getBottom());
        //Start the circular reveal animation for the chat activity
        if (savedInstanceState == null) {
            chatLayout.setVisibility(View.INVISIBLE);
            ViewTreeObserver viewTreeObserver = chatLayout.getViewTreeObserver();
            if (viewTreeObserver.isAlive()) {
                viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        circularRevealActivity();
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                            chatLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        } else {
                            chatLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    }
                });
            }
        }
        //Sets up LocalBroadcastManager which watch for notifications on the event that the user logs out
        //Registering the observer, logoutReceiver, to receive intents with the action name "logout"
        LocalBroadcastManager.getInstance(this).registerReceiver(logoutReceiver, new IntentFilter("logout"));
        //Locates necessary components of the chat activity
        final ScrollView svChat = findViewById(R.id.chatScrollView);
        svChat.post(new Runnable() {
            @Override
            public void run() {
                svChat.fullScroll(View.FOCUS_DOWN);
            }
        });
        llChat = findViewById(R.id.chatLayout);
        final ImageView btnSend = findViewById(R.id.sendBtn);
        etQuery = findViewById(R.id.queryEditText);
        //Sets a listener for the edit text to spawn the keyboard
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
        //Start the chat: establish the dialogflow client and populate the frame with previously sent messages or send a welcome message
        startChat();
    }
    //Circular Reveal Animation upon opening activity
    private void circularRevealActivity() {
        float finalRadius = Math.max(chatLayout.getWidth(), chatLayout.getHeight());
        Animator circularReveal = ViewAnimationUtils.createCircularReveal(chatLayout, fabx, faby, 0, finalRadius);
        circularReveal.setDuration(500);
        chatLayout.setVisibility(View.VISIBLE);
        circularReveal.start();
    }
    //Exiting the activity prompts another circular reveal animation
    @Override
    public void onBackPressed() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            float finalRadius = Math.max(chatLayout.getWidth(), chatLayout.getHeight());
            Animator circularReveal = ViewAnimationUtils.createCircularReveal(chatLayout, fabx, faby, finalRadius, 0);
            circularReveal.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    chatLayout.setVisibility(View.INVISIBLE);
                    finish();
                }
                @Override
                public void onAnimationCancel(Animator animator) {

                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            circularReveal.setDuration(500);
            circularReveal.start();
        } else {
            super.onBackPressed();
        }
    }
    //Called upon opening the chat activity
    private void startChat() {
        try {
            //Takes in information from the google cloud authentication key
            InputStream stream = getResources().openRawResource(getResources().getIdentifier("client_secrets", "raw", getPackageName()));
            GoogleCredentials credentials = GoogleCredentials.fromStream(stream);
            //Finds the dialogflow project from the established credentials
            String projectId = ((ServiceAccountCredentials) credentials).getProjectId();
            SessionsSettings.Builder settingsBuilder = SessionsSettings.newBuilder();
            SessionsSettings sessionsSettings = settingsBuilder.setCredentialsProvider(FixedCredentialsProvider.create(credentials)).build();
            //Establishes a session client
            sessionsClient = SessionsClient.create(sessionsSettings);
            session = SessionName.of(projectId, UUID.randomUUID().toString());
            //Takes note of the current time at which a chat is started with Ted
            sessionStart=new Date();
            //Access user's previous chat messages from firebase in chronological order
            user = FirebaseAuth.getInstance().getCurrentUser();
            userDB = FirebaseDatabase.getInstance().getReference("users").child(user.getUid());
            Query previous = userDB.child("messages").orderByChild("timestamp");
            previous.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    //If the user has no previous messages, then have Ted talk to the user first.
                    if (!snapshot.exists()){
                        welcomeMessage();
                        return;
                    }
                    //If the user has previous messages, then add them to the chat layout to be displayed
                    for (DataSnapshot message:snapshot.getChildren()){
                        //Display the time of each session, does nothing if the message was not the start of a session
                        showTimeStamp((String)message.child("sessionStart").getValue());
                        //Display the message
                        showTextView((String) message.child("msgText").getValue(), Math.toIntExact((long)message.child("bot").getValue()));
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    //Prompts the welcome intent so Ted can properly welcome the user for the first time
    private void welcomeMessage() {
        QueryInput queryInput = QueryInput.newBuilder().setText(TextInput.newBuilder().setText("Hi").setLanguageCode("en-US")).build();
        new ChatClient(ChatActivity.this, session, sessionsClient, queryInput, queryParam, "Hi").execute();
    }
    //Onclick method for the arrow button, handles displaying and sending the user input to firebase
    private void sendMessage(View view) {
        String msg = etQuery.getText().toString();
        //Handles empty message
        if (msg.trim().isEmpty()) {
            Toast.makeText(ChatActivity.this, "Messages can't be empty!", Toast.LENGTH_LONG).show();
        } else {
            Log.d(TAG, "sent message "+ msg);
            //Sends the message to the database
            messageToDB(msg, USER);
            //Displays the message
            showTextView(msg, USER);
            //Clears out the edit text
            etQuery.setText("");
            //Establishes a queryinput for the dialogflow request
            QueryInput queryInput = QueryInput.newBuilder().setText(TextInput.newBuilder().setText(msg).setLanguageCode("en-US")).build();
            //Creates a new chat client with the message which will handle the backend work
            new ChatClient(ChatActivity.this, session, sessionsClient, queryInput, queryParam, msg).execute();
            //Prompts a typing indicator for the bot
            showTextView(null, BOT);
        }
    }
    //Called when the chat client has found a response
    public void callback(String response) {
        //Delete the typing indicator bubble
        llChat.removeView(llChat.findViewById(1));
        //If there is an actual response
        if (response != null) {
            Log.d(TAG, "Bot Reply: " + response);
            //Save the bot's message in firebase
            messageToDB(response, BOT);
            //Display the bot's response
            showTextView(response, BOT);
        } else {
            Log.d(TAG, "Bot Reply: Null");
            //Display some kind of error message
            showTextView("There was some communication issue. Please Try again!", BOT);
        }
    }
    //Handles saving a message in firebase
    private void messageToDB(String msg, int isBot) {
        //Creates a chatmessage object and adds that to the user's messages database
        HashMap<String, Object> map = new HashMap<>();
        ChatMessage message = new ChatMessage(msg, ServerValue.TIMESTAMP, isBot, sessionStart);
        showTimeStamp(message.getSessionStart());
        map.put(UUID.randomUUID().toString(), message);
        userDB.child("messages").updateChildren(map);
        //If the message was the start of a session, add that to the user's activity database
        if (sessionStart !=null){
            userDB.child("activity").updateChildren(map);
            //Clarifies that all following messages will not be the start of a session
            sessionStart=null;
        }
    }
    //Inserts the timestamp into the chat layout
    private void showTimeStamp(String timestamp) {
        //If the message is the start of a session
        if(timestamp!=null){
            //Establishes the format of the parameter
            SimpleDateFormat sdf = new SimpleDateFormat("h:mm a d MMM", Locale.ENGLISH);
            TimeZone tz = TimeZone.getDefault();
            try {
                //Extract a date from the parameter and accounts for the user's offset from UTC
                Date temp = sdf.parse(timestamp);
                int currentOffsetFromUTC = tz.getRawOffset() + (tz.inDaylightTime(temp) ? tz.getDSTSavings() : 0);
                timestamp = sdf.format(temp.getTime()+currentOffsetFromUTC);
            } catch (ParseException e) {
                Log.d(TAG, "Could not parse timestamp");
                e.printStackTrace();
            }
            //Creates a textview for the timestamp
            TextView tvTime = new TextView(ChatActivity.this);
            tvTime.setText(timestamp);
            tvTime.setTextSize(10);
            tvTime.setTextColor(getResources().getColor(R.color.colorAccent));
            tvTime.setGravity(Gravity.CENTER_HORIZONTAL);
            tvTime.setPadding(0,10,0,10);
            tvTime.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            //Adds the text to the chat layout
            llChat.addView(tvTime);
        }
    }
    //Inserts a message into the chat layout accordingly
    private void showTextView(String message, int type) {
        FrameLayout layout;
        //Get the layout by the specified type
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
        //Move focus to text view to automatically make it scroll up if soft focus
        layout.setFocusableInTouchMode(true);
        //Add the message to the chat layout
        llChat.addView(layout);
        TextView tv = layout.findViewById(R.id.chatMsg);
        //If the message is an actual message, then set the chat bubble's text
        if (message != null) {
            layout.setId(0);
            tv.setText(message);
        }
        //If the message is supposed to be a typing indicator, then add a Lottie animation to the chat bubble
        else {
            layout.setId(1);
            tv.setVisibility(View.GONE);
            LottieAnimationView typing = layout.findViewById(R.id.typing);
            typing.setVisibility(View.VISIBLE);
        }
        layout.requestFocus();
        // change focus back to edit text to continue typing
        etQuery.requestFocus();
    }
    //Handles inflating the layout for a user message
    private FrameLayout getUserLayout() {
        LayoutInflater inflater = LayoutInflater.from(ChatActivity.this);
        return (FrameLayout) inflater.inflate(R.layout.item_message, null);
    }
    //Handles inflating the layout for a bot message
    private FrameLayout getBotLayout() {
        LayoutInflater inflater = LayoutInflater.from(ChatActivity.this);
        return (FrameLayout) inflater.inflate(R.layout.item_bot_message, null);
    }
    /*Handler for broadcasted actions under the name "logout"
    If the user logs out in the profile activity, we want to also close out this activity such that upon
    being redirected to the login activity, users can not go back to the chat page*/
    public BroadcastReceiver logoutReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };

    /*
    public void onCreateAudio(){
        private AIDataService aiDataService;
        private AIServiceContext customAIServiceContext;
        private AIRequest aiRequest;
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
            if (permission != PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Permission to record denied");
                makeRequest();
            }
        final AIConfiguration config = new AIConfiguration("AIzaSyAK8h56gc1CvpvfgEaaRC5tr6YnPyU6UJg",
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);
        aiDataService = new AIDataService(this, config);
        customAIServiceContext = AIServiceContextBuilder.buildFromSessionId(uuid);
        aiRequest = new AIRequest();
        aiService = AIService.getService(this, config);
        aiService.setListener(this);
        }

    public void callbackV1(AIResponse aiResponse) {
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