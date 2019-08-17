package com.example.mcomm.group.chat;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.example.mcomm.MainActivity;
import com.example.mcomm.R;
import com.example.mcomm.database.MCommDatabaseHelper;
import com.example.mcomm.database.MessagesTableListener;
import com.example.mcomm.group.chat.messages.Message;
import com.example.mcomm.group.chat.messages.MessageListAdapter;
import com.example.mcomm.group.chat.messages.MessageType;
import com.example.mcomm.service.CommunicationService;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

public class ChatActivity extends AppCompatActivity implements MessagesTableListener {

    private RecyclerView mMessageRecycler;
    private MessageListAdapter messageListAdapter;
    private EditText writeMessage;
    public static final int MESSAGE_READ = 1;
    private String contactName;
    private MCommDatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        contactName = getIntent().getExtras().getString("chatSelectedUser");
        initViews(contactName);
        databaseHelper = new MCommDatabaseHelper(this);
        databaseHelper.setMessagesTableListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        databaseHelper.setMessagesTableListener(null);
    }

    @Override
    protected void onStart() {
        super.onStart();
        messageListAdapter.clearScreen();
        loadMessages();
    }

    private void initViews(String contactName) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(contactName);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(navigationClickListener);

        writeMessage = findViewById(R.id.typeMessage);
        Button sendButton = findViewById(R.id.sendButton);
        sendButton.setOnClickListener(clickListener);
        mMessageRecycler = findViewById(R.id.messageHistory);
        messageListAdapter = new MessageListAdapter(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
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
                        createdAt = DateFormat.getTimeInstance(DateFormat.SHORT).format(date);
                    } else {
                        createdAt = DateFormat.getDateTimeInstance().format(date);
                    }
                    String messageToSend = writeMessage.getText().toString();
                    //send message via CommunicationService
                    Intent intent = new Intent(getApplicationContext(), CommunicationService.class);
                    intent.setAction("message");
                    intent.putExtra("message", messageToSend);
                    intent.putExtra("sender", MainActivity.deviceName);
                    intent.putExtra("receiver", contactName);
                    intent.putExtra("date", date.toString());
                    startService(intent);

                    //print message on the screen
                    mMessageRecycler.scrollToPosition(messageListAdapter.getItemCount());
                    messageListAdapter.insertMessage( new Message(messageToSend, MessageType.SEND, createdAt));
                    writeMessage.setText("");

                }
            }
        }
    };

    private void loadMessages()
    {
        List<Message> messages = databaseHelper.getMessages(contactName, 50);
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);
        for (Message message : messages)
        {
            Date messageDate = null;
            try {
                messageDate = dateFormat.parse(message.createdAt);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (DateUtils.isToday(messageDate.getTime())) {
                message.createdAt = DateFormat.getTimeInstance(DateFormat.SHORT).format(messageDate);
            }
            mMessageRecycler.scrollToPosition(messageListAdapter.getItemCount());
            messageListAdapter.insertMessage(message);
        }
    }

    View.OnClickListener navigationClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            });
        }
    };

    @Override
    public void onMessageReceived(final String contact, final String content, @Nullable final Date date) {
        if (contact.equals(contactName))
        {
            final String createdAt;
            if (DateUtils.isToday(date.getTime())) {
                createdAt = DateFormat.getTimeInstance(DateFormat.SHORT).format(date);
            } else {
                createdAt = DateFormat.getDateTimeInstance().format(date);
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mMessageRecycler.scrollToPosition(messageListAdapter.getItemCount());
                    messageListAdapter.insertMessage( new Message(content, MessageType.RECEIVED, createdAt));
                }
            });
        }
    }
}
