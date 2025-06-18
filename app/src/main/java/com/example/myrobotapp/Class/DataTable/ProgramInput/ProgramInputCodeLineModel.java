package com.example.myrobotapp.Class.DataTable.ProgramInput;

public class ProgramInputCodeLineModel {
    private int index;
    private String content;

    public ProgramInputCodeLineModel(int index, String content) {
        this.index = index;
        this.content = content;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}

