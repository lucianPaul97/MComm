package com.example.mcomm.mcomm.communication;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Client extends CommunicationParticipant implements Runnable {

    private String TAG = "ClientClass";
    private Socket mSocket;
    private String mHostAddress;
    private Handler mHandler;

    public Client(String hostAddress, Handler handler)
    {
        super(null, handler);
        mHostAddress = hostAddress;
        mSocket = new Socket();
        mHandler = handler;
    }

    @Override
    public void run() {
        try
        {
            mSocket.connect(new InetSocketAddress(mHostAddress, 9000), 500);
            this.setSocket(mSocket);
            this.beginCommunication();

        }catch (IOException e)
        {
            Log.d(TAG, e.toString());
        }
    }
}

