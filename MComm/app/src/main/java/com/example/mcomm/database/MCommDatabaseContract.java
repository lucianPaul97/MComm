package com.example.mcomm.database;

import android.provider.BaseColumns;

public final class MCommDatabaseContract {

    private MCommDatabaseContract() {
    }

    static class ClientsTable implements BaseColumns {

        static final String TABLE_NAME = "clients";
        static final String COLUMN_NAME_CLIENT = "client_name";
    }

    static class MessagesTable implements BaseColumns {
        static final String TABLE_NAME = "messages";
        static final String COLUMN_NAME_CONTACT = "contact";
        static final String COLUMN_NAME_MESSAGE_CONTENT = "message_content";
        static final String COLUMN_NAME_MESSAGE_TYPE = "message_type"; //received or sent
        static final String COLUMN_NAME_DATE = "date";
    }

    static final String SQL_CREATE_CLIENTS_TABLE =
            "CREATE TABLE " + ClientsTable.TABLE_NAME + " (" +
                    ClientsTable._ID + " INTEGER PRIMARY KEY," +
                    ClientsTable.COLUMN_NAME_CLIENT + " TEXT)";

    static final String SQL_DELETE_CLIENTS_TABLE =
            "DROP TABLE IF EXISTS " + ClientsTable.TABLE_NAME;

    static final String SQL_CREATE_MESSAGES_TABLE =
            "CREATE TABLE " + MessagesTable.TABLE_NAME + " (" +
                    MessagesTable._ID + " INTEGER PRIMARY KEY," +
                    MessagesTable.COLUMN_NAME_CONTACT + " TEXT," +
                    MessagesTable.COLUMN_NAME_MESSAGE_CONTENT + " TEXT,"+
                    MessagesTable.COLUMN_NAME_MESSAGE_TYPE + " INTEGER,"+
                    MessagesTable.COLUMN_NAME_DATE + " TEXT)";

    static final String SQL_DELETE_MESSAGES_TABLE =
            "DROP TABLE IF EXISTS " + MessagesTable.TABLE_NAME;
}
