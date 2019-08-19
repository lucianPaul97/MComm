package com.example.mcomm.available_devices;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mcomm.R;

import java.util.List;

public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.MyViewHolder> {
    private List<String> devices;
    private ItemClickListener mListener;

    public DeviceListAdapter(List<String> devicesNames) {
        devices = devicesNames;
    }

    public void setOnItemClickListener(ItemClickListener listener) {
        mListener = listener;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView itemIcon;
        TextView itemText;

        public MyViewHolder(View itemView, final ItemClickListener listener) {
            super(itemView);
            itemIcon = itemView.findViewById(R.id.itemIcon);
            itemText = itemView.findViewById(R.id.itemText);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.available_device_list, viewGroup, false);
        return new MyViewHolder(view, mListener);

    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        String title = devices.get(i);
        myViewHolder.itemText.setText(title);

    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public void updateList(List<String> deviceList) {
        this.devices.clear();
        this.devices.addAll(deviceList);
        notifyDataSetChanged();
    }

    public List<String> getDevices()
    {
        return devices;
    }
}


