package com.example.mcommnew;
import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.mcommnew.available_devices.AvailableDevicesFragment;

public class MainActivity extends AppCompatActivity {

    private static int ACCESS_COARSE_LOCATION_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("MComm");
        toolbar.setTitleTextColor(getResources().getColor(R.color.colorPrimaryDark));
        setSupportActionBar(toolbar);
        checkPermissions();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        for (int itemPosition = 0; itemPosition < menu.size(); ++itemPosition) {
            MenuItem item = menu.getItem(itemPosition);
            SpannableString s = new SpannableString(item.getTitle());
            s.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorPrimaryDark)), 0, s.length(), 0);
            item.setTitle(s);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.newGroup:
                //TODO: implement this feature
                break;
            case R.id.discoverGroups:
                if (isWiFiEnabled()) {
                    loadAvailableDevicesFragment();
                }
                else
                {
                    askToEnableWiFi();
                }
                break;

        }
        return true;
    }

    private boolean isWiFiEnabled()
    {
        final WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        return wifiManager.isWifiEnabled();
    }

    private void askToEnableWiFi() {
        final WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) { //check if WiFi is enabled
            //build an alert dialog and ask the user to activate te wifi
            AlertDialog.Builder askForWifiBuilder = new AlertDialog.Builder(this);
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
    }

    private void loadAvailableDevicesFragment()
    {
        AvailableDevicesFragment availableDevices = new AvailableDevicesFragment();
        Bundle fragmentParameters = new Bundle();
        fragmentParameters.putString("messageToDisplay", "Discover devices");
        availableDevices.setArguments(fragmentParameters);
        getSupportFragmentManager().beginTransaction().replace(R.id.frameContainer, availableDevices).commit();
    }
    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        ACCESS_COARSE_LOCATION_PERMISSION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
        }

    }
}
