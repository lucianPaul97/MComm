package com.example.mcomm.available_devices;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mcomm.GroupFragment;
import com.example.mcomm.HomeScreenFragment;
import com.example.mcomm.MainActivity;
import com.example.mcomm.MyWifiP2PManager;
import com.example.mcomm.R;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AvailableDevicesFragment extends Fragment implements IAvailableDevicesListener {

    private TextView message;
    private View loadingScreen;
    private ProgressBar progressBar;
    private TextView availableDevicesText;
    private MyWifiP2PManager p2PManager;
    private DeviceListAdapter deviceListAdapter;
    private List<WifiP2pDevice> peers;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.available_devices_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);  //setting toolbar with back button
        MainActivity.toolbar.setNavigationOnClickListener(navigationClickListener);
        message = view.findViewById(R.id.loadingMessage);
        message.setText(getArguments().getString("messageToDisplay"));
        loadingScreen = view.findViewById(R.id.deviceDiscoveryLoading);
        progressBar = view.findViewById(R.id.loadingBar);
        availableDevicesText = view.findViewById(R.id.availableDevices);
        availableDevicesText.setVisibility(View.INVISIBLE);
        RecyclerView deviceList = view.findViewById(R.id.deviceList);
        deviceList.setLayoutManager(new LinearLayoutManager(getActivity()));
        deviceListAdapter = new DeviceListAdapter(new ArrayList<String>());
        deviceListAdapter.setOnItemClickListener(itemClickListener);
        deviceList.setAdapter(deviceListAdapter);
        p2PManager = new MyWifiP2PManager(getActivity(), getContext());
        p2PManager.setAvailableDevicesListener(this);
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
        while (true) {
            if (wifiManager.isWifiEnabled())
                break;
        }
    }

    View.OnClickListener navigationClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            HomeScreenFragment homeScreenFragment = new HomeScreenFragment();
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameContainer, homeScreenFragment).commit();
        }
    };

    @Override
    public void onPeersDiscoveryStarted(boolean successfullyStarted) {
        if (!successfullyStarted) {
            progressBar.setVisibility(View.INVISIBLE);
            message.setText("Couldn't find any device");
        }
    }

    @Override
    public void onAvailableDevicesChanged(Collection<WifiP2pDevice> peers) {
        this.peers = new ArrayList<>(peers);
        loadingScreen.setVisibility(View.INVISIBLE);
        availableDevicesText.setVisibility(View.VISIBLE);
        List<String> deviceNameList = new ArrayList<>();
        for (WifiP2pDevice device : peers) {
            deviceNameList.add(device.deviceName);
        }
        deviceListAdapter.updateList(deviceNameList);
    }

    ItemClickListener itemClickListener = new ItemClickListener() {
        @Override
        public void onItemClick(int itemPosition) {
            final WifiP2pDevice device = peers.get(itemPosition);
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
    };

    @Override
    public void onConnectionInfoAvailable(final WifiP2pInfo wifiP2pInfo) {
        //      start group fragment
        if (!MainActivity.isJoinedToGroup) {
            MainActivity.isJoinedToGroup = true;
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    final InetAddress groupOwnerAddress = wifiP2pInfo.groupOwnerAddress;
                    String groupOwner;
                    boolean isHost = false;
                    if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {
                        //this device is the host
                        groupOwner = "You";
                        isHost = true;

                    } else {
                        groupOwner = p2PManager.getGroupName();
                    }
                    GroupFragment groupFragment = new GroupFragment();
                    Bundle fragmentParameters = new Bundle();
                    fragmentParameters.putString("groupName", groupOwner);
                    fragmentParameters.putBoolean("isHost", isHost);
                    fragmentParameters.putString("hostAddress", groupOwnerAddress.getHostAddress());
                    groupFragment.setArguments(fragmentParameters);
                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameContainer, groupFragment).commit();
                }
            });
            thread.start();
        }

    }

    @Override
    public void onClientsListChanged(List<String> clients) {
        //not implementing here
    }
}
