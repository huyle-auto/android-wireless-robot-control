package com.example.myrobotapp.Class.DataTable.ProgramFile;

public class ProgramDataTableRowModel {
    private String programName, tag, timeStamp, attribute;

    public ProgramDataTableRowModel(String programName, String tag, String timeStamp, String attribute) {
        this.programName = programName;
        this.tag = tag;
        this.timeStamp = timeStamp;
        this.attribute = attribute;
    }

    public String getProgramName() {
        return programName;
    }

    public String getTag() {
        return tag;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }
}
