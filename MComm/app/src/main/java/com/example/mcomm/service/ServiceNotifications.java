package com.example.mcomm.service;

import org.json.JSONObject;

public interface ServiceNotifications {

    void onNewMessageToRoute(JSONObject message);
}
