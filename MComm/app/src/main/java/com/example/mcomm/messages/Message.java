package com.example.mcomm.messages;
import java.util.Date;

public class Message {

    public String message;
    public int  messageType;  // type of message: sent(value=1) / received(value=2)
    public String createdAt;

    public Message(String message, int messageType, String createdAt)
    {
        this.message = message;
        this.messageType = messageType;
        this.createdAt = createdAt;
    }
}
