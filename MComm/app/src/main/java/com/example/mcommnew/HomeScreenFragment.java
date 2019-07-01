package com.example.mcommnew;

import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mcommnew.user.IUsername;
import com.example.mcommnew.user.UsernameDialog;
import com.example.mcommnew.user.UsernameDialogListener;

public class HomeScreenFragment extends Fragment implements UsernameDialogListener, IUsername {

    private TextView username;
    private Button changeUsername;
    private MyWifiP2PManager p2PManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_screen_fragment, container, false);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        username = view.findViewById(R.id.textUsername);
        changeUsername = view.findViewById(R.id.changeUsernameButton);
        changeUsername.setOnClickListener(onClickListener);
        p2PManager = new MyWifiP2PManager(getActivity(), getContext());
        p2PManager.setIUsernameListener(this);
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
            if (view.getId() == R.id.changeUsernameButton)
            {
                openChangeUsernameDialog();
            }
        }
    };

    private void openChangeUsernameDialog() {
        UsernameDialog usernameDialog = new UsernameDialog();
        usernameDialog.setUsernameDialogListener(this);
        usernameDialog.show(getActivity().getSupportFragmentManager(), "change username");
    }

    @Override
    public void changeUsername(final String newUsername) {

        WifiP2pManager.ActionListener actionListener = new WifiP2pManager.ActionListener() { //declare listener as a result code for setting username operation
            @Override
            public void onSuccess() {
                Toast.makeText(getActivity(), "Username changed successfully", Toast.LENGTH_SHORT).show();
                username.setText(newUsername);
            }

            @Override
            public void onFailure(int i) {
                Toast.makeText(getActivity(), "WiFi must be enabled to perform this action", Toast.LENGTH_SHORT).show();
            }
        };
        p2PManager.changeUsername(newUsername, actionListener);

    }


    @Override
    public void setUserName(String userName) {
        username.setText(userName);
    }
}
