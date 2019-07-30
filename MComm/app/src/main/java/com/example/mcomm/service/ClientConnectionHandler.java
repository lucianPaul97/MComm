package com.example.mcomm.service;


import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class ClientConnectionHandler extends Thread {

    private Socket mSocket;
    private static String TAG = "ClientConnectionHandler";

    public ClientConnectionHandler(Socket socket) {
        mSocket = socket;
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
                        if(receivedMessage.equals("clear"))
                        {
                            putClientInQueue(receivedMessage);
                        }
                        else if (receivedMessage.contains("client:") || receivedMessage.contains("ient:")) //second check is performed because malformed messages can be received
                        {
                            putClientInQueue(receivedMessage.substring(7));
                        }
                        else {
                            putMessageInQueue(receivedMessage);
                        }
                        Log.d("Received: ", new String(buffer).substring(0, bytes));
                    }
                }
            }
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
        }
    }


    private synchronized void putMessageInQueue(String message) {
        CommunicationService.messageQueue.add(message);
    }

    private synchronized void putClientInQueue(String client)
    {
        CommunicationService.clientsQueue.add(client);
    }

    public void closeConnection() {
        try {
            mSocket.shutdownInput();
            mSocket.close();
            mSocket = null;
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
        }
    }

    public Socket getSocket() {
        return mSocket;
    }
}
