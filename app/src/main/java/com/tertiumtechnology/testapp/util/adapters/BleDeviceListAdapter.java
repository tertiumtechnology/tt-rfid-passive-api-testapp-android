package com.tertiumtechnology.testapp.util.adapters;

import android.bluetooth.BluetoothDevice;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tertiumtechnology.testapp.R;

import java.util.ArrayList;

public class BleDeviceListAdapter extends RecyclerView.Adapter<BleDeviceListAdapter.ViewHolder> {

    public interface OnDeviceClickListener {
        void onDeviceClick(BluetoothDevice device);
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        AppCompatTextView deviceAddress;
        AppCompatTextView deviceName;

        ViewHolder(View itemView) {
            super(itemView);
            deviceName = itemView.findViewById(R.id.device_name);
            deviceAddress = itemView.findViewById(R.id.device_address);
        }

        private void bind(final BluetoothDevice device, final OnDeviceClickListener listener) {
            String deviceNameString = device.getName();
            if (deviceNameString != null && deviceNameString.length() > 0) {
                deviceName.setText(deviceNameString);
            }
            else {
                deviceName.setText(R.string.unknown_device);
            }
            deviceAddress.setText(device.getAddress());

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onDeviceClick(device);
                }
            });

        }
    }

    private OnDeviceClickListener listener;
    private ArrayList<BluetoothDevice> bleDevices;

    public BleDeviceListAdapter(OnDeviceClickListener listener) {
        this.bleDevices = new ArrayList<>();
        this.listener = listener;
    }

    public void addDevice(BluetoothDevice device) {
        if (!bleDevices.contains(device)) {
            bleDevices.add(device);
        }
    }

    public void clear() {
        bleDevices.clear();
    }

    @Override
    public int getItemCount() {
        return bleDevices.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bind(bleDevices.get(position), listener);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_list_item, parent, false);

        return new ViewHolder(view);
    }
}