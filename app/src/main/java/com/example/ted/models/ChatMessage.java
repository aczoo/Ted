package com.example.ted.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChatMessage {
    //Message
    private String msgText;
    //Server timestamp, used to easily sort through the user's messages in chronological order
    private Object timestamp;
    //Determines if message is from the user or Ted, 101= user 102=bot
    private int bot;
    //Value established if the message was the start of a session
    //Used to determine when to display the time of each conversation
    private String sessionStart;

    //Constructor for a chat message
    public ChatMessage(String msgText,Object timestamp, int bot, Date sessionStart){
        this.msgText = msgText;
        this.timestamp = timestamp;
        this.bot = bot;
        this.sessionStart = convert(sessionStart);

    }
    //Default constructor, getters, and setters so class is parcelable
    public ChatMessage(){
    }
    public String getMsgText() { return msgText; }
    public void setMsgText(String msgText) { this.msgText = msgText; }
    public Object getTimestamp() { return timestamp; }
    public void setTimestamp(Object timestamp) {this.timestamp = timestamp; }
    public int getBot(){return bot;}
    public void setBot(int bot){this.bot = bot;}
    public String getSessionStart() {return sessionStart; }
    public void setSessionStart(Date sessionStart) {this.sessionStart = convert(sessionStart); }
    //Convert the Date object into a string
    //Seemingly unnecessary, but could not pass in a date into firebase
    public String convert(Date sessionStart){
        if (sessionStart==null)
        {return null;}
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a d MMM", Locale.ENGLISH);
        return sdf.format(sessionStart);
    }

}
