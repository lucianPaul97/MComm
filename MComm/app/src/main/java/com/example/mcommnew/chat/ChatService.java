package com.example.mcommnew.chat;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.mcommnew.MainActivity;
import com.example.mcommnew.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import static com.example.mcommnew.chat.ChatActivity.MESSAGE_READ;
import static com.example.mcommnew.notifications.NotificationManager.CHANNEL_ID;

public class ChatService extends Service {
    private final String TAG = "CommunicationService";
    private boolean isHost;
    private Socket mSocket;

    @Override
    public void onCreate() {
        super.onCreate();
        startInForeground(); //start service in foreground
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
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    initializeSocket(intent);
                }
            });
            thread.start();
        } else {
            String messageToSend = intent.getExtras().getString("message");
            write(messageToSend.getBytes());
        }


        return START_REDELIVER_INTENT;

    }

    @Override
    public void onDestroy() {
        try {
            mSocket.getInputStream().close();
            mSocket.getOutputStream().close();
            mSocket.close();
            mSocket = null;
        } catch (IOException e) {
            e.printStackTrace();
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
                ServerSocket serverSocket = new ServerSocket(9000);
                Thread.sleep(5000);
                mSocket = serverSocket.accept();
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
            } catch (IOException e) {
                Log.d(TAG, e.getMessage());
            }
        }

        beginCommunication();
    }


    //Handle incoming message from other users
    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(android.os.Message message) {
            if (message.what == MESSAGE_READ) {
                byte[] readBuff = (byte[]) message.obj;
                String receivedMessage = new String(readBuff, 0, message.arg1);
                Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                intent.setAction("messageReceiver");
                intent.putExtra("message", receivedMessage);
                //send the received message to chat activity
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);


            }

            return true;
        }
    });

    private void beginCommunication() {

        try {
            InputStream mInputStream = mSocket.getInputStream();
            int bytes;
            byte[] buffer = new byte[1024];

            if (mInputStream != null) {
                while (mSocket != null) {
                    bytes = mInputStream.read(buffer);
                    if (bytes > 0) {
                        handler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                    }
                }
            }
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
        }
    }


    private void write(byte[] bytes) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            OutputStream outputStream = mSocket.getOutputStream();
            outputStream.write(bytes);
        } catch (IOException e) {
            Log.d(TAG, e.toString());
        }
    }
}
