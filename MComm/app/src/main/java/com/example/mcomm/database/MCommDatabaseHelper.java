package com.example.mcomm.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.mcomm.group.chat.messages.Message;
import com.example.mcomm.group.chat.messages.MessageType;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MCommDatabaseHelper extends SQLiteOpenHelper {

    private SQLiteDatabase dbWritable;
    private SQLiteDatabase dbReadable;
    private static final int DATABASE_VERSION = 6;
    private static final String DATABASE_NAME = "MComm.db";
    private static ClientsTableListener mClientsTableListener;
    private static MessagesTableListener mMessagesTableListener;

    public MCommDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        dbWritable = this.getWritableDatabase();
        dbReadable = this.getReadableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(MCommDatabaseContract.SQL_CREATE_CLIENTS_TABLE);
        sqLiteDatabase.execSQL(MCommDatabaseContract.SQL_CREATE_MESSAGES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public boolean addClient(String client) {
        ContentValues values = new ContentValues();
        values.put(MCommDatabaseContract.ClientsTable.COLUMN_NAME_CLIENT, client);
        long clientID = dbWritable.insert(MCommDatabaseContract.ClientsTable.TABLE_NAME, null, values);
        if (clientID != -1) {
            if (mClientsTableListener != null) {
                mClientsTableListener.onClientAdded(client);
            }
            return true;
        }
        return false;
    }

    public boolean addMessage(String contact, String content, int type, Date date) {
        ContentValues values = new ContentValues();
        values.put(MCommDatabaseContract.MessagesTable.COLUMN_NAME_CONTACT, contact);
        values.put(MCommDatabaseContract.MessagesTable.COLUMN_NAME_MESSAGE_CONTENT, content);
        values.put(MCommDatabaseContract.MessagesTable.COLUMN_NAME_MESSAGE_TYPE, type);
        //set the sending/receiving date of the message
        values.put(MCommDatabaseContract.MessagesTable.COLUMN_NAME_DATE, DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(date));
        long messageID = dbWritable.insert(MCommDatabaseContract.MessagesTable.TABLE_NAME, null, values);
        Log.d("message added", content);
        if (messageID != -1) {
            if (type == MessageType.RECEIVED.ordinal() && mMessagesTableListener != null) {
                mMessagesTableListener.onMessageReceived(contact, content, date);
            }
            return true;
        }
        return false;
    }

    public void deleteAllClients() {
        dbWritable.delete(MCommDatabaseContract.ClientsTable.TABLE_NAME, null, null);
    }

    public void deleteClient(String client) {
        String selection = MCommDatabaseContract.ClientsTable.COLUMN_NAME_CLIENT + " LIKE ?";
        String[] selectionArgs = {client};

        dbWritable.delete(MCommDatabaseContract.ClientsTable.TABLE_NAME, selection, selectionArgs);
        if (mClientsTableListener != null) {
            mClientsTableListener.onClientDeleted(client);
        }

    }

    public int getClientsCount() {
        int clientNumber = 0;
        String query = String.format("SELECT count(*) FROM %s;", MCommDatabaseContract.ClientsTable.TABLE_NAME);
        Cursor cursor = dbReadable.rawQuery(query, null);
        if (cursor.moveToNext()) {
            clientNumber = cursor.getInt(0);
        }
        cursor.close();
        return clientNumber;
    }

    public List<String> getAllClients() {
        List<String> clients = new ArrayList<>();
        String query = String.format("SELECT %s FROM %s GROUP BY %s;", MCommDatabaseContract.ClientsTable.COLUMN_NAME_CLIENT, MCommDatabaseContract.ClientsTable.TABLE_NAME, MCommDatabaseContract.ClientsTable.COLUMN_NAME_CLIENT);
        Cursor cursor = dbReadable.rawQuery(query, null);
        while (cursor.moveToNext()) {
            clients.add(cursor.getString(cursor.getColumnIndex(MCommDatabaseContract.ClientsTable.COLUMN_NAME_CLIENT)));
        }
        cursor.close();
        return clients;
    }

    public List<Message> getMessages(String contact, int limit) {
        List<Message> messages = new ArrayList<>();
        String query = String.format("SELECT %s, %s, %s FROM %s WHERE contact=\"%s\" LIMIT %s;", MCommDatabaseContract.MessagesTable.COLUMN_NAME_MESSAGE_CONTENT, MCommDatabaseContract.MessagesTable.COLUMN_NAME_MESSAGE_TYPE,
                MCommDatabaseContract.MessagesTable.COLUMN_NAME_DATE, MCommDatabaseContract.MessagesTable.TABLE_NAME, contact, String.valueOf(limit));
        Cursor cursor = dbReadable.rawQuery(query, null);
        while (cursor.moveToNext()) {
            messages.add(new Message(cursor.getString(cursor.getColumnIndex(MCommDatabaseContract.MessagesTable.COLUMN_NAME_MESSAGE_CONTENT)),
                    MessageType.valueOf(cursor.getInt(cursor.getColumnIndex(MCommDatabaseContract.MessagesTable.COLUMN_NAME_MESSAGE_TYPE))),
                    cursor.getString(cursor.getColumnIndex(MCommDatabaseContract.MessagesTable.COLUMN_NAME_DATE))));
        }
        cursor.close();
        return messages;
    }

    public boolean isClientStored(String client) {
        int result = 0;
        String query = String.format("SELECT * FROM %s WHERE client_name=\"%s\";", MCommDatabaseContract.ClientsTable.TABLE_NAME, client);
        Cursor cursor = dbReadable.rawQuery(query, null);
        if (cursor.moveToNext()) {
            result = cursor.getCount();
        }
        cursor.close();
        return (result > 0);
    }

    public List<String> getRecentContact() //the method returns a list of contacts you have had a conversation with in the last 5 days
    {
        List<String> recentContact = new ArrayList<>();
        String query = String.format("SELECT %s FROM %s WHERE %s > date(\'now\', \'-5 days\') GROUP BY %s;", MCommDatabaseContract.MessagesTable.COLUMN_NAME_CONTACT, MCommDatabaseContract.MessagesTable.TABLE_NAME,
                MCommDatabaseContract.MessagesTable.COLUMN_NAME_DATE, MCommDatabaseContract.MessagesTable.COLUMN_NAME_CONTACT);
        Cursor cursor = dbReadable.rawQuery(query, null);
        while(cursor.moveToNext())
        {
            recentContact.add(cursor.getString(cursor.getColumnIndex(MCommDatabaseContract.MessagesTable.COLUMN_NAME_CONTACT)));
        }
        query = query.replace(">", "<");
        cursor = dbReadable.rawQuery(query, null);
        while(cursor.moveToNext())
        {
            recentContact.add(cursor.getString(cursor.getColumnIndex(MCommDatabaseContract.MessagesTable.COLUMN_NAME_CONTACT)));
        }
        cursor.close();
        return recentContact;
    }

    public void setClientsTableListener(ClientsTableListener clientsTableListener) {
        mClientsTableListener = clientsTableListener;
    }

    public void setMessagesTableListener(MessagesTableListener messagesTableListener) {
        mMessagesTableListener = messagesTableListener;
    }

}
