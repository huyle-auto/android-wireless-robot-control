package com.example.myrobotapp.Class;

import android.widget.EditText;

import com.example.myrobotapp.Class.TCP.TCPClient;

import java.util.concurrent.ConcurrentHashMap;

public class GlobalData {
    // Singleton instance of GlobalData
    private static GlobalData instance;

    // Shared data: HashMap and EditText
    private ConcurrentHashMap<String, TCPClient> activeConnections;
    private EditText sharedEditText;

    // Private constructor to ensure Singleton pattern
    private GlobalData() {
        // Initially, HashMap can be null until it's set by the fragment
        activeConnections = null;
    }

    // Method to get the Singleton instance
    public static synchronized GlobalData getInstance() {
        if (instance == null) {
            instance = new GlobalData();
        }
        return instance;
    }

    // Method to link an external HashMap
    public void setActiveConnections(ConcurrentHashMap<String, TCPClient> activeConnections) {
        this.activeConnections = activeConnections;
    }

    // Method to get the linked HashMap
    public ConcurrentHashMap<String, TCPClient> getActiveConnections() {
        return activeConnections;
    }

    // EditText-related methods
    public EditText getSharedEditText() {
        return sharedEditText;
    }

    public void setSharedEditText(EditText editText) {
        this.sharedEditText = editText;
    }
}
