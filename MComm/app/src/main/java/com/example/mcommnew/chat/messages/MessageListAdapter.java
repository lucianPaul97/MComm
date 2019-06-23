package com.example.mcommnew.chat.messages;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mcommnew.R;

import java.util.List;

public class MessageListAdapter extends RecyclerView.Adapter {

    private Context mContext;
    private List<Message> mMessageList;

    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    public MessageListAdapter(Context context, List<Message> messageList) {
        this.mContext = context;
        this.mMessageList = messageList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_send, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_received, parent, false);
        }
        return new MessageHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        Message message = mMessageList.get(position);
        ((MessageHolder) holder).bind(message);

    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }


    @Override
    public int getItemViewType(int position) {
        Message message = mMessageList.get(position);

        if (message.messageType == VIEW_TYPE_MESSAGE_SENT) {
            // If the current user is the sender of the message
            return VIEW_TYPE_MESSAGE_SENT;
        } else {
            // If some other user sent the message
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    private class MessageHolder extends RecyclerView.ViewHolder {
        private TextView messageText, timeText;

        private MessageHolder(View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.text_message_body);
            timeText = itemView.findViewById(R.id.text_message_time);
        }

        private void bind(Message message) {
            messageText.setText(message.message);
            timeText.setText(message.createdAt);
        }

    }
}
