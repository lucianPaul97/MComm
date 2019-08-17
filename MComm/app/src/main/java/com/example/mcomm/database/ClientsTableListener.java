package com.example.mcomm.database;


public interface ClientsTableListener {

    void onClientAdded(String newClients);
    void onClientDeleted(String client);
}
