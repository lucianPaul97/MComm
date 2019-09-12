package com.example.mcomm.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import com.example.mcomm.MyWifiP2PManager;

public class WifiDirectBroadcastReceiver extends BroadcastReceiver {
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private MyWifiP2PManager mWifiP2PManager;
    private final String TAG = "WifiDirectBroadcastReceiver";

    public WifiDirectBroadcastReceiver(WifiP2pManager mManager, WifiP2pManager.Channel mChannel, MyWifiP2PManager wifiP2PManager) {
        this.mManager = mManager;
        this.mChannel = mChannel;
        this.mWifiP2PManager = wifiP2PManager;
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int wifiState = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (wifiState == WifiP2pManager.WIFI_P2P_STATE_DISABLED) //if wifi is disabled
            {
                //delete all users from database, because you're disconnected from WiFi P2P Direct
                mWifiP2PManager.deleteClients();
                mWifiP2PManager.wifiIsTurnedOff();
            }

        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            if (mManager != null) {
                mManager.requestPeers(mChannel, mWifiP2PManager.peerListListener);
            }

        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            if (mManager == null) {
                return;
            }
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if (networkInfo.isConnected()) {
                mManager.requestConnectionInfo(mChannel, mWifiP2PManager.connectionInfoListener);
            }
            WifiP2pGroup wifiP2pGroup = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_GROUP);
            try {
//                mManager.requestGroupInfo(mChannel, mWifiP2PManager.groupInfoListener);
                mWifiP2PManager.setGroupName(wifiP2pGroup.getOwner().deviceName);
                if (!wifiP2pGroup.getClientList().isEmpty()) {
                    mWifiP2PManager.setClientsList(wifiP2pGroup.getClientList());
                }
            } catch (NullPointerException e) {
                Log.d(TAG, e.getMessage());
            }


        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            WifiP2pDevice device = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
            mWifiP2PManager.setDeviceName(device.deviceName);

        }
    }
}
