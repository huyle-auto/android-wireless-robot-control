package com.example.myrobotapp.Class.DataTable.World;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class WorldDataSharedViewModel extends ViewModel {
    private final MutableLiveData<List<WorldDataTableRowModel>> inputWorldData = new MutableLiveData<>(new ArrayList<>());
    private List<WorldDataTableRowModel> currentData;

    // Retrieve Rows Data
    public LiveData<List<WorldDataTableRowModel>> getInputWorldData() {
        return inputWorldData;
    }

    // Clear all Data
    public void clearWorldData(){
        inputWorldData.setValue(new ArrayList<>());
    }

    // Update the whole list
    public void updateWorldData(List<WorldDataTableRowModel> worldData){
        clearWorldData();
        currentData = inputWorldData.getValue();
        if (currentData != null){
            currentData.addAll(worldData);
            inputWorldData.setValue(currentData);
        }
    }

    // Append new model (row data) to old rows
    public void addWorldData(WorldDataTableRowModel data) {
        currentData = inputWorldData.getValue();

        if (currentData != null && !addDataIfNotExists(data)){
            currentData.add(data);
            inputWorldData.setValue(currentData);
        }
    }

    // Delete single model using index
    public void deleteWorldData(int index){
        currentData = inputWorldData.getValue();

        if (currentData != null){
            currentData.remove(index);
            inputWorldData.setValue(currentData);
        }
    }

    // Automatic save every single value after edited
    public void saveWorldData(int currentRowPosition, int columnIndex, String value){
        currentData = inputWorldData.getValue();

        if (currentData != null){
            try{
                switch (columnIndex){

                    case 1:
                        currentData.get(currentRowPosition).setX(value);
                        break;
                    case 2:
                        currentData.get(currentRowPosition).setY(value);
                        break;
                    case 3:
                        currentData.get(currentRowPosition).setZ(value);
                        break;
                    case 4:
                        currentData.get(currentRowPosition).setRx(value);
                        break;
                    case 5:
                        currentData.get(currentRowPosition).setRy(value);
                        break;
                    case 6:
                        currentData.get(currentRowPosition).setRz(value);
                        break;
                }
            }
            catch (Exception e){
                System.out.println("Did not specify row to save");
            }
        }
    }

    // Method to add new data after checking for duplicates
    public boolean addDataIfNotExists(WorldDataTableRowModel newData) {

        // Check if this entry already exists. Check all fields of class (rather than memory allocation)
        for (WorldDataTableRowModel row : currentData) {
            if (row.getX().equals(newData.getX()) &&
                    row.getY().equals(newData.getY()) &&
                        row.getZ().equals(newData.getZ()) &&
                            row.getRx().equals(newData.getRx()) &&
                                row.getRy().equals(newData.getRy()) &&
                                    row.getRz().equals(newData.getRz()))
                return true;
        }
        return false;
    }
}
