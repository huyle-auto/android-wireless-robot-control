package com.example.myrobotapp.Class.DataTable.History.Operation;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class OperationLogEntryModel {
    private String timestamp;
    private String message;

    public OperationLogEntryModel(String message) {
        this.timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
