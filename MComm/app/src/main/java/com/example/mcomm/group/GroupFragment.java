package com.example.mcomm.group;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mcomm.HomeScreenFragment;
import com.example.mcomm.MainActivity;
import com.example.mcomm.MyWifiP2PManager;
import com.example.mcomm.R;
import com.example.mcomm.available_devices.DeviceListAdapter;
import com.example.mcomm.available_devices.ItemClickListener;
import com.example.mcomm.database.ClientsTableListener;
import com.example.mcomm.database.MCommDatabaseHelper;
import com.example.mcomm.group.chat.ChatActivity;
import com.example.mcomm.service.CommunicationService;

import java.util.ArrayList;
import java.util.List;

public class GroupFragment extends Fragment implements IClientsListener, ClientsTableListener, ItemClickListener {


    private DeviceListAdapter deviceListAdapter;
    private MyWifiP2PManager p2PManager;
    private List<String> clientsList;
    private  MCommDatabaseHelper databaseHelper;

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
        SharedPreferences sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        if (sharedPreferences.getBoolean("isGroupFormed", false)) {
            //get shared preferences
            boolean isHost = sharedPreferences.getBoolean("isHost", false);
            String groupOwner = sharedPreferences.getString("groupOwner", "");
            String hostAddress = sharedPreferences.getString("hostAddress", "");

            View loadingScreen = view.findViewById(R.id.customScreenLoading);
            loadingScreen.setVisibility(View.INVISIBLE);
            TextView nameLabel = view.findViewById(R.id.nameLabel);
            nameLabel.setText("Group owner");
            TextView contactsLabel = view.findViewById(R.id.contactsLabel);
            contactsLabel.setText("Group members");
            TextView name = view.findViewById(R.id.textName);
            name.setText(groupOwner);
            Button changeName = view.findViewById(R.id.changeNameButton);
            changeName.setVisibility(View.INVISIBLE);
            clientsList = new ArrayList<>();
            RecyclerView clientsList = view.findViewById(R.id.contactsList);
            clientsList.setLayoutManager(new LinearLayoutManager(getActivity()));
            deviceListAdapter = new DeviceListAdapter(new ArrayList<String>());
            deviceListAdapter.setOnItemClickListener(this);
            clientsList.setAdapter(deviceListAdapter);
            p2PManager = new MyWifiP2PManager(getActivity(), getContext());
            p2PManager.setClientsListener(this);

            //open database connection
            databaseHelper = new MCommDatabaseHelper(getContext());
            if (!isHost) {
                databaseHelper.setClientsTableListener(this);
            }
            //start the service
            if (!isCommunicationServiceRunning()) {
                startCommunicationService(isHost, hostAddress);
            }else
            {
                deviceListAdapter.updateList(databaseHelper.getAllClients());
            }
        } else {
            view.findViewById(R.id.loadingBar).setVisibility(View.INVISIBLE);
            TextView loadingMessage = view.findViewById(R.id.loadingMessage);
            view.findViewById(R.id.retryImage).setVisibility(View.INVISIBLE);
            loadingMessage.setText("Not part of any group");
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        if (databaseHelper != null) {
            databaseHelper.setClientsTableListener(null);
        }
        if (p2PManager != null) {
            p2PManager.unregisterWifiReceiver();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (p2PManager != null) {
            p2PManager.registerWifiReceiver();
        }
    }

    @Override
    public void onClientsListChanged(List<String> clients) {
        clientsList.clear();
        clientsList.addAll(clients);
        deviceListAdapter.updateList(clients);
        broadcastUsers(clients);
    }

    View.OnClickListener navigationClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            HomeScreenFragment homeScreenFragment = new HomeScreenFragment();
            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.enter_from_top, R.anim.exit_to_top);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.replace(R.id.frameContainer, homeScreenFragment);
            fragmentTransaction.commit();
        }
    };

    private void startCommunicationService(boolean isHost, @Nullable String hostAddress) {
        Intent intent = new Intent(getActivity(), CommunicationService.class);
        intent.setAction("initialize");
        intent.putExtra("isHost", isHost);
        intent.putExtra("hostAddress", hostAddress);
        intent.putExtra("deviceName", MainActivity.deviceName);
        getActivity().startService(intent);
    }

    private void broadcastUsers(List<String> clients) {
        Intent intent = new Intent(getActivity(), CommunicationService.class);
        intent.setAction("broadcastUsers");
        clients.add(MainActivity.deviceName); //add leader username to clients list
        intent.putStringArrayListExtra("clients", new ArrayList<>(clients));
        getActivity().startService(intent);
    }

    @Override
    public void onClientAdded(String newClients) {
        if (!newClients.equals(MainActivity.deviceName) && !clientsList.contains(newClients)) {
            clientsList.add(newClients);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    deviceListAdapter.updateList(clientsList);
                }
            });
            Log.d("Client added", newClients);
        }
    }

    @Override
    public void onClientDeleted(String client) {
        if (clientsList.contains(client)) {

            clientsList.remove(client);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    deviceListAdapter.updateList(clientsList);
                }
            });
            Log.d("Client deleted", client);
        }
    }

    @Override
    public void onItemClick(int itemPosition) {

        String chatSelectedUser = clientsList.get(itemPosition);
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra("chatSelectedUser", chatSelectedUser);
        startActivity(intent);
    }

    private boolean isCommunicationServiceRunning()
    {
        ActivityManager manager = (ActivityManager)getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (CommunicationService.class.getName().equals(service.service.getClassName())) {
                Log.i("Service already","running");
                return true;
            }
        }
        Log.i("Service not","running");
        return false;
    }
}


