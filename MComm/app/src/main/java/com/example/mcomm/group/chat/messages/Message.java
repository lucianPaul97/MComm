package com.example.mcomm.group.chat.messages;

public class Message {

    public String message;
    public MessageType  messageType;  // type of message: sent(value=1) / received(value=2)
    public String createdAt;

    public Message(String message, MessageType messageType, String createdAt)
    {
        this.message = message;
        this.messageType = messageType;
        this.createdAt = createdAt;
    }
}
