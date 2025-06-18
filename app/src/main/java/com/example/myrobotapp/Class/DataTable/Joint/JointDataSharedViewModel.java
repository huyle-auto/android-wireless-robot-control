package com.example.myrobotapp.Class.DataTable.Joint;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class JointDataSharedViewModel extends ViewModel {
    private final MutableLiveData<List<JointDataTableRowModel>> inputJointData = new MutableLiveData<>(new ArrayList<>());
    private List<JointDataTableRowModel> currentData;

    // Retrieve Rows Data
    public LiveData<List<JointDataTableRowModel>> getInputJointData() {
        return inputJointData;
    }

    // Clear all Data
    public void clearJointData(){
        inputJointData.setValue(new ArrayList<>());
    }

    // Update the whole list
    public void updateJointData(List<JointDataTableRowModel> jointData){
        clearJointData();
        currentData = inputJointData.getValue();
        if (currentData != null){
            currentData.addAll(jointData);
            inputJointData.setValue(currentData);
        }
    }

    // Append new model (row data) to old rows
    public void addJointData(JointDataTableRowModel data) {
        currentData = inputJointData.getValue();

        if (currentData != null && !addDataIfNotExists(data)){
            currentData.add(data);
            inputJointData.setValue(currentData);
        }
    }

    // Delete single model using index
    public void deleteJointData(int index){
        currentData = inputJointData.getValue();

        if (currentData != null){
            currentData.remove(index);
            inputJointData.setValue(currentData);
        }
    }

    // Automatic save every single value after edited
    public void saveJointData(int currentRowPosition, int columnIndex, String value){
        currentData = inputJointData.getValue();

        if (currentData != null){
            try{
                switch (columnIndex){

                    case 1:
                        currentData.get(currentRowPosition).setJ1(value);
                        System.out.println("Set J1 value equals: " + value);
                        break;
                    case 2:
                        currentData.get(currentRowPosition).setJ2(value);
                        break;
                    case 3:
                        currentData.get(currentRowPosition).setJ3(value);
                        break;
                    case 4:
                        currentData.get(currentRowPosition).setJ4(value);
                        break;
                    case 5:
                        currentData.get(currentRowPosition).setJ5(value);
                        break;
                    case 6:
                        currentData.get(currentRowPosition).setJ6(value);
                        break;
                }
            }
            catch (Exception e){
                System.out.println("Did not specify row to save");
            }
        }

        // updateJointData();
    }

    public void updateJointData() {
        currentData = inputJointData.getValue();

        if (currentData != null) {
            inputJointData.setValue(currentData);
        }
    }

    // Method to add new data after checking for duplicates
    public boolean addDataIfNotExists(JointDataTableRowModel newData) {

        // Check if this entry already exists. Check all fields of class (rather than memory allocation)
        for (JointDataTableRowModel row : currentData) {
            if (row.getJ1().equals(newData.getJ1()) &&
                    row.getJ2().equals(newData.getJ2()) &&
                        row.getJ3().equals(newData.getJ3()) &&
                            row.getJ4().equals(newData.getJ4()) &&
                                row.getJ5().equals(newData.getJ5()) &&
                                    row.getJ6().equals(newData.getJ6()))
                return true;
        }
        return false;
    }
}
