package com.example.myrobotapp.Class.DataTable.History;

import android.content.Context;
import android.util.Log;

import com.example.myrobotapp.Class.DataTable.History.Alarm.AlarmLogEntryModel;
import com.example.myrobotapp.Class.DataTable.History.Operation.OperationLogEntryModel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Deque;
import java.util.Locale;
import java.util.function.Function;

public class LogManager {
    private static final int MAX_LOG_SIZE = 500; // Limit buffer size
    private final Deque<OperationLogEntryModel> operationLogs = new ArrayDeque<>();
    private final Deque<AlarmLogEntryModel> alarmLogs = new ArrayDeque<>();
    private final Context context;

    public LogManager(Context context) {
        this.context = context;
    }

    public void addOperationLog(String message) {
        synchronized (operationLogs) {
            operationLogs.addLast(new OperationLogEntryModel(message));
            if (operationLogs.size() > MAX_LOG_SIZE) {
                operationLogs.pollFirst();
            }
        }
    }

    public void addAlarmLog(String code, String subCode, String message) {
        synchronized (alarmLogs) {
            alarmLogs.addLast(new AlarmLogEntryModel(code, subCode, message));
            if (alarmLogs.size() > MAX_LOG_SIZE) {
                alarmLogs.pollFirst();
            }
        }
    }

    public void saveLogsToFile() {
        saveOperationLogsToFile();
        saveAlarmLogsToFile();
    }

    private void saveOperationLogsToFile() {
        // Update the folder path to include "Log/operations"
        saveLogBufferToFile("operations", operationLogs, entry ->
                entry.getTimestamp() + " - " + entry.getMessage()
        );
    }

    private void saveAlarmLogsToFile() {
        // Update the folder path to include "Log/alarms"
        saveLogBufferToFile("alarms", alarmLogs, entry ->
                entry.getTimestamp() + " | Code: " + entry.getCode() +
                        " | SubCode: " + entry.getSubCode() + " | " + entry.getMessage()
        );
    }

    private <T> void saveLogBufferToFile(String subFolderName, Deque<T> logBuffer, Function<T, String> logFormatter) {
        // Define the parent folder "Log"
        File parentFolder = new File(context.getFilesDir(), "Log");
        if (!parentFolder.exists()) {
            parentFolder.mkdirs(); // Create parent folder if it doesn't exist
        }

        // Define the subfolder (operations or alarms)
        File directory = new File(parentFolder, subFolderName);
        if (!directory.exists()) {
            directory.mkdirs(); // Create subfolder if it doesn't exist
        }

        // Use .txt as the file extension
        String fileName = "logs_" + new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date()) + ".txt";
        File logFile = new File(directory, fileName);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
            while (!logBuffer.isEmpty()) {
                T logEntry = logBuffer.poll();
                writer.write(logFormatter.apply(logEntry));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    interface LogFormatter<T> {
        String format(T entry);
    }
}


