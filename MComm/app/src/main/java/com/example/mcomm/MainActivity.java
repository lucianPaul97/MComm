package com.example.mcomm;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mcomm.mcomm.communication.ChatActivity;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button createMeshButton, discoverButton;
    private Button sendButton;
    private EditText inputText;
    private TextView receiveMessage;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;
    RecyclerView devices_list;
    DevicesListAdapter mAdapter;
//    Server communicateAsServer;
//    Client communicateAsClient;

    List<WifiP2pDevice> peers = new ArrayList<>();
    List<String> deviceNameArray = new ArrayList<>();
    WifiP2pDevice[] devicesArray;
    public static final int MESSAGE_READ=1;
    private int ACCESS_COARSE_LOCATION_PERMISSION = 1;
    public String contactName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initWifiReceiver();
        receiveMessage = findViewById(R.id.receiveMessage);
        inputText = findViewById(R.id.inputText);
        createMeshButton = findViewById(R.id.createMeshButton);
        createMeshButton.setOnClickListener(onClickListener);
        discoverButton = findViewById(R.id.discoverButton);
        discoverButton.setOnClickListener(onClickListener);
        sendButton = findViewById(R.id.sendButton);
        sendButton.setOnClickListener(onClickListener);
        devices_list = findViewById(R.id.devices_list);
        devices_list.setLayoutManager(new LinearLayoutManager(this));

        checkPermissions();

    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[] {Manifest.permission.ACCESS_COARSE_LOCATION},
                        ACCESS_COARSE_LOCATION_PERMISSION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
        }
        
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    private void initWifiReceiver() {
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        if (mManager != null) {
            mChannel = mManager.initialize(this, getMainLooper(), null);
        }
        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Button eventButton = (Button) v;
            switch (eventButton.getId()) {
                case R.id.discoverButton:
                    mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(MainActivity.this, "Discovery started", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(int reason) {
                            Toast.makeText(MainActivity.this, "Discovery failed", Toast.LENGTH_SHORT).show();

                        }
                    });
                    break;
                case R.id.sendButton:
                    String message = inputText.getText().toString();
//                    if (communicateAsServer != null)
//                        communicateAsServer.write(message.getBytes());
//                    else
//                        communicateAsClient.write(message.getBytes());
//                   break;



            }
        }
    };
//
//    Handler handler = new Handler(new Handler.Callback() {
//        @Override
//        public boolean handleMessage(Message message) {
//            switch (message.what)
//            {
//                case MESSAGE_READ:
//                    byte [] readBuff = (byte[]) message.obj;
//                    String tempMessage = new String(readBuff, 0, message.arg1);
//                    receiveMessage.setText(tempMessage);
//                    break;
//            }
//            return true;
//        }
//    });




    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peersList) {
            if (!peersList.getDeviceList().equals(peers))
            {
                peers.clear();
                peers.addAll(peersList.getDeviceList());

                deviceNameArray.clear();
                devicesArray = new WifiP2pDevice[peersList.getDeviceList().size()];
                int index=0;

                for (WifiP2pDevice device: peersList.getDeviceList())
                {
                    deviceNameArray.add(device.deviceName);
                    devicesArray[index] = device;
                    index++;
                }

                mAdapter = new DevicesListAdapter(deviceNameArray);
                devices_list.setAdapter(mAdapter);
                mAdapter.setOnItemClickListener(new ItemClickListener() {
                    @Override
                    public void onItemClick(int itemPosition) {
                        final WifiP2pDevice device = peers.get(itemPosition);
                        WifiP2pConfig config = new WifiP2pConfig();
                        config.deviceAddress =  device.deviceAddress;

                        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(MainActivity.this, "Connected to"+ device.deviceName, Toast.LENGTH_SHORT).show();
                                contactName = device.deviceName;
                            }

                            @Override
                            public void onFailure(int i) {
                                Toast.makeText(MainActivity.this, "Failed to connect with"+ device.deviceName, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

            }
            if (peers.size()==0)
            {
                Toast.makeText(MainActivity.this, "No device found", Toast.LENGTH_SHORT).show();
            }
        }
    };

    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            final InetAddress groupOwnerAddress = wifiP2pInfo.groupOwnerAddress;
            //Thread communicationThread;
            boolean isHost;
            if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner)
            {
//                Toast.makeText(MainActivity.this, "Host", Toast.LENGTH_SHORT).show();
//                communicateAsServer = new Server(handler);
//                communicationThread  = new Thread(communicateAsServer);
                isHost = true;

            }
            else {
//                Toast.makeText(MainActivity.this, "Client", Toast.LENGTH_SHORT).show();
//                communicateAsClient = new Client(groupOwnerAddress.toString(), handler);
//                communicationThread = new Thread(communicateAsClient);
                isHost = false;
            }
//            communicationThread.start();
            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
            intent.putExtra("contactName", contactName);
            intent.putExtra("isHost", isHost);
            intent.putExtra("hostAddress", groupOwnerAddress.getHostAddress());
            startActivity(intent);

        }
    };

}
