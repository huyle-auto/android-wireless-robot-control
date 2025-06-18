package com.example.myrobotapp.Class.TCP;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myrobotapp.R;

import java.util.ArrayList;
import java.util.List;

public class TCPConnectedDeviceAdapter extends RecyclerView.Adapter<TCPConnectedDeviceAdapter.ViewHolder> {
    private final Context context;
    private final List<String> devices;
    private final com.example.myrobotapp.Class.TCP.TCPConnectedDeviceAdapter.onConnectedDeviceClickListener listener;
    private int selectedPosition = RecyclerView.NO_POSITION;

    public interface onConnectedDeviceClickListener {
        void onDeviceSelected(String device);
    }

    public TCPConnectedDeviceAdapter(Context context, List<String> devices, com.example.myrobotapp.Class.TCP.TCPConnectedDeviceAdapter.onConnectedDeviceClickListener listener) {
        this.context = context;
        this.devices = new ArrayList<>(devices);
        this.listener = listener;
    }

    @NonNull
    @Override
    public com.example.myrobotapp.Class.TCP.TCPConnectedDeviceAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tcp_connected_device_table_row, parent, false);
        return new com.example.myrobotapp.Class.TCP.TCPConnectedDeviceAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull com.example.myrobotapp.Class.TCP.TCPConnectedDeviceAdapter.ViewHolder holder, int position) {
        String device = devices.get(position);
        holder.tcpConnectedDeviceNameTextView.setText(device);

        // Change background color if selected
        if (selectedPosition == position) {
            holder.itemView.setBackgroundColor(Color.parseColor("#A9A9A9")); // Highlight selected
        } else {
            holder.itemView.setBackgroundColor(Color.parseColor("#4E1496BB")); // Default color
        }

        // Handle row click to select device
        holder.itemView.setOnClickListener(v -> {
            selectedPosition = holder.getAdapterPosition(); // Update selected position
            notifyDataSetChanged(); // Refresh UI
            listener.onDeviceSelected(device); // Send selected device to MainActivity
        });
    }

    public void updateDevices(List<String> newDevices) {
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
        TextView tcpConnectedDeviceNameTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tcpConnectedDeviceNameTextView = itemView.findViewById(R.id.tcpConnectedDeviceNameTextView);
        }
    }
}
