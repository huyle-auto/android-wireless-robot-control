package com.example.myrobotapp.Fragments.Features;

import static com.example.myrobotapp.Class.FileManager.convertProgramFile;
import static com.example.myrobotapp.Class.FileManager.createTempTextFile;
import static com.example.myrobotapp.Class.FileManager.writeToDir;
import static com.example.myrobotapp.Fragments.Features.DataFragment.jointDataSharedViewModel;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.myrobotapp.Class.DataTable.Joint.JointDataTableRowModel;
import com.example.myrobotapp.Class.DataTable.World.WorldDataSharedViewModel;
import com.example.myrobotapp.Class.DataTable.World.WorldDataTableRowModel;
import com.example.myrobotapp.Fragments.Features.Program.ProgramEditFragment;
import com.example.myrobotapp.Fragments.Features.Program.ProgramListFragment;
import com.example.myrobotapp.R;

import java.io.File;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProgramFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProgramFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ProgramFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProgramFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProgramFragment newInstance(String param1, String param2) {
        ProgramFragment fragment = new ProgramFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    private WorldDataSharedViewModel worldDataSharedViewModel;
    private List<JointDataTableRowModel> currentJointPoint;
    private List<WorldDataTableRowModel> currentWorldPoint;
    private TextView lineIndexes;
    EditText programInputEditText, programNameEditText;
    Button saveToDeviceButton, sendToControllerButton, compileButton;

    // ---------------------- NEW LAYOUT ----------------------

    private Fragment programListFragment, programEditFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_program, container, false);

        // Initialize fragments only if they haven't been added before
        if (getChildFragmentManager().findFragmentByTag("child1") == null) {
            programListFragment = new ProgramListFragment();
            programEditFragment = new ProgramEditFragment();

            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.add(R.id.fragment_container, programListFragment, "child1");
            transaction.add(R.id.fragment_container, programEditFragment, "child2");
            transaction.hide(programEditFragment);  // Hide the second fragment initially
            transaction.commit();
        } else {
            // Fragments already added, retrieve them
            programListFragment = getChildFragmentManager().findFragmentByTag("child1");
            programEditFragment = getChildFragmentManager().findFragmentByTag("child2");
        }
