package com.example.savemoneytime.model;

public class ChatMessage {

    public enum Sender { USER, AI, SYSTEM }

    private final String content;
    private final Sender sender;
    private final long   timestamp;

    public ChatMessage(String content, Sender sender) {
        this.content   = content;
        this.sender    = sender;
        this.timestamp = System.currentTimeMillis();
    }

    public String getContent()   { return content; }
    public Sender getSender()    { return sender; }
    public long   getTimestamp() { return timestamp; }
    public boolean isUser()      { return sender == Sender.USER; }
    public boolean isAI()        { return sender == Sender.AI; }
}