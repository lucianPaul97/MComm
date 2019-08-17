package com.example.mcomm;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;

import com.example.mcomm.available_devices.IAvailableDevicesListener;
import com.example.mcomm.database.MCommDatabaseHelper;
import com.example.mcomm.group.IClientsListener;
import com.example.mcomm.receiver.WifiDirectBroadcastReceiver;
import com.example.mcomm.user.IUsername;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static android.os.Looper.getMainLooper;

public class MyWifiP2PManager {

    private Activity mActivity;
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private WifiDirectBroadcastReceiver mReceiver;
    private IntentFilter mIntentFilter;
    private IAvailableDevicesListener mAvailableDevicesListener;
    private IClientsListener mClientsListener;
    private IUsername iUsername;
    private String groupName;

    public MyWifiP2PManager(Activity activity, Context context) {
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
    }

    public void registerWifiReceiver() {
        mActivity.registerReceiver(mReceiver, mIntentFilter);
    }

    public void unregisterWifiReceiver() {
        mActivity.unregisterReceiver(mReceiver);
    }

    public void discoverPeers() {
        if (mAvailableDevicesListener != null) {
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
    }

    public WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peers) {
            if (mAvailableDevicesListener != null) {
                mAvailableDevicesListener.onAvailableDevicesChanged(peers.getDeviceList());
            }
        }
    };

    public WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            if (mAvailableDevicesListener != null) {
                mAvailableDevicesListener.onConnectionInfoAvailable(wifiP2pInfo);
            }
        }
    };

    public WifiP2pManager getWiFiP2PManager() {
        return mManager;
    }

    public WifiP2pManager.Channel getWiFiP2PChannel() {
        return mChannel;
    }

    public void setAvailableDevicesListener(IAvailableDevicesListener availableDevicesListener) {
        mAvailableDevicesListener = availableDevicesListener;
    }

    public void setUsernameListener(IUsername iUsernameListener) {
        iUsername = iUsernameListener;
    }

    public void setClientsListener(IClientsListener clientsListener) {
        mClientsListener = clientsListener;
    }


    public void setDeviceName(String deviceName) {
        if (iUsername != null) {
            iUsername.setUserName(deviceName);
        }
    }

    public void changeUsername(String newUsername, WifiP2pManager.ActionListener actionListener) {
        try {
            Method m = mManager.getClass().getMethod(
                    "setDeviceName",
                    new Class[]{WifiP2pManager.Channel.class, String.class,
                            WifiP2pManager.ActionListener.class});

            m.invoke(mManager, mChannel, newUsername, actionListener);
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    public void createGroup() {
        mManager.createGroup(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(mActivity.getApplicationContext(), "Group successfully created", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int i) {
                Toast.makeText(mActivity.getApplicationContext(), "Failed to create new group", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setClientsList(Collection<WifiP2pDevice> clientsList) {
        List<String> clients = new ArrayList<>();
        for (WifiP2pDevice client : clientsList) {
            clients.add(client.deviceName);
        }
        if (mClientsListener != null) {
            mClientsListener.onClientsListChanged(clients);
        }
    }

    public void deleteClients() {
        MCommDatabaseHelper dbHelper = new MCommDatabaseHelper(mActivity);
        dbHelper.deleteAllClients();
    }

}
