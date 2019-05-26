package com.example.mcomm.messages;

public class Message {

    public String message;
    public int  messageType;  // type of message: sent(value=1) / received(value=2)
    public long createdAt;

    public Message(String message, int messageType, long createdAt)
    {
        this.message = message;
        this.messageType = messageType;
        this.createdAt = createdAt;
    }
}
