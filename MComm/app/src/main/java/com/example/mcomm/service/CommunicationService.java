package com.example.mcomm.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.mcomm.MainActivity;
import com.example.mcomm.R;
import com.example.mcomm.database.MCommDatabaseHelper;
import com.example.mcomm.group.chat.messages.MessageType;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.example.mcomm.notifications.NotificationManager.CHANNEL_ID;

public class CommunicationService extends Service implements ServiceNotifications{
    private final String TAG = "CommunicationService";
    private Socket mSocket;
    private List<ClientConnectionHandler> clientsList;
    protected static MCommDatabaseHelper dbHelper;

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
        performAction(intent);
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
            dbHelper.close();
        } else {
            for (int i = 0; i < clientsList.size(); ++i) {
                clientsList.get(i).closeConnection();
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private void performAction(final Intent intent) {
        switch (intent.getAction()) {
            case "initialize": //called when the group is created; initializes a server or a client socket, depending if the devices is leader or not
                Thread initializeSocketsThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        initializeSocket(intent);
                    }
                });
                initializeSocketsThread.start();
                break;
            case "broadcastUsers": //only in leader case; broadcast new users
                List<String> clientsNameList = new ArrayList<>();
                clientsNameList.addAll(intent.getStringArrayListExtra("clients"));
                int clientsCount = dbHelper.getClientsCount();
                if (clientsCount <= clientsNameList.size()) { //if a client joined the group
                    for (String clientName : clientsNameList) {
                        if (!dbHelper.isClientStored(clientName)) {
                            dbHelper.addClient(clientName);
                            sendMessageToAllClients(("add:" + clientName).getBytes());
                        }
                    }
                } else { //if a client leaved the group
                    List<String> dbClients = dbHelper.getAllClients();
                    for (String dbClient : dbClients) {
                        if (!clientsNameList.contains(dbClient)) {
                            dbHelper.deleteClient(dbClient);
                            sendMessageToAllClients(("remove:" + dbClient).getBytes());
                        }
                    }
                }
                break;
            case "message": //for sending messages
                String message = intent.getExtras().getString("message");
                String receiver = intent.getExtras().getString("receiver");
                Date date = new Date(intent.getExtras().getString("date"));
                //add message to database
                dbHelper.addMessage(receiver, message, MessageType.SEND.ordinal(), date);
                JSONObject messageToSend = new JSONObject();
                //send the message to the other user
                try {
                    messageToSend.put("sender", intent.getExtras().getString("sender"));
                    messageToSend.put("receiver", receiver);
                    messageToSend.put("message", message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (clientsList != null) //leader side
                {
                    for (ClientConnectionHandler client : clientsList) {
                        if (receiver.equals(client.getClientName())) {
                            sendMessageToClient(messageToSend.toString().getBytes(), client.getSocket());
                            break;
                        }
                    }
                } else //client side
                {
                    sendMessageToClient(messageToSend.toString().getBytes(), mSocket);
                }
                break;
        }
    }

    private void initializeSocket(final Intent intent) {
        boolean isHost = intent.getExtras().getBoolean("isHost");
        dbHelper = new MCommDatabaseHelper(getApplicationContext());
        if (isHost) {
            try {
                clientsList = new ArrayList<>();
                ServerSocket serverSocket = new ServerSocket();
                serverSocket.setReuseAddress(true);
                serverSocket.bind(new InetSocketAddress(9000));
                String leaderName = intent.getExtras().getString("deviceName");
                while (true) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Socket clientSocket = serverSocket.accept();

                    ClientConnectionHandler clientConnectionHandler = new ClientConnectionHandler(clientSocket, leaderName, this);
                    clientsList.add(clientConnectionHandler);
                    clientConnectionHandler.start();

                    //send the list of clients to the new user
                    List<String> clients = dbHelper.getAllClients();
                    try {
                        for (String client : clients) {
                            Thread.sleep(300);
                            sendMessageToClient(("client:" + client).getBytes(), clientSocket);
                        }
                    } catch (InterruptedException e) {
                        Log.d(TAG, e.getMessage());
                    }
                }
            } catch (IOException e) {
                Log.d(TAG, e.getMessage());
            }

        } else {
            String hostAddress = intent.getExtras().getString("hostAddress");
            mSocket = new Socket();
            try {
                mSocket.connect(new InetSocketAddress(hostAddress, 9000), 500);

            } catch (IOException e) {
                try {
                    mSocket.connect(new InetSocketAddress(hostAddress, 9000), 500);
                } catch (IOException ex) {
                    Log.d(TAG, ex.getMessage());

                }
            }
            ClientConnectionHandler clientConnectionHandler = new ClientConnectionHandler(mSocket, null, null);
            clientConnectionHandler.start();

            //send your name to leader
            String clientName = intent.getExtras().getString("deviceName");
            sendMessageToClient(("setName:" + clientName).getBytes(), mSocket);
        }
    }

    private void sendMessageToClient(byte[] bytes, Socket socket) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            OutputStream outputStream = socket.getOutputStream();
            outputStream.flush();
            outputStream.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessageToAllClients(byte[] bytes) {
        for (ClientConnectionHandler client : clientsList) {
            sendMessageToClient(bytes, client.getSocket());
        }
    }

    @Override
    public void onNewMessageToRoute(JSONObject message) {
        try {
            String receiver = message.getString("receiver");
            for (ClientConnectionHandler client: clientsList)
            {
                if (client.getClientName().equals(receiver))
                {
                    sendMessageToClient(message.toString().getBytes(), client.getSocket());
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}