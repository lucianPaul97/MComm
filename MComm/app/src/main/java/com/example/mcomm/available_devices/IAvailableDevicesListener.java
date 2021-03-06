package com.example.mcomm.available_devices;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;

import java.util.Collection;
import java.util.List;

public interface IAvailableDevicesListener {
    void onPeersDiscoveryStarted(boolean successfullyStarted);
    void onAvailableDevicesChanged(Collection<WifiP2pDevice> peers);
    void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo);
}
