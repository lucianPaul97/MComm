package com.example.mcommnew;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;

import com.example.mcommnew.available_devices.IAvailableDevicesListener;
import com.example.mcommnew.receiver.WifiDirectBroadcastReceiver;

import java.util.ArrayList;
import java.util.List;

import static android.os.Looper.getMainLooper;

public class MyWifiP2PManager {

    private Activity mActivity;
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private BroadcastReceiver mReceiver;
    private IntentFilter mIntentFilter;
    private IAvailableDevicesListener mAvailableDevicesListener;

    public MyWifiP2PManager(Activity activity, Context context, IAvailableDevicesListener availableDevicesListener)
    {
        mActivity = activity;
        mManager = (WifiP2pManager) activity.getSystemService(Context.WIFI_P2P_SERVICE);
        if (mManager != null) {
            mChannel = mManager.initialize(context, getMainLooper(), null);
        }
        mReceiver = new WifiDirectBroadcastReceiver(mManager, mChannel, this);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        mAvailableDevicesListener = availableDevicesListener;
    }

    public void registerWifiReceiver()
    {
        mActivity.registerReceiver(mReceiver, mIntentFilter);
    }

    public void unregisterWifiReceiver()
    {
        mActivity.unregisterReceiver(mReceiver);
    }

    public void discoverPeers()
    {
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                mAvailableDevicesListener.onPeersDiscoveryStarted(true);
            }

            @Override
            public void onFailure(int reason) {
                mAvailableDevicesListener.onPeersDiscoveryStarted(false);
            }
        });
    }

    public WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peers) {
            mAvailableDevicesListener.onAvailableDevicesChanged(peers.getDeviceList());
        }
    };

    public WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener()
    {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            mAvailableDevicesListener.onConnectionInfoAvailable(wifiP2pInfo);

        }
    };

    public WifiP2pManager getWiFiP2PManager()
    {
        return mManager;
    }

    public WifiP2pManager.Channel getWiFiP2PChannel()
    {
        return mChannel;
    }
}
