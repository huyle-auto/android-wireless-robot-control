package com.example.myrobotapp.Class.DataTable.ProgramFile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class ProgramDataSharedViewModel extends ViewModel {
    private final MutableLiveData<List<ProgramDataTableRowModel>> inputProgram = new MutableLiveData<>(new ArrayList<>());
    private List<ProgramDataTableRowModel> currentProgram;

    // Retrieve Rows Data
    public LiveData<List<ProgramDataTableRowModel>> getProgram() {
        return inputProgram;
    }

    // Append new model (row data) to old rows
    public void addProgram(ProgramDataTableRowModel program) {
        currentProgram = inputProgram.getValue();

        if (currentProgram != null && !addProgramIfNotExists(program)){
            currentProgram.add(program);
            inputProgram.setValue(currentProgram);
        }
    }


    // Currently this method would handle every adjustments made to Program files (ADD, DELETE, DUPLICATE)
    // No need to care about UI updating. Focus on managing internal storage's program files
    public void updateProgram(List<ProgramDataTableRowModel> programs) {
        // Clear list
        inputProgram.setValue(new ArrayList<>());

        // Add all to new list
        currentProgram = inputProgram.getValue();
        if (currentProgram != null){
            currentProgram.addAll(programs);
            inputProgram.setValue(currentProgram);
        }
    }

    // Delete single model using index
    public void deleteProgram(int index){
        currentProgram = inputProgram.getValue();

        if (currentProgram != null){
            currentProgram.remove(index);
            inputProgram.setValue(currentProgram);
        }
    }

    // Automatic save every single value after edited
    public void saveProgram(int currentRowPosition, int columnIndex, String value){
        currentProgram = inputProgram.getValue();

        if (currentProgram != null){
            try{
                switch (columnIndex){

                    case 1:
                        currentProgram.get(currentRowPosition).setProgramName(value);
                        break;
                    case 2:
                        currentProgram.get(currentRowPosition).setTag(value);
                        break;
                    case 3:
                        currentProgram.get(currentRowPosition).setTimeStamp(value);
                        break;
                    case 4:
                        currentProgram.get(currentRowPosition).setAttribute(value);
                        break;
                }
            }
            catch (Exception e){
                System.out.println("Did not specify row to save");
            }
        }
    }

    // Method to add new data after checking for duplicates
    public boolean addProgramIfNotExists(ProgramDataTableRowModel program) {

        // Check if this entry already exists. Check all fields of class (rather than memory allocation)
        for (ProgramDataTableRowModel row : currentProgram) {
            if (row.getProgramName().equals(program.getProgramName()) &&
                    row.getTag().equals(program.getTag()) &&
                    row.getTimeStamp().equals(program.getTimeStamp()) &&
                    row.getAttribute().equals(program.getAttribute()))
                return true;
        }
        return false;
    }
}
