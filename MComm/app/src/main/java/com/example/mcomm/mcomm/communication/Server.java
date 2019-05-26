package com.example.mcomm.mcomm.communication;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends CommunicationParticipant implements Runnable {

    private String TAG = "SocketClass";
    private Socket mSocket;
    private ServerSocket mServerSocket;
    private Handler mHandler;


    public Server (Handler handler)
    {
        super(null, handler);
        mHandler = handler;
    }

    @Override
    public void run() {
        try
        {
            mServerSocket = new ServerSocket(9000);
            mSocket=mServerSocket.accept();
            this.setSocket(mSocket);
            this.beginCommunication();
        }catch (IOException e)
        {
            Log.d(TAG, e.toString());
        }
    }
}
