package com.example.myrobotapp.Class.DataTable.History.Alarm;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AlarmLogEntryModel {
    private final String timestamp;
    private final String code;
    private final String subCode;
    private final String message;

    public AlarmLogEntryModel(String code, String subCode, String message) {
        this.timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());
        this.code = code;
        this.subCode = subCode;
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getCode() {
        return code;
    }

    public String getSubCode() {
        return subCode;
    }

    public String getMessage() {
        return message;
    }
}


