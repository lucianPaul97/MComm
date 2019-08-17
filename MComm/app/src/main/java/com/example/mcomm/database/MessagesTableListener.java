package com.example.mcomm.database;

import android.support.annotation.Nullable;

import java.util.Date;

public interface MessagesTableListener {

    void onMessageReceived(String contact, String content, @Nullable Date date);
}
