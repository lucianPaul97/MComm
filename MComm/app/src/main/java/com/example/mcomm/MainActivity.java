package com.example.mcomm;

import android.Manifest;
import android.content.pm.PackageManager;

import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.example.mcomm.user.CustomDialog;
import com.example.mcomm.user.CustomDialogListener;


public class MainActivity extends AppCompatActivity implements CustomDialogListener {

    private static int ACCESS_COARSE_LOCATION_PERMISSION = 1;
    public static String deviceName;
    public static boolean isJoinedToGroup = false;
    public static Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("MComm");
        setSupportActionBar(toolbar);
        checkPermissions();
        loadHomeFragment();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.newGroup) {
            openCreateGroupDialog();
        }
        return true;
    }

    private void loadHomeFragment() {
        HomeScreenFragment homeScreenFragment = new HomeScreenFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.frameContainer, homeScreenFragment).commit();
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        ACCESS_COARSE_LOCATION_PERMISSION);

            }
        }
    }

    private void openCreateGroupDialog() {
        CustomDialog customDialog = new CustomDialog();
        Bundle arguments = new Bundle();
        arguments.putString("intent", "createGroup");
        customDialog.setArguments(arguments);
        customDialog.setUsernameDialogListener(this);
        customDialog.show(this.getSupportFragmentManager(), "change username");
    }

    @Override
    public void createGroup(String groupName) {
        MyWifiP2PManager p2PManager = new MyWifiP2PManager(this, MainActivity.this);
        p2PManager.createGroup();
    }

    @Override
    public void changeUsername(String newUsername) {
        //not implementing here
    }

}
