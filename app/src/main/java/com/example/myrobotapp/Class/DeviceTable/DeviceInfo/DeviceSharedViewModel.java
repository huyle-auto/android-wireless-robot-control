package com.example.myrobotapp.Class.DeviceTable.DeviceInfo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class DeviceSharedViewModel extends ViewModel {
    private final MutableLiveData<List<DeviceTableRowModel>> rxPdoTableData = new MutableLiveData<>();
    private final MutableLiveData<List<DeviceTableRowModel>> txPdoTableData = new MutableLiveData<>();
    private final MutableLiveData<List<DeviceTableRowModel>> pdoTableData = new MutableLiveData<>();
    private final MutableLiveData<List<DeviceTableRowModel>> syncTableData = new MutableLiveData<>();

    // Setters for each table's data
    public void setTable1Data(List<DeviceTableRowModel> data) {
        rxPdoTableData.setValue(data);
    }

    public void setTable2Data(List<DeviceTableRowModel> data) {
        txPdoTableData.setValue(data);
    }

    public void setTable3Data(List<DeviceTableRowModel> data) {
        pdoTableData.setValue(data);
    }

    public void setTable4Data(List<DeviceTableRowModel> data) {
        syncTableData.setValue(data);
    }

    // Getters for each table's data
    public LiveData<List<DeviceTableRowModel>> getTable1Data() {
        return rxPdoTableData;
    }

    public LiveData<List<DeviceTableRowModel>> getTable2Data() {
        return txPdoTableData;
    }

    public LiveData<List<DeviceTableRowModel>> getTable3Data() {
        return pdoTableData;
    }

    public LiveData<List<DeviceTableRowModel>> getTable4Data() {
        return syncTableData;
    }
}

