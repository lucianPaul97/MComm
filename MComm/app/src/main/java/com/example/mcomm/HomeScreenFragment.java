package com.example.mcomm;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.example.mcomm.available_devices.AvailableDevicesFragment;
import com.example.mcomm.available_devices.DeviceListAdapter;
import com.example.mcomm.available_devices.ItemClickListener;
import com.example.mcomm.database.MCommDatabaseHelper;
import com.example.mcomm.group.GroupFragment;
import com.example.mcomm.group.chat.ChatActivity;
import com.example.mcomm.service.CommunicationService;
import com.example.mcomm.user.IUsername;
import com.example.mcomm.user.CustomDialog;
import com.example.mcomm.user.CustomDialogListener;
import java.util.ArrayList;

public class HomeScreenFragment extends Fragment implements CustomDialogListener, IUsername {

    private TextView username;
    private MyWifiP2PManager p2PManager;
    private DeviceListAdapter deviceListAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.custom_screen_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false); //setting toolbar with no back button

        View loadingScreen = view.findViewById(R.id.customScreenLoading);
        loadingScreen.setVisibility(View.INVISIBLE);
        username = view.findViewById(R.id.textName);
        Button changeUsername = view.findViewById(R.id.changeNameButton);
        changeUsername.setOnClickListener(onClickListener);

        Button discoverUsername = view.findViewById(R.id.discoverUsersButton);
        discoverUsername.setOnClickListener(onClickListener);
        Button goToGroup = view.findViewById(R.id.goToGroupButton);
        goToGroup.setOnClickListener(onClickListener);
        p2PManager = new MyWifiP2PManager(getActivity(), getContext());
        p2PManager.setUsernameListener(this);

        deviceListAdapter = new DeviceListAdapter(new ArrayList<String>());
        deviceListAdapter.setOnItemClickListener(itemClickListener);
        RecyclerView recentConversationsList = view.findViewById(R.id.contactsList);
        recentConversationsList.setLayoutManager(new LinearLayoutManager(getActivity()));
        recentConversationsList.setAdapter(deviceListAdapter);
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

    @Override
    public void onStart() {
        super.onStart();
        MCommDatabaseHelper dbHelper = new MCommDatabaseHelper(getContext());
        deviceListAdapter.updateList(dbHelper.getRecentContact());
        p2PManager.requestConnectionInfo(connectionInfo);
//        shouldServiceBeStarted();
    }

    View.OnClickListener onClickListener  = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId())
            {
                case R.id.changeNameButton:
                    openChangeUsernameDialog();
                    break;
                case R.id.discoverUsersButton:
                    if (isWiFiEnabled()) {
                        if (Build.VERSION.SDK_INT >=26) { //check if devices runs Android Oreo or higher
                            if (isLocationEnabled()) {
                                loadAvailableDevicesFragment();
                            }
                            else
                            {
                                askToEnableLocation();
                            }
                        }
                        else
                        {
                            loadAvailableDevicesFragment();
                        }

                    } else {
                        askToEnableWiFi();
                    }
                    break;
                case R.id.goToGroupButton:
                    loadGroupFragment();
                    break;
            }
        }
    };

    private void openChangeUsernameDialog() {
        CustomDialog usernameDialog = new CustomDialog();
        Bundle arguments = new Bundle();
        arguments.putString("intent", "changeUsername");
        usernameDialog.setArguments(arguments);
        usernameDialog.setUsernameDialogListener(this);
        usernameDialog.show(getActivity().getSupportFragmentManager(), "change username");
    }

    @Override
    public void changeUsername(final String newUsername) {


        p2PManager.changeUsername(newUsername, actionListener);

    }

    @Override
    public void createGroup(String groupName) {
        //not implementing here
    }

    WifiP2pManager.ActionListener actionListener = new WifiP2pManager.ActionListener() { //define listener as a result code for setting username operation
        @Override
        public void onSuccess() {
            Toast.makeText(getActivity(), "Username changed successfully", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFailure(int i) {
            Toast.makeText(getActivity(), "WiFi must be enabled to perform this action", Toast.LENGTH_SHORT).show();
        }
    };

    ItemClickListener itemClickListener = new ItemClickListener() {
        @Override
        public void onItemClick(int itemPosition) {
            Intent intent = new Intent(getActivity(), ChatActivity.class);
            intent.putExtra("chatSelectedUser", deviceListAdapter.getDevices().get(itemPosition));
            startActivity(intent);
        }
    };


    @Override
    public void setUserName(String username) {
        this.username.setText(username);
        MainActivity.deviceName = username;
    }

    private boolean isWiFiEnabled()
    {
        final WifiManager wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
        return wifiManager.isWifiEnabled();
    }

    private boolean isLocationEnabled()
    {
        final LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void askToEnableLocation()
    {
        //build an alert dialog and ask the user to activate the location
        AlertDialog.Builder askFoLocationBuilder = new AlertDialog.Builder(getContext());
        askFoLocationBuilder.setMessage("The application requires that location must be enabled. Would you like to activate now ?");
        askFoLocationBuilder.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        //if Location was enabled, load AvailableDevicesFragment
                        if (isLocationEnabled())
                        {
                            loadAvailableDevicesFragment();
                        }
                    }
                }
        );
        askFoLocationBuilder.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }
        );
        AlertDialog askForWifi = askFoLocationBuilder.create();
        askForWifi.show();
    }

    private void askToEnableWiFi() {
        final WifiManager wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
        //build an alert dialog and ask the user to activate te wifi
        AlertDialog.Builder askForWifiBuilder = new AlertDialog.Builder(getContext());
        askForWifiBuilder.setMessage("WiFi is disabled. Would you like to activate now ?");
        askForWifiBuilder.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        wifiManager.setWifiEnabled(true);
                        //if WiFi was enabled, load AvailableDevicesFragment
//                        loadAvailableDevicesFragment();
                        if (Build.VERSION.SDK_INT >=26) { //check if devices runs Android Oreo or higher
                            if (isLocationEnabled()) {
                                loadAvailableDevicesFragment();
                            }
                            else
                            {
                                askToEnableLocation();
                            }
                        }
                        else
                        {
                            loadAvailableDevicesFragment();
                        }
                    }
                }
        );
        askForWifiBuilder.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }
        );
        AlertDialog askForWifi = askForWifiBuilder.create();
        askForWifi.show();

    }

    WifiP2pManager.ConnectionInfoListener connectionInfo = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info) {


            SharedPreferences sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isGroupFormed", info.groupFormed);
            if(info.groupFormed)
            {
                shouldServiceBeStarted();
            }
        }
    };


    private FragmentTransaction addAnimationToTransaction ()
    {
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.enter_from_top, R.anim.exit_to_top, R.anim.enter_from_top, R.anim.exit_to_top);
        fragmentTransaction.addToBackStack(null);
        return fragmentTransaction;
    }


    private void loadAvailableDevicesFragment()
    {
        AvailableDevicesFragment availableDevices = new AvailableDevicesFragment();
        Bundle fragmentParameters = new Bundle();
        fragmentParameters.putString("messageToDisplay", "Discover users");
        availableDevices.setArguments(fragmentParameters);
        FragmentTransaction fragmentTransaction = addAnimationToTransaction();
        fragmentTransaction.replace(R.id.frameContainer, availableDevices);
        fragmentTransaction.commit();
    }

    private void loadGroupFragment()
    {
        if (!isWiFiEnabled())
        {
            SharedPreferences sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isGroupFormed", false);
        }
        GroupFragment groupFragment = new GroupFragment();
        FragmentTransaction fragmentTransaction = addAnimationToTransaction();
        fragmentTransaction.replace(R.id.frameContainer, groupFragment);
        fragmentTransaction.commit();
    }

    private void shouldServiceBeStarted()
    {
        MCommDatabaseHelper dbHelper = new MCommDatabaseHelper(getContext());
        //start the service if is not started already
        if (!isServiceRunning()) //check if server is started
        {
            SharedPreferences sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
            Intent intent = new Intent(getContext(), CommunicationService.class);
            intent.setAction("initialize");
            intent.putExtra("isHost", sharedPreferences.getBoolean("isHost", false));
            intent.putExtra("hostAddress", sharedPreferences.getString("hostAddress", ""));
            intent.putExtra("deviceName", MainActivity.deviceName);
            getActivity().startService(intent);
        }
    }

    private boolean isServiceRunning()
    {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (CommunicationService.class.toString().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}
