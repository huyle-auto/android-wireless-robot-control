package com.example.myrobotapp.Class.Communication;

import static com.example.myrobotapp.Class.Bluetooth.BluetoothHelper.socketMap;
import static com.example.myrobotapp.Fragments.Features.Settings.ConnectionFragment.activeConnectionKey;
import static com.example.myrobotapp.Fragments.Features.Settings.ConnectionFragment.activeConnections;
import static com.example.myrobotapp.Fragments.Features.Settings.ConnectionFragment.globalSelectedBTDevice;

import com.example.myrobotapp.Class.TCP.TCPClient;

public class PriorityCommunication {
    public enum ChannelType {
        TCP,
        Bluetooth,
        None
    }

    public static ChannelType checkAvailableChannel() {
        // TCP (Priority, only falls back to Bluetooth if unavailable)
        TCPClient tcpClient = activeConnections.get(activeConnectionKey);
        if (tcpClient != null && tcpClient.isConnected()) {
            return ChannelType.TCP;
        }

        // Bluetooth
        if (socketMap.get(globalSelectedBTDevice) != null) {
            return ChannelType.Bluetooth;
        }

        return ChannelType.None;
    }
}
