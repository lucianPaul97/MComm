package com.example.mcomm.mcomm.communication;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Client extends Thread {

    private String TAG = "ClientClass";
    private Socket mSocket;
    private String mHostAddress;
    public SendReceive sendReceive;
    private Handler mHandler;

    public Client(InetAddress hostAddress, Handler handler)
    {
        mHostAddress = hostAddress.getHostAddress();
        mSocket = new Socket();
        mHandler = handler;
    }

    @Override
    public void run() {
        try
        {
            mSocket.connect(new InetSocketAddress(mHostAddress, 9000), 500);
            sendReceive = new SendReceive(mSocket, mHandler);
            sendReceive.start();

        }catch (IOException e)
        {
            Log.d(TAG, e.toString());
        }
    }
}

