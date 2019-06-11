package com.example.mcomm.mcomm.communication;

import android.content.Intent;
import android.os.Handler;
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
import com.example.mcomm.R;
import com.example.mcomm.messages.Message;
import com.example.mcomm.messages.MessageListAdapter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.example.mcomm.MainActivity.MESSAGE_READ;

public class ChatActivity extends AppCompatActivity {

    private RecyclerView mMessageRecycler;
    private MessageListAdapter messageListAdapter;
    private List<Message> messageList;
    private Button sendButton;
    private EditText writeMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        String contactName = getIntent().getExtras().getString("contactName");
        boolean isHost = getIntent().getExtras().getBoolean("isHost");
        String hostAddress = getIntent().getExtras().getString("hostAddress");
        startCommunicationService(isHost, hostAddress);
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

//    @Override
//    protected void onPause() {
//        super.onPause();
//        if (server != null)
//        {
//            server.closeConnection();
//        }
//        else
//        {
//            client.closeConnection();
//        }
//    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            if (view.getId() == R.id.sendButton)
            {
                if (!writeMessage.getText().toString().equals(""))
                {
                    Date date = new Date();
                    String createdAt;
                    if (DateUtils.isToday(date.getTime()))
                    {
                        createdAt = DateFormat.getTimeInstance().format(date);
                    }
                    else
                    {
                        createdAt = DateFormat.getDateTimeInstance().format(date);
                    }
                    String messageToSend = writeMessage.getText().toString();
                    //send message via CommunicationService
                    Intent intent = new Intent(getApplicationContext(), CommunicationService.class);
                    intent.putExtra("message", messageToSend);
                    startService(intent);

                    //print message on the screen
                    messageList.add(0, new Message(messageToSend,1, createdAt));
                    messageListAdapter = new MessageListAdapter(getApplicationContext(), messageList);
                    mMessageRecycler.setAdapter(messageListAdapter);
                    writeMessage.setText("");
                }
            }
        }
    };

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(android.os.Message message) {
            switch (message.what)
            {
                case MESSAGE_READ:
                    byte [] readBuff = (byte[]) message.obj;
                    String tempMessage = new String(readBuff, 0, message.arg1);
                    Date date = new Date();
                    String createdAt;
                    if (DateUtils.isToday(date.getTime()))
                    {
                        createdAt = DateFormat.getTimeInstance().format(date);
                    }
                    else
                    {
                        createdAt = DateFormat.getDateTimeInstance().format(date);
                    }
                    messageList.add(0, new Message(tempMessage,2, createdAt));
                    messageListAdapter = new MessageListAdapter(getApplicationContext(), messageList);
                    mMessageRecycler.setAdapter(messageListAdapter);
            }
            return true;
        }
    });

    private void startCommunicationService(boolean isHost, @Nullable String hostAddress)
    {
        Intent intent = new Intent(this, CommunicationService.class);
        intent.setAction("createService");
        intent.putExtra("isHost", isHost);
        intent.putExtra("hostAddress", hostAddress);
        startService(intent);
    }

}
