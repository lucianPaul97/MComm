package com.example.mcomm.mcomm.communication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.example.mcomm.R;
import com.example.mcomm.messages.Message;
import com.example.mcomm.messages.MessageListAdapter;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView mMessageRecycler;
    private MessageListAdapter messageListAdapter;
    private List<Message> messageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        String contactName = getIntent().getExtras().getString("contactName");
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(contactName);
        setSupportActionBar(toolbar);

        messageList = new ArrayList<>();
        initialize();
        mMessageRecycler = findViewById(R.id.messageHistory);
        messageListAdapter = new MessageListAdapter(this, messageList);
        mMessageRecycler.setLayoutManager(new LinearLayoutManager(this));
        messageListAdapter = new MessageListAdapter(this, messageList);
        mMessageRecycler.setAdapter(messageListAdapter);

//        try {
//            Thread.sleep(2000);
//            initialize();
//            messageListAdapter = new MessageListAdapter(this, messageList);
//            mMessageRecycler.setAdapter(messageListAdapter);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        try {
//            Thread.sleep(2000);
//            initialize();
//            messageListAdapter = new MessageListAdapter(this, messageList);
//            mMessageRecycler.setAdapter(messageListAdapter);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

    }

    private List<Message> initialize() {

        Message message = new Message("Ce faci coaie", 2, 11);
        messageList.add(message);
        return messageList;
    }
}
