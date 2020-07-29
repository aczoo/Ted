package com.example.ted.models;

import java.util.Date;

public class ChatMessage {
    private String msgText;
    private Object timestamp;
    private Boolean bot;


    public ChatMessage(String msgText,Object timestamp, Boolean bot){
        this.msgText = msgText;
        this.timestamp = timestamp;
        this.bot = bot;

    }

    public ChatMessage(){

    }
    public String getMsgText() {
        return msgText;
    }
    public void setMsgText(String msgText) {
        this.msgText = msgText;
    }
    public Object getTimestamp() { return timestamp; }
    public void setTimestamp(Object timestamp) {this.timestamp = timestamp; }
    public Boolean getBot(){return bot;}
    public void setBot(Boolean bot){this.bot = bot;}


}
