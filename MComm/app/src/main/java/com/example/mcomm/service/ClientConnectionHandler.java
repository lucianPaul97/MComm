package com.example.mcomm.service;
import android.util.Log;

import com.example.mcomm.group.chat.messages.MessageType;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Date;

public class ClientConnectionHandler extends Thread {

    private Socket mSocket;
    private static String TAG = "ClientConnectionHandler";
    private String clientName;
    private String leaderName;
    private ServiceNotifications serviceNotifications;

    ClientConnectionHandler(Socket socket, String leaderName, ServiceNotifications notifications) {
        mSocket = socket;
        this.leaderName = leaderName;
        serviceNotifications = notifications;
    }


    @Override
    public void run() {
        try {
            InputStream mInputStream = mSocket.getInputStream();
            int bytes;
            byte[] buffer = new byte[1024];
            if (mInputStream != null) {
                while (mSocket != null) {
                    bytes = mInputStream.read(buffer);
                    if (bytes > 0) {
                        String receivedMessage = new String(buffer).substring(0, bytes);
                        performAction(receivedMessage);
                        Log.d("Received: ", new String(buffer).substring(0, bytes));
                    }
                }
            }
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    private void performAction(String receivedMessage) //performs an action depending on received message
    {
        if (receivedMessage.contains("client:") || receivedMessage.contains("ient:")) //second check is performed because malformed messages can be received
        {
            CommunicationService.dbHelper.addClient(receivedMessage.replace("client:", ""));
        }
        else if (receivedMessage.contains("add:")){
            CommunicationService.dbHelper.addClient(receivedMessage.replace("add:", ""));
        }
        else if(receivedMessage.contains("remove:"))
        {
            CommunicationService.dbHelper.deleteClient(receivedMessage.replace("remove:", ""));
        }
        else if (receivedMessage.contains("setName:"))
        {
            clientName = receivedMessage.replace("setName:", "");
        }
        else
        {
            try {
                JSONObject jsonObject = new JSONObject(receivedMessage);
                if (leaderName != null && !leaderName.equals(jsonObject.getString("receiver"))) // check is the message is for leader; otherwise route the message to the receiver
                {
                    serviceNotifications.onNewMessageToRoute(jsonObject);
                }
                else {
                    CommunicationService.dbHelper.addMessage(jsonObject.getString("sender"), jsonObject.getString("message"), MessageType.RECEIVED.ordinal(), new Date());
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
    void closeConnection() {
        try {
            mSocket.shutdownInput();
            mSocket.close();
            mSocket = null;
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
        }
    }

    Socket getSocket() {
        return mSocket;
    }

    String getClientName ()
    {
        return clientName;
    }

}
