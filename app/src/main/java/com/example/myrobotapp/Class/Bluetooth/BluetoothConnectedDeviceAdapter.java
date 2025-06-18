package com.example.myrobotapp.Class.Bluetooth;

import static com.example.myrobotapp.Fragments.Features.Settings.ConnectionFragment.globalSelectedBTDevice;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import android.bluetooth.BluetoothDevice;

import com.example.myrobotapp.R;

public class BluetoothConnectedDeviceAdapter extends RecyclerView.Adapter<BluetoothConnectedDeviceAdapter.ViewHolder> {
    private final Context context;
    private final List<BluetoothDevice> devices;
    private final onConnectedDeviceClickListener listener;
    private int selectedPosition = RecyclerView.NO_POSITION;

    public interface onConnectedDeviceClickListener {
        void onDeviceSelected(BluetoothDevice device);
    }

    public BluetoothConnectedDeviceAdapter(Context context, List<BluetoothDevice> devices, onConnectedDeviceClickListener listener) {
        this.context = context;
        this.devices = new ArrayList<>(devices);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bluetooth_connected_device_table_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BluetoothDevice device = devices.get(position);

        String deviceName = "";
        if (Build.VERSION.SDK_INT >= 31) {
            deviceName = (device != null && ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) ? device.getName() : "Permission BLUETOOTH_CONNECT for API > 31";
        }
        else if(Build.VERSION.SDK_INT >= 24) {
            deviceName = (device != null && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) ? device.getName() : "Permission LOCATION for 24 <= API < 31";

        }
        holder.bluetoothConnectedDeviceNameTextView.setText(deviceName != null ? deviceName : "Unknown Device");

        // Change background color if selected
        if (selectedPosition == position) {
            holder.itemView.setBackgroundColor(Color.parseColor("#A9A9A9")); // Highlight selected
        }
        else {
            holder.itemView.setBackgroundColor(Color.parseColor("#4E1496BB")); // Default color
        }

        // Handle row click to select device
        holder.itemView.setOnClickListener(v -> {
            selectedPosition = holder.getAdapterPosition(); // Update selected position
            notifyDataSetChanged(); // Refresh UI
            listener.onDeviceSelected(device); // Send selected device to MainActivity
        });

        if (globalSelectedBTDevice != null && globalSelectedBTDevice.equals(device)) {
            holder.itemView.setBackgroundColor(Color.parseColor("#A9A9A9")); // Highlight selected
        }
        else if (globalSelectedBTDevice != null && !globalSelectedBTDevice.equals(device)) {
            holder.itemView.setBackgroundColor(Color.parseColor("#4E1496BB")); // Default color
        }
    }

    public void updateDevices(List<BluetoothDevice> newDevices) {
        devices.clear();
        devices.addAll(newDevices);
        notifyDataSetChanged();
        // System.out.println("Called update connected devices......... with " + devices.size() + " devices");
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView bluetoothConnectedDeviceNameTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            bluetoothConnectedDeviceNameTextView = itemView.findViewById(R.id.bluetoothConnectedDeviceNameTextView);
        }
    }
}

