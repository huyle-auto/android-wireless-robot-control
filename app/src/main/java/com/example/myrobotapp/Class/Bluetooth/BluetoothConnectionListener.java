package com.example.myrobotapp.Class.Bluetooth;

import android.bluetooth.BluetoothDevice;

import java.util.Set;

public interface BluetoothConnectionListener {
    void onConnectedDevicesChanged(Set<BluetoothDevice> connectedDevices, BluetoothHelper.ConnectionState connectionState, BluetoothHelper.Disconnector disconnector);
}
