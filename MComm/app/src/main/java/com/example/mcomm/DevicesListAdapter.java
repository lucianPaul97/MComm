package com.example.mcomm;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

class DevicesListAdapter extends RecyclerView.Adapter<DevicesListAdapter.MyViewHolder> {

    private String [] devices;

    public DevicesListAdapter (String [] devicesNames)
    {
        //TODO: find out what type of arguments are required
        devices = devicesNames;

    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        ImageView itemIcon;
        TextView itemText;

        public MyViewHolder (View itemView)
        {
            super(itemView);
            itemIcon  = itemView.findViewById(R.id.itemIcon);
            itemText = itemView.findViewById(R.id.itemText);
        }
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.list_item_layout, viewGroup, false);
        return new MyViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        String title = devices[i];
        myViewHolder.itemText.setText(title);

    }

    @Override
    public int getItemCount() {
        return devices.length;
    }
}
