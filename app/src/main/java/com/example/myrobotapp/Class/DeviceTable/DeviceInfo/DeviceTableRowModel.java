package com.example.myrobotapp.Class.DeviceTable.DeviceInfo;

public class DeviceTableRowModel {
    private String id;       // ID of the entry, e.g., "Entry0", "Entry1", etc.
    private String group;    // Group of the entry, e.g., "SV0"
    private String index;    // Index, e.g., "0x6040"
    private String subIndex; // SubIndex, e.g., "0x00"
    private String size;        // Size in bits, e.g., 16, 32
    private String role;     // Role, e.g., "DEFAULT"

    // Constructor
    public DeviceTableRowModel(String id, String group, String index, String subIndex, String size, String role) {
        this.id = id;
        this.group = group;
        this.index = index;
        this.subIndex = subIndex;
        this.size = size;
        this.role = role;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getGroup() { return group; }
    public void setGroup(String group) { this.group = group; }

    public String getIndex() { return index; }
    public void setIndex(String index) { this.index = index; }

    public String getSubIndex() { return subIndex; }
    public void setSubIndex(String subIndex) { this.subIndex = subIndex; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}

