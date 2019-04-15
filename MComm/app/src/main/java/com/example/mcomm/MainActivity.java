package com.example.mcomm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button createMeshButton, discoverButton;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;
    RecyclerView devices_list;
    DevicesListAdapter mAdapter;

    List<WifiP2pDevice> peers = new ArrayList<>();
    List<String> deviceNameArray = new ArrayList<>();
    WifiP2pDevice[] devicesArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initWifiReceiver();
        createMeshButton = findViewById(R.id.createMeshButton);
        createMeshButton.setOnClickListener(onClickListener);
        discoverButton = findViewById(R.id.discoverButton);
        discoverButton.setOnClickListener(onClickListener);
        devices_list = findViewById(R.id.devices_list);
        devices_list.setLayoutManager(new LinearLayoutManager(this));

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
        mChannel = mManager.initialize(this, getMainLooper(), null);
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
                case R.id.createMeshButton:
                    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    if (wifiManager.isWifiEnabled()) {
                        Toast.makeText(MainActivity.this, "WiFi is on", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "WiFi is off", Toast.LENGTH_SHORT).show();
                    }
                    break;
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

            }
        }
    };


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
                            }

                            @Override
                            public void onFailure(int i) {
                                Toast.makeText(MainActivity.this, "Failed to connect with"+ device.deviceName, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

            }
            Log.d("n", Integer.toString(peers.size()));
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
            if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner)
            {
                Toast.makeText(MainActivity.this, "Host", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(MainActivity.this, "Client", Toast.LENGTH_SHORT).show();
            }
        }
    };
}
