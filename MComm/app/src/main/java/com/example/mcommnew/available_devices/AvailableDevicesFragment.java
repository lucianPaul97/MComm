package com.example.mcommnew.available_devices;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mcommnew.MyWifiP2PManager;
import com.example.mcommnew.R;
import com.example.mcommnew.chat.ChatActivity;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AvailableDevicesFragment extends Fragment implements IAvailableDevicesListener{

    private TextView message;
    private View loadingScreen;
    private ProgressBar progressBar;
    private TextView availableDevicesText;
    private MyWifiP2PManager p2PManager;
    private RecyclerView deviceList;
    private DeviceListAdapter deviceListAdapter;
    private List<String> deviceNameList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.available_devices_fragment, container, false);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        message = view.findViewById(R.id.loadingMessage);
        message.setText(getArguments().getString("messageToDisplay"));
        loadingScreen = view.findViewById(R.id.deviceDiscoveryLoading);
        progressBar = view.findViewById(R.id.loadingBar);
        availableDevicesText = view.findViewById(R.id.availableDevices);
        availableDevicesText.setVisibility(View.INVISIBLE);
        deviceList = view.findViewById(R.id.deviceList);
        deviceList.setLayoutManager(new LinearLayoutManager(getActivity()));
        deviceNameList = new ArrayList<>();
        deviceListAdapter = new DeviceListAdapter(deviceNameList);
        deviceList.setAdapter(deviceListAdapter);
        p2PManager = new MyWifiP2PManager(getActivity(), getContext(), this);
        checkForAvailableDevices();

    }

    @Override
    public void onResume() {
        super.onResume();
        p2PManager.registerWifiReceiver();
    }

    @Override
    public void onPause() {
        super.onPause();
        p2PManager.unregisterWifiReceiver();
    }

    private void checkForAvailableDevices() {
        waitUntilWiFiIsEnabled();
        p2PManager.discoverPeers();
    }

    private void waitUntilWiFiIsEnabled() {
        WifiManager wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
        while (true)
        {
            if(wifiManager.isWifiEnabled())
                break;
        }
    }

    @Override
    public void onPeersDiscoveryStarted(boolean successfullyStarted) {
        if (!successfullyStarted)
        {
            progressBar.setVisibility(View.INVISIBLE);
            message.setText("Couldn't find any device");
        }
    }

    @Override
    public void onAvailableDevicesChanged(Collection<WifiP2pDevice> peers) {
        final List<WifiP2pDevice> peersList = new ArrayList<>(peers);
        loadingScreen.setVisibility(View.INVISIBLE);
        availableDevicesText.setVisibility(View.VISIBLE);
        deviceNameList.clear();
        for (WifiP2pDevice device : peersList) {
            deviceNameList.add(device.deviceName);
        }
        deviceListAdapter = new DeviceListAdapter(deviceNameList);
        deviceList.setAdapter(deviceListAdapter);
        deviceListAdapter.setOnItemClickListener(new ItemClickListener() {
            @Override
            public void onItemClick(int itemPosition) {
                final WifiP2pDevice device = peersList.get(itemPosition);
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;

                p2PManager.getWiFiP2PManager().connect(p2PManager.getWiFiP2PChannel(), config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getActivity(), "Connecting to " + device.deviceName, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int i) {
                        Toast.makeText(getActivity(), "Failed to connect with " + device.deviceName, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
        final InetAddress groupOwnerAddress = wifiP2pInfo.groupOwnerAddress;
        boolean isHost;
        if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {
            isHost = true;

        } else {
            isHost = false;
        }
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra("contactName", "Contact name");
        intent.putExtra("isHost", isHost);
        intent.putExtra("hostAddress", groupOwnerAddress.getHostAddress());
        startActivity(intent);
    }
}
