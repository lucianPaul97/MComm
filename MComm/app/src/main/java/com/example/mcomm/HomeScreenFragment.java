package com.example.mcomm;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mcomm.available_devices.AvailableDevicesFragment;
import com.example.mcomm.user.IUsername;
import com.example.mcomm.user.CustomDialog;
import com.example.mcomm.user.CustomDialogListener;

public class HomeScreenFragment extends Fragment implements CustomDialogListener, IUsername {

    private TextView username;
    private MyWifiP2PManager p2PManager;

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
                    loadAvailableDevicesFragment();
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
                        loadAvailableDevicesFragment();
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

    private void loadAvailableDevicesFragment()
    {
        AvailableDevicesFragment availableDevices = new AvailableDevicesFragment();
        Bundle fragmentParameters = new Bundle();
        fragmentParameters.putString("messageToDisplay", "Discover users");
        availableDevices.setArguments(fragmentParameters);
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameContainer, availableDevices).commit();
    }

    private void loadGroupFragment()
    {
        GroupFragment groupFragment = new GroupFragment();
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameContainer, groupFragment).commit();
    }
}
