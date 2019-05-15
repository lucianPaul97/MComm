package com.example.mcomm.mcomm.communication;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Thread {

    private String TAG = "SocketClass";
    private Socket mSocket;
    private ServerSocket mServerSocket;
    public SendReceive sendReceive;
    private Handler mHandler;


    public Server (Handler handler)
    {
        mHandler = handler;
    }

    @Override
    public void run() {
        try
        {
            mServerSocket = new ServerSocket(9000);
            mSocket=mServerSocket.accept();
            sendReceive = new SendReceive(mSocket, mHandler);
            sendReceive.start();

        }catch (IOException e)
        {
            Log.d(TAG, e.toString());
        }
    }
}
