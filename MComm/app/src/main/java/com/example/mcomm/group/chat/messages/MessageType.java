package com.example.mcomm.group.chat.messages;

public enum MessageType {
    SEND,
    RECEIVED;

    public static MessageType valueOf(int ordinal)
    {
        MessageType type = SEND;
        if (ordinal == 1)
        {
            type = RECEIVED;
        }
        return type;
    }
}