//        // Populate View Model
//        worldDataSharedViewModel = new ViewModelProvider(requireActivity()).get(WorldDataSharedViewModel.class);
//
//        // Assign elements
//        sendToControllerButton = view.findViewById(R.id.sendToControllerButton);
//        sendToControllerButton.setOnClickListener(v -> sendToController());
//
//        saveToDeviceButton = view.findViewById(R.id.saveToDeviceButton);
//        saveToDeviceButton.setOnClickListener(v -> saveToDevice());
//
//        compileButton = view.findViewById(R.id.compileButton);
//        compileButton.setOnClickListener(v -> compile());
//
//        programInputEditText = view.findViewById(R.id.programInput);
//        programNameEditText = view.findViewById(R.id.programName);
//
//        // Print out 1000 lines of index of program
//        lineIndexes = view.findViewById(R.id.lineIndexes);
//        setLineIndexes();

        return view;
    }

    public void showProgramList() {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        //transaction.replace(R.id.fragment_container, new ProgramListFragment());
        //transaction.addToBackStack(null);
        transaction.show(programListFragment);
        transaction.hide(programEditFragment);
        transaction.commit();
    }

    public void showProgramEdit() {
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
//        transaction.replace(R.id.fragment_container, new ProgramEditFragment());
//        transaction.addToBackStack(null);
        transaction.hide(programListFragment);
        transaction.show(programEditFragment);
        transaction.commit();
    }

    private void setLineIndexes(){
        StringBuilder numbers = new StringBuilder();
        new Thread(()->{
            for (int i = 0; i <= 1000; i++) {
                numbers.append(String.format(Locale.US, "%04d", i)).append("\n");
            }
            getActivity().runOnUiThread(() -> lineIndexes.setText(numbers.toString()));
        }).start();

    }

    private void saveToDevice(){
        String programName = programNameEditText.getText().toString();
        String programInput = programInputEditText.getText().toString();

        // 1. Construct .txt program file
        File txtFile = createTempTextFile(this.requireContext(), programInput, "Anyname");
        writeToDir(this.requireContext(), "Program/" + programName, programName + ".txt", txtFile);

        try{
            // 2. Construct .pnt file
            currentJointPoint = jointDataSharedViewModel.getInputJointData().getValue();
            String pntFileData="";

            // Append Joint Point list
            pntFileData += "#JOINT_LIST" + "\n";
            if (currentJointPoint != null){
                int index = 0;
                for (JointDataTableRowModel model : currentJointPoint){
                    if (index < currentJointPoint.size()){
                        pntFileData += "#" + "P" + index + ","
                                                + model.getJ1() + ","
                                                    + model.getJ2() + ","
                                                        + model.getJ3() + ","
                                                            + model.getJ4() + ","
                                                                + model.getJ5() + ","
                                                                    + model.getJ6()
                                                                        + "\n";
                        index++;
                    }
                    else {
                        break;
                    }
                }
            }

            // Append World Point list
            currentWorldPoint = worldDataSharedViewModel.getInputWorldData().getValue();
            pntFileData += "#WORLD_LIST" + "\n";

            if (currentWorldPoint != null){
                int index = 0;
                for (WorldDataTableRowModel model : currentWorldPoint){
                    if (index < currentWorldPoint.size()){
                        pntFileData += "P" + index + ","
                                                        + model.getX() + ","
                                                            + model.getY() + ","
                                                                + model.getZ() + ","
                                                                    + model.getRx() + ","
                                                                        + model.getRy() + ","
                                                                            + model.getRz()
                                                                                + "\n";
                        index++;
                    }
                    else {
                        break;
                    }
                }
            }

            // Save .pnt file
            File pntFile = createTempTextFile(this.requireContext(), pntFileData, "Anyname");
            writeToDir(this.requireContext(), "Program/" + programName, programName + ".pnt", pntFile);

            // 3. Construct controller's file
            StringBuilder pointBuilder = new StringBuilder();

            // Declare Joint Point
            for (int i=0; i < currentJointPoint.size(); i++){
                pointBuilder.append("POINT #P[" + i + "] = [" +
                                                currentJointPoint.get(i).getJ1() + ", "
                                                    + currentJointPoint.get(i).getJ2() + ", "
                                                        + currentJointPoint.get(i).getJ3() + ", "
                                                            + currentJointPoint.get(i).getJ4() + ", "
                                                                + currentJointPoint.get(i).getJ5() + ", "
                                                                    + currentJointPoint.get(i).getJ6() + "]" + "\n");
            }
            // Declare World Point
            for (int i=0; i < currentWorldPoint.size(); i++){
                pointBuilder.append("POINT P[" + i + "] = [" +
                                                currentWorldPoint.get(i).getX() + ", "
                                                    + currentWorldPoint.get(i).getY() + ", "
                                                        + currentWorldPoint.get(i).getZ() + ", "
                                                            + currentWorldPoint.get(i).getRx() + ", "
                                                                + currentWorldPoint.get(i).getRy() + ", "
                                                                    + currentWorldPoint.get(i).getRz() + "]" + "\n");
            }
            // Append .txt program file to form the controller's file


            String ctrlContent = pointBuilder.toString();

            // Save controller's file
            File ctrlFile = createTempTextFile(this.requireContext(), ctrlContent, "Anyname");
            writeToDir(this.requireContext(), "Program/" + programName, programName + "Ctrl" + ".txt", ctrlFile);
        }
        catch (NullPointerException e){
            System.out.println("Warning: Empty point list");
        }
    }

    private void sendToController(){
        // Construct file from .txt file and .pnt file


    }

    private void compile(){

    }
}