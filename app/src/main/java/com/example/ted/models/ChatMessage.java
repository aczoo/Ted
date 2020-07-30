package com.example.ted.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChatMessage {
    private String msgText;
    private Object timestamp;
    private int bot;
    private String sessionStart;


    public ChatMessage(String msgText,Object timestamp, int bot, Date sessionStart){
        this.msgText = msgText;
        this.timestamp = timestamp;
        this.bot = bot;
        this.sessionStart = convert(sessionStart);

    }

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
    public String convert(Date sessionStart){
        if (sessionStart==null)
        {return null;}
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a d MMM", Locale.ENGLISH);
        return sdf.format(sessionStart);
    }

}
