package com.example.mcomm.chat;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.mcomm.MainActivity;
import com.example.mcomm.R;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import static com.example.mcomm.notifications.NotificationManager.CHANNEL_ID;

public class ChatService extends Service {
    //TODO: write method for clients thread pool
    //TODO: retrieve messages from queue and send them to app
    private final String TAG = "CommunicationService";
    private boolean isHost;
    private Socket mSocket;
    private List<ClientConnectionHandler> clientsList;
    public static Queue<String> messageQueue;
    private boolean sendMessages;

    @Override
    public void onCreate() {
        super.onCreate();
        startInForeground(); //start service in foreground
        messageQueue = new ConcurrentLinkedQueue<>();
        sendMessages = true;

    }

    private void startInForeground() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("MComm")
                .setContentText("Service is started")
                .setSmallIcon(R.drawable.ic_android)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        if (intent.getAction() == "createService") {
            Thread initializeSocketsThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    initializeSocket(intent);
                }
            });
            initializeSocketsThread.start();
            Thread sendMessagesToAppThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    sendMessagesToApp();
                }
            });
            sendMessagesToAppThread.start();
        } else {
            String messageToSend = intent.getExtras().getString("message");
            write(messageToSend.getBytes());
        }


        return START_REDELIVER_INTENT;

    }

    @Override
    public void onDestroy() {
        if (mSocket != null) {
            try {
                mSocket.getInputStream().close();
                mSocket.getOutputStream().close();
                mSocket.close();
                mSocket = null;

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
        {
            for(int i=0;i<clientsList.size();++i)
            {
                clientsList.get(i).closeConnection();
            }
            sendMessages = false;
            sendRemainingMessagesToApp();
            messageQueue = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void initializeSocket(final Intent intent) {
        isHost = intent.getExtras().getBoolean("isHost");

        if (isHost) {
            try {
                clientsList = new ArrayList<>();
                ServerSocket serverSocket = new ServerSocket(9000);
                while(true) {
                    Thread.sleep(5000);
                    Socket clientSocket = serverSocket.accept();
                    ClientConnectionHandler clientConnectionHandler = new ClientConnectionHandler(clientSocket);
                    clientsList.add(clientConnectionHandler);
                    clientConnectionHandler.start();
                }
            } catch (IOException e) {
                Log.d(TAG, e.getMessage());
            }
            catch (InterruptedException e)
            {
                Log.d(TAG, e.getMessage());

            }

        } else {
            String hostAddress = intent.getExtras().getString("hostAddress");
            try {
                mSocket = new Socket();
                mSocket.connect(new InetSocketAddress(hostAddress, 9000), 500);
                ClientConnectionHandler clientConnectionHandler = new ClientConnectionHandler(mSocket);
                clientConnectionHandler.start();
            } catch (IOException e) {
                Log.d(TAG, e.getMessage());
            }
        }

    }

    private void sendMessagesToApp()
    {
        while(sendMessages)
        {
            if (!messageQueue.isEmpty())
            {
                String messageToSend = messageQueue.poll();
                sendMessage(messageToSend);
            }
        }

    }

    private void sendRemainingMessagesToApp()
    {
        while(!messageQueue.isEmpty())
        {
            String messageToSend = messageQueue.poll();
            sendMessage(messageToSend);
        }
    }

    private void sendMessage(String messageToSend)
    {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.setAction("messageReceiver");
        intent.putExtra("message", messageToSend);
        //send the received message to chat activity
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }


    private void write(byte[] bytes) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);
        OutputStream outputStream;
        try {

            if (clientsList != null) {

                for (int i = 0; i < clientsList.size(); ++i) {
                    outputStream = clientsList.get(i).getSocket().getOutputStream();
                    outputStream.write(bytes);
                }
            }
            else
            {
                outputStream = mSocket.getOutputStream();
                outputStream.write(bytes);
            }
        } catch (IOException e) {
            Log.d(TAG, e.toString());
        }
    }
}