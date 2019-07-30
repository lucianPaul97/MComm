package com.example.mcomm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.mcomm.available_devices.DeviceListAdapter;
import com.example.mcomm.available_devices.IAvailableDevicesListener;
import com.example.mcomm.service.CommunicationService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GroupFragment extends Fragment implements IAvailableDevicesListener {


    private DeviceListAdapter deviceListAdapter;
    private MyWifiP2PManager p2PManager;
    private List<String> clientsList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.custom_screen_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        view.findViewById(R.id.discoverUsersButton).setVisibility(View.INVISIBLE);
        view.findViewById(R.id.goToGroupButton).setVisibility(View.INVISIBLE);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        MainActivity.toolbar.setNavigationOnClickListener(navigationClickListener);
        if (MainActivity.isJoinedToGroup) {
            View loadingScreen = view.findViewById(R.id.customScreenLoading);
            loadingScreen.setVisibility(View.INVISIBLE);
            TextView nameLabel = view.findViewById(R.id.nameLabel);
            nameLabel.setText("Group owner");
            TextView contactsLabel = view.findViewById(R.id.contactsLabel);
            contactsLabel.setText("Group members");
            TextView name = view.findViewById(R.id.textName);
            name.setText(getArguments().getString("groupName"));
            Button changeName = view.findViewById(R.id.changeNameButton);
            changeName.setVisibility(View.INVISIBLE);
            clientsList = new ArrayList<>();
            RecyclerView clientsList = view.findViewById(R.id.contactsList);
            clientsList.setLayoutManager(new LinearLayoutManager(getActivity()));
            deviceListAdapter = new DeviceListAdapter(new ArrayList<String>());
            clientsList.setAdapter(deviceListAdapter);
            p2PManager = new MyWifiP2PManager(getActivity(), getContext());
            p2PManager.setAvailableDevicesListener(this);
            //start the service
            startCommunicationService(getArguments().getBoolean("isHost"), getArguments().getString("hostAddress"));
        } else {
            view.findViewById(R.id.loadingBar).setVisibility(View.INVISIBLE);
            TextView loadingMessage = view.findViewById(R.id.loadingMessage);
            loadingMessage.setText("Not part of any group");
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        if (p2PManager != null) {
            p2PManager.unregisterWifiReceiver();
        }
        if (clientsReceiver != null)
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(clientsReceiver);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (p2PManager != null) {
            p2PManager.registerWifiReceiver();
        }
        if (clientsReceiver != null) {
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(clientsReceiver, new IntentFilter("clientReceiver"));
        }
    }


    @Override
    public void onPeersDiscoveryStarted(boolean successfullyStarted) {
        // not implementing here
    }

    @Override
    public void onAvailableDevicesChanged(Collection<WifiP2pDevice> peers) {
        // not implementing here

    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
        // not implementing here

    }

    @Override
    public void onClientsListChanged(List<String> clients) {
        deviceListAdapter.updateList(clients);
        broadcastUsers(clients);
    }

    View.OnClickListener navigationClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            HomeScreenFragment homeScreenFragment = new HomeScreenFragment();
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameContainer, homeScreenFragment).commit();
        }
    };

    private void startCommunicationService(boolean isHost, @Nullable String hostAddress) {
        Intent intent = new Intent(getActivity(), CommunicationService.class);
        intent.setAction("createService");
        intent.putExtra("isHost", isHost);
        intent.putExtra("hostAddress", hostAddress);
        getActivity().startService(intent);
    }

    private void broadcastUsers(List<String> clients) {
        Intent intent = new Intent(getActivity(), CommunicationService.class);
        intent.setAction("broadcastUsers");
        clients.add(MainActivity.deviceName); //add leader username to clients list
        intent.putStringArrayListExtra("clients", new ArrayList<>(clients));
        getActivity().startService(intent);
    }

    private BroadcastReceiver clientsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String receivedClient = intent.getStringExtra("client");
            if (receivedClient != null) {
                if (receivedClient.equals("clear")) {
                    clientsList.clear();
                } else {
                    if (!receivedClient.equals(MainActivity.deviceName)) {
                        clientsList.add(receivedClient);
                    }
                }
                deviceListAdapter.updateList(clientsList);
            }
        }
    };

//    ItemClickListener itemClickListener = new ItemClickListener() {
//        @Override
//        public void onItemClick(int itemPosition) {
//            final WifiP2pDevice device = peers.get(itemPosition);
//            WifiP2pConfig config = new WifiP2pConfig();
//            config.deviceAddress = device.deviceAddress;
//            p2PManager.getWiFiP2PManager().connect(p2PManager.getWiFiP2PChannel(), config, new WifiP2pManager.ActionListener() {
//                @Override
//                public void onSuccess() {
//                    Toast.makeText(getActivity(), "Connecting to " + device.deviceName, Toast.LENGTH_SHORT).show();
//                }
//
//                @Override
//                public void onFailure(int i) {
//                    Toast.makeText(getActivity(), "Failed to connect with " + device.deviceName, Toast.LENGTH_SHORT).show();
//                }
//            });
//        }
//    };
}


