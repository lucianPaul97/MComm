package com.example.mcommnew.notifications;

import android.app.Application;
import android.app.NotificationChannel;
import android.os.Build;

public class NotificationManager extends Application {

    public static final String CHANNEL_ID = "MComm_Service";
    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "MComm Serice",
                    android.app.NotificationManager.IMPORTANCE_LOW

            );
            android.app.NotificationManager notificationManager = getSystemService(android.app.NotificationManager.class);
            notificationManager.createNotificationChannel(serviceChannel);
        }
    }
}
