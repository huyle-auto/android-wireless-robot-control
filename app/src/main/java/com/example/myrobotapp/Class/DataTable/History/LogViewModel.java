package com.example.myrobotapp.Class.DataTable.History;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.myrobotapp.Class.DataTable.History.Alarm.AlarmLogEntryModel;
import com.example.myrobotapp.Class.DataTable.History.Operation.OperationLogEntryModel;

import java.util.ArrayList;
import java.util.List;

public class LogViewModel extends ViewModel {
    private final MutableLiveData<List<OperationLogEntryModel>> operationLogs = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<AlarmLogEntryModel>> alarmLogs = new MutableLiveData<>(new ArrayList<>());

    public LiveData<List<OperationLogEntryModel>> getOperationLogs() {
        return operationLogs;
    }

    public LiveData<List<AlarmLogEntryModel>> getAlarmLogs() {
        return alarmLogs;
    }

    public void addOperationLog(String message) {
        List<OperationLogEntryModel> currentLogs = new ArrayList<>(operationLogs.getValue());
        currentLogs.add(new OperationLogEntryModel(message));
        operationLogs.setValue(currentLogs);
    }

    public void addAlarmLog(String code, String subCode, String message) {
        List<AlarmLogEntryModel> currentLogs = new ArrayList<>(alarmLogs.getValue());
        currentLogs.add(new AlarmLogEntryModel(code, subCode, message));
        alarmLogs.setValue(currentLogs);
    }
}


