package com.example.mcomm.mcomm.communication;

import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;

import com.example.mcomm.MainActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class SendReceive extends Thread {

    private String TAG = "SendReceive Class";
    private Socket mSocket;
    private InputStream mInputStream;
    private OutputStream mOutputStream;
    private Handler mHandler;

    public SendReceive(Socket socket, Handler handler)
    {
        mSocket = socket;
        mHandler = handler;
        try
        {
            mInputStream = mSocket.getInputStream();
            mOutputStream = mSocket.getOutputStream();
        }catch (IOException e)
        {
            Log.d(TAG, e.toString());
        }
    }

    @Override
    public void run() {
        int bytes;
        byte[] buffer = new byte[1024];

        while (mSocket != null)
        {
            try
            {
                bytes = mInputStream.read(buffer);
                if (bytes >0)
                {
                    mHandler.obtainMessage(MainActivity.MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                }
            }catch (IOException e)
            {
                Log.d(TAG, e.toString());
            }
        }
    }

    public void write(byte [] bytes)
    {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            mOutputStream.write(bytes);
        } catch (IOException e) {
            Log.d(TAG, e.toString());
        }
    }
}
