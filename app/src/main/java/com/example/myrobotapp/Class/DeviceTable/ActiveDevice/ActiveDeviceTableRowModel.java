package com.example.myrobotapp.Class.DeviceTable.ActiveDevice;

public class ActiveDeviceTableRowModel {
    private int index;
    private String name;

    public ActiveDeviceTableRowModel(int index, String name) {
        this.index = index;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setName(String name) {
        this.name = name;
    }
}
