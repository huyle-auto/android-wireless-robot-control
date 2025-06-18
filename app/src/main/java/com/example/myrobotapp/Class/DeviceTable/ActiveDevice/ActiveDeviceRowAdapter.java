package com.example.myrobotapp.Class.DeviceTable.ActiveDevice;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myrobotapp.Class.UserInput.NumpadPopup;
import com.example.myrobotapp.Fragments.Features.Settings.DevicesFragment;
import com.example.myrobotapp.R;

import java.util.ArrayList;
import java.util.List;

public class ActiveDeviceRowAdapter extends RecyclerView.Adapter<ActiveDeviceRowAdapter.ViewHolder> {

    Context context;
    List<ActiveDeviceTableRowModel> activeDeviceList;
    private TableRow selectedTableRow;
    public static int selectedPosition = -1;

    public ActiveDeviceRowAdapter(Context context, List<ActiveDeviceTableRowModel> activeDeviceList) {
        this.context = context;
        this.activeDeviceList = activeDeviceList;
    }

    @NonNull
    @Override
    public ActiveDeviceRowAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.active_device_table_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActiveDeviceRowAdapter.ViewHolder holder, int position) {
        ActiveDeviceTableRowModel device = activeDeviceList.get(position);

        holder.activeDeviceIndex.setText(String.valueOf(position + 1));
        holder.activeDeviceName.setText(device.getName());

        holder.activeDeviceTableRow.setBackgroundColor(
                position == selectedPosition ? Color.parseColor("#A9A9A9") : Color.parseColor("#4E1496BB")
        );

        holder.activeDeviceTableRow.setOnClickListener(v -> {
            selectedPosition = holder.getAdapterPosition();
            notifyDataSetChanged();
        });
    }

    public void showActiveDevice() {
        System.out.println("active Devices are: \n");
        for (int i = 0; i < activeDeviceList.size(); i++) {
            System.out.println(activeDeviceList.get(i).getName() + ", ");
        }
    }

    public void addDevice(String deviceName) {
        activeDeviceList.add(new ActiveDeviceTableRowModel(getItemCount() + 1, deviceName));
        DevicesFragment.activeDeviceList = activeDeviceList;    // Update back to parent class

        notifyDataSetChanged();
    }

    public void removeDevice() {
        if (selectedPosition >= 0 && selectedPosition < activeDeviceList.size()) {
            activeDeviceList.remove(selectedPosition);

            // Reset selection
            selectedPosition = -1;

            DevicesFragment.activeDeviceList = activeDeviceList;    // Update back to parent class

            notifyDataSetChanged();
        } else {
            showToast("No Active Device selected", false);
        }
    }

    @Override
    public int getItemCount() {
        return activeDeviceList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TableRow activeDeviceTableRow;
        TextView activeDeviceIndex, activeDeviceName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            activeDeviceTableRow = itemView.findViewById(R.id.activeDeviceTableRow);
            activeDeviceIndex = itemView.findViewById(R.id.activeDeviceIndex);
            activeDeviceName = itemView.findViewById(R.id.activeDeviceName);
        }
    }

    private void showToast(String message, boolean longToast) {

        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(context, message, longToast ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show()
        );
    }

}
