package com.example.mcommnew.chat;


import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class ClientConnectionHandler extends Thread {

    private Socket mSocket;
    private static String TAG = "ClientConnectionHandler";

    public ClientConnectionHandler(Socket socket)
    {
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
                        putMessageInQueue(new String(buffer));
                    }
                }
            }
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
        }
    }


    private synchronized void putMessageInQueue(String message)
    {
        ChatService.messageQueue.add(message);
        Log.d("Added:", message);
    }

    public void closeConnection()
    {
        try {
            mSocket.shutdownInput();
            mSocket.close();
            mSocket = null;
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
        }
    }

    public Socket getSocket()
    {
        return mSocket;
    }
}
