package com.example.myrobotapp.Class.Communication;

import static com.example.myrobotapp.Fragments.Features.Settings.ConnectionFragment.rssi;

import android.content.Context;
import android.util.Log;

import com.example.myrobotapp.Class.FileManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FrameTracker {
    final String TAG = "FrameTracker";

    public static String FRAME_TRACKING_FOLDER_NAME = "Log/frame_tracking";
    public static String TCP_FRAME_TRACKING_FILE_NAME = "tcp_tracking.csv";
    public static String BT_FRAME_TRACKING_FILE_NAME = "bt_tracking.csv";

    public enum ChannelType {
        TCP,
        Bluetooth
    }

    // Small class for a frame
    public static class FrameEntry {
        public int frameLogId;
        public long sendTime;     // Unix timestamp in ms
        public long receiveTime;  // Unix timestamp in ms
        public int sizeInBytes;

        public FrameEntry(int frameLogId, long sendTime, long receiveTime, int sizeInBytes) {
            this.frameLogId = frameLogId;
            this.sendTime = sendTime;
            this.receiveTime = receiveTime;
            this.sizeInBytes = sizeInBytes;
        }

        public long getRttMs() {
            return receiveTime - sendTime;
        }

        public double getBandwidthKbps() {
            long durationMs = receiveTime - sendTime;
            return (durationMs > 0) ? (sizeInBytes * 8.0) / durationMs : 0;
        }
    }

    private final ChannelType channelName;
    private final List<FrameEntry> entries = new ArrayList<>();

    // Different instances for different channels
    public FrameTracker(ChannelType channelName) {
        this.channelName = channelName;
    }

    public void log(int frameLogId, long sendTime, long receiveTime, int sizeInBytes) {
        entries.add(new FrameEntry(frameLogId, sendTime, receiveTime, sizeInBytes));
    }

    public List<FrameEntry> getEntries() {
        return entries;
    }

    public ChannelType getChannelName() {
        return channelName;
    }

    public String toCsvLines(Context context, ChannelType channelType, List<FrameEntry> entries) {
        // CSV export: returns lines for writing to file
        List<String> csv = new ArrayList<>();

        File folder = new File(context.getFilesDir(), FRAME_TRACKING_FOLDER_NAME);
        File file = new File(folder, channelType == ChannelType.TCP ? TCP_FRAME_TRACKING_FILE_NAME : BT_FRAME_TRACKING_FILE_NAME);

        // Add columns
        if (!file.exists()) {
            if (channelType == ChannelType.TCP) {
                csv.add("FrameID,SendTime,ReceiveTime,RoundTripTimeMs,SizeBytes,BandwidthKbps");
            }
            else {
                csv.add("FrameID,SendTime,ReceiveTime,RoundTripTimeMs,SizeBytes,BandwidthKbps,RSSI");
            }
        }

        // Add rows
        if (channelType == ChannelType.TCP) {
            for (FrameEntry entry : entries) {
                csv.add(String.format(Locale.US,
                        "%d,%d,%d,%d,%d,%.2f",
                        entry.frameLogId,
                        entry.sendTime,
                        entry.receiveTime,
                        entry.getRttMs(),
                        entry.sizeInBytes,
                        entry.getBandwidthKbps()
                ));
            }
        }
        else {
             for (FrameEntry entry : entries) {
                csv.add(String.format(Locale.US,
                        "%d,%d,%d,%d,%d,%.2f,%d",
                        entry.frameLogId,
                        entry.sendTime,
                        entry.receiveTime,
                        entry.getRttMs(),
                        entry.sizeInBytes,
                        entry.getBandwidthKbps(),
                        rssi
                ));
            }
        }


        return String.join("\n", csv) + "\n";
    }

    public void exportToCsv(Context context, ChannelType channelType) {
        File logFile = FileManager.createTempTextFile(context, toCsvLines(context, channelType, entries), "anyName");
        entries.clear();    // Append mode used

        if (channelType == ChannelType.TCP) {
            FileManager.writeToDir(context, FRAME_TRACKING_FOLDER_NAME, TCP_FRAME_TRACKING_FILE_NAME, logFile, true);
        }
        else {
            FileManager.writeToDir(context, FRAME_TRACKING_FOLDER_NAME, BT_FRAME_TRACKING_FILE_NAME, logFile, true);
        }
    }
}
