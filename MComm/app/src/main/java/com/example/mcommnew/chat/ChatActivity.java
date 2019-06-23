package com.example.mcommnew.chat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.mcommnew.R;
import com.example.mcommnew.chat.messages.Message;
import com.example.mcommnew.chat.messages.MessageListAdapter;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView mMessageRecycler;
    private MessageListAdapter messageListAdapter;
    private List<Message> messageList;
    private Button sendButton;
    private EditText writeMessage;
    public static final int MESSAGE_READ = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        String contactName = getIntent().getExtras().getString("contactName");
        boolean isHost = getIntent().getExtras().getBoolean("isHost");
        String hostAddress = getIntent().getExtras().getString("hostAddress");
        startCommunicationService(isHost, hostAddress);
        initViews(contactName);


    }

    @Override
    protected void onStop() {
        super.onStop();
        if (messageReceiver != null)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReceiver);
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, new IntentFilter("messageReceiver"));
    }

    private void initViews(String contactName) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(contactName);
        setSupportActionBar(toolbar);

        writeMessage = findViewById(R.id.typeMessage);
        sendButton = findViewById(R.id.sendButton);
        sendButton.setOnClickListener(clickListener);
        messageList = new ArrayList<>();
        mMessageRecycler = findViewById(R.id.messageHistory);
        messageListAdapter = new MessageListAdapter(this, messageList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        mMessageRecycler.setLayoutManager(linearLayoutManager);
        mMessageRecycler.setAdapter(messageListAdapter);
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if (view.getId() == R.id.sendButton) {
                if (!writeMessage.getText().toString().equals("")) {
                    Date date = new Date();
                    String createdAt;
                    if (DateUtils.isToday(date.getTime())) {
                        createdAt = DateFormat.getTimeInstance().format(date);
                    } else {
                        createdAt = DateFormat.getDateTimeInstance().format(date);
                    }
                    String messageToSend = writeMessage.getText().toString();
                    //send message via CommunicationService
                    Intent intent = new Intent(getApplicationContext(), ChatService.class);
                    intent.putExtra("message", messageToSend);
                    startService(intent);

                    //print message on the screen
                    messageList.add(0, new Message(messageToSend, 1, createdAt));
                    messageListAdapter = new MessageListAdapter(getApplicationContext(), messageList);
                    mMessageRecycler.setAdapter(messageListAdapter);
                    writeMessage.setText("");
                    mMessageRecycler.scrollToPosition(0);
                }
            }
        }
    };

    private BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String receivedMessage = intent.getStringExtra("message");
            Date date = new Date();
            String createdAt;
            if (DateUtils.isToday(date.getTime())) {
                createdAt = DateFormat.getTimeInstance().format(date);
            } else {
                createdAt = DateFormat.getDateTimeInstance().format(date);
            }
            messageList.add(0, new Message(receivedMessage, 2, createdAt));
            messageListAdapter = new MessageListAdapter(getApplicationContext(), messageList);
            mMessageRecycler.setAdapter(messageListAdapter);
            mMessageRecycler.scrollToPosition(0);
        }
    };

    private void startCommunicationService(boolean isHost, @Nullable String hostAddress) {
        Intent intent = new Intent(this, ChatService.class);
        intent.setAction("createService");
        intent.putExtra("isHost", isHost);
        intent.putExtra("hostAddress", hostAddress);
        startService(intent);
    }
}
