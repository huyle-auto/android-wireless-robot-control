package com.example.myrobotapp.Fragments.Features.Program;

import static com.example.myrobotapp.Class.CommandList.MotionCommandsFragment.addedCommandText;
import static com.example.myrobotapp.Class.DataTable.ProgramFile.ProgramDataTableRowAdapter.selectedProgramName;
import static com.example.myrobotapp.Class.DataTable.ProgramInput.ProgramInputRowAdapter.posProgramLine;
import static com.example.myrobotapp.Class.FileManager.createTempTextFile;
import static com.example.myrobotapp.Class.FileManager.readFile;
import static com.example.myrobotapp.Class.FileManager.writeToDir;
import static com.example.myrobotapp.Class.ProtocolReceive.compileResult;
import static com.example.myrobotapp.Class.ProtocolReceive.programRunningStatus;
import static com.example.myrobotapp.Class.ProtocolReceive.sendProgramEndPartACK;
import static com.example.myrobotapp.Class.ProtocolReceive.sendProgramPartACK;
import static com.example.myrobotapp.Class.ProtocolSend.readInternalFile;
import static com.example.myrobotapp.Fragments.Features.DataFragment.jointDataSharedViewModel;
import static com.example.myrobotapp.Fragments.Features.HistoryFragment.recordOperation;
import static com.example.myrobotapp.Fragments.Features.Settings.ConnectionFragment.activeConnectionKey;
import static com.example.myrobotapp.Fragments.Features.Settings.ConnectionFragment.globalSelectedBTDevice;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myrobotapp.Class.CommandList.ConfigurationCommandsFragment;
import com.example.myrobotapp.Class.CommandList.ControlCommandsFragment;
import com.example.myrobotapp.Class.CommandList.ConveyorCommandsFragment;
import com.example.myrobotapp.Class.CommandList.IOCommandsFragment;
import com.example.myrobotapp.Class.CommandList.MathCommandsFragment;
import com.example.myrobotapp.Class.CommandList.MotionCommandsFragment;
import com.example.myrobotapp.Class.Communication.PriorityCommunication;
import com.example.myrobotapp.Class.DataTable.Joint.JointDataTableRowModel;
import com.example.myrobotapp.Class.DataTable.ProgramInput.ProgramInputCodeLineModel;
import com.example.myrobotapp.Class.DataTable.ProgramInput.ProgramInputRowAdapter;
import com.example.myrobotapp.Class.DataTable.World.WorldDataSharedViewModel;
import com.example.myrobotapp.Class.DataTable.World.WorldDataTableRowModel;
import com.example.myrobotapp.Class.Dialog.ErrorDialog;
import com.example.myrobotapp.Class.Dialog.InfoDialog;
import com.example.myrobotapp.Class.GlobalData;
import com.example.myrobotapp.Class.TCP.TCPClient;
import com.example.myrobotapp.Class.UserInput.KeyboardPopup;
import com.example.myrobotapp.Class.UserInput.NumpadPopup;
import com.example.myrobotapp.Fragments.Features.ProgramFragment;
import com.example.myrobotapp.Fragments.Features.Settings.ConnectionFragment;
import com.example.myrobotapp.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import okio.BufferedSource;
import okio.Okio;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProgramEditFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProgramEditFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ProgramEditFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProgramEditFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProgramEditFragment newInstance(String param1, String param2) {
        ProgramEditFragment fragment = new ProgramEditFragment();
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

    public static final String TAG = "ProgramEditFragment";

    private ProgramInputRowAdapter programInputRowAdapter;
    RecyclerView programInputRecyclerView;
    FrameLayout programInputFrameLayout;

    private WorldDataSharedViewModel worldDataSharedViewModel;
    private List<JointDataTableRowModel> currentJointPoint;
    private List<WorldDataTableRowModel> currentWorldPoint;
    private TextView lineIndexes;
    EditText programNameEditText;
    TextView programStatusTextView, programCompileStatusTextView, programRunningStatusTextView;
    Button saveToDeviceButton, sendToControllerButton, compileButton, backButton, runProgramButton, stopProgramButton;
    FrameLayout commandListFrameLayout;
    Button firstCommandListButton, secondCommandListButton, thirdCommandListButton;
    Button switchForwardCommandListButton, switchBackCommandListButton;
    Button insertButton, deleteButton, upLineButton, downLineButton;
    EditText programInputBuffer;
    String selectedCommandListButton = "MOTION";

    private String lastAddedCommand = "";
    private Handler handler;

    SwitchCompat newFormatToggle;
    boolean leaveOneEmptyLineAfterPointDeclaration;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_program_edit, container, false);

        // Populate View Model
        worldDataSharedViewModel = new ViewModelProvider(requireActivity()).get(WorldDataSharedViewModel.class);

        // Assign elements
        sendToControllerButton = view.findViewById(R.id.sendToControllerButton);
        sendToControllerButton.setOnClickListener(v -> sendToController());

        saveToDeviceButton = view.findViewById(R.id.saveToDeviceButton);
        saveToDeviceButton.setOnClickListener(v -> saveToDevice());

        compileButton = view.findViewById(R.id.compileButton);
        compileButton.setOnClickListener(v -> compile());

        backButton = view.findViewById(R.id.backToProgramListButton);
        backButton.setOnClickListener(v -> backToProgramList());

        runProgramButton = view.findViewById(R.id.runProgramButton);
        runProgramButton.setOnClickListener(v -> runProgram());
        stopProgramButton = view.findViewById(R.id.stopProgramButton);
        stopProgramButton.setOnClickListener(v -> stopProgram());

        // PROGRAM INPUT Recycler View
        List<ProgramInputCodeLineModel> initialLines = new ArrayList<>();

        programInputRecyclerView = view.findViewById(R.id.programInputRecyclerView);
        programInputRowAdapter = new ProgramInputRowAdapter(this.requireActivity(), initialLines);

        programInputRecyclerView.setAdapter(programInputRowAdapter);
        programInputRecyclerView.setLayoutManager(new LinearLayoutManager(this.requireActivity()));


        // programInputEditText = view.findViewById(R.id.programInput);
        programNameEditText = view.findViewById(R.id.programName);
        programNameEditText.setOnClickListener(v -> {
            KeyboardPopup keyboardPopup = new KeyboardPopup(this.requireContext(), programNameEditText, 3);
            keyboardPopup.showKeyboardPopup(this.requireActivity());
        });
        programStatusTextView = view.findViewById(R.id.programStatus);
        programCompileStatusTextView = view.findViewById(R.id.programCompileStatusTextView);
        programRunningStatusTextView = view.findViewById(R.id.programRunningStatusTextView);

        // Function helper (COMMAND LIST)
        commandListFrameLayout = view.findViewById(R.id.commandListFrameLayout);

        firstCommandListButton = view.findViewById(R.id.firstCommandListButton);
        firstCommandListButton.setOnClickListener(v -> {
            showCommandList(firstCommandListButton.getText().toString());
            selectedCommandListButton = firstCommandListButton.getText().toString();
        });

        secondCommandListButton = view.findViewById(R.id.secondCommandListButton);
        secondCommandListButton.setOnClickListener(v -> {
            showCommandList(secondCommandListButton.getText().toString());
            selectedCommandListButton = secondCommandListButton.getText().toString();
        });

        thirdCommandListButton = view.findViewById(R.id.thirdCommandListButton);
        thirdCommandListButton.setOnClickListener(v -> {
            showCommandList(thirdCommandListButton.getText().toString());
            selectedCommandListButton = secondCommandListButton.getText().toString();
        });

        switchForwardCommandListButton = view.findViewById(R.id.switchForwardCommandListButton);
        switchBackCommandListButton = view.findViewById(R.id.switchBackCommandListButton);

        switchForwardCommandListButton.setOnClickListener(v -> {
            // Change buttons text
            String firstText = "MATH";
            String secondText = "CONFIG";
            String thirdText = "CONVEYOR";
            firstCommandListButton.setText(firstText);
            secondCommandListButton.setText(secondText);
            thirdCommandListButton.setText(thirdText);
            switchForwardCommandListButton.setVisibility(View.INVISIBLE);
            switchBackCommandListButton.setVisibility(View.VISIBLE);

            // Highlight button
            firstCommandListButton.setBackgroundColor(Color.parseColor("#FFFFFF"));
            secondCommandListButton.setBackgroundColor(Color.parseColor("#FFFFFF"));
            thirdCommandListButton.setBackgroundColor(Color.parseColor("#FFFFFF"));

            if (selectedCommandListButton.equals(firstCommandListButton.getText().toString())){
                firstCommandListButton.setBackgroundColor(Color.parseColor("#1496BB"));
            }
            else if (selectedCommandListButton.equals(secondCommandListButton.getText().toString())){
                secondCommandListButton.setBackgroundColor(Color.parseColor("#1496BB"));
            }
            else if (selectedCommandListButton.equals(thirdCommandListButton.getText().toString())){
                thirdCommandListButton.setBackgroundColor(Color.parseColor("#1496BB"));
            }
        });

        switchBackCommandListButton.setOnClickListener(v -> {
            // Change buttons text
            String firstText = "MOTION";
            String secondText = "I/O";
            String thirdText = "CONTROL";
            firstCommandListButton.setText(firstText);
            secondCommandListButton.setText(secondText);
            thirdCommandListButton.setText(thirdText);
            switchBackCommandListButton.setVisibility(View.INVISIBLE);
            switchForwardCommandListButton.setVisibility(View.VISIBLE);

            // Highlight button
            firstCommandListButton.setBackgroundColor(Color.parseColor("#FFFFFF"));
            secondCommandListButton.setBackgroundColor(Color.parseColor("#FFFFFF"));
            thirdCommandListButton.setBackgroundColor(Color.parseColor("#FFFFFF"));

            if (selectedCommandListButton.equals(firstCommandListButton.getText().toString())){
                firstCommandListButton.setBackgroundColor(Color.parseColor("#1496BB"));
            }
            else if (selectedCommandListButton.equals(secondCommandListButton.getText().toString())){
                secondCommandListButton.setBackgroundColor(Color.parseColor("#1496BB"));
            }
            else if (selectedCommandListButton.equals(thirdCommandListButton.getText().toString())){
                thirdCommandListButton.setBackgroundColor(Color.parseColor("#1496BB"));
            }
        });
        
        // PROGRAM EDIT
        programInputFrameLayout = view.findViewById(R.id.programInputFrameLayout);
        programInputFrameLayout.setOnClickListener(v -> removeFocus());
        insertButton = view.findViewById(R.id.insertLineButton);
        insertButton.setOnClickListener(v -> insertCodeLine());
        deleteButton = view.findViewById(R.id.deleteLineButton);
        deleteButton.setOnClickListener(v -> deleteCodeLine());
        programInputBuffer = view.findViewById(R.id.programInputBuffer);
        programInputBuffer.setOnClickListener(v -> {
            KeyboardPopup keyboardPopup = new KeyboardPopup(this.requireContext(), programInputBuffer, 3);
            keyboardPopup.showKeyboardPopup(this.requireActivity());
        });

        upLineButton = view.findViewById(R.id.upLineButton);
        upLineButton.setOnClickListener(v -> {
            int currentLineIndex = Integer.parseInt(posProgramLine);

            // If at first code line then goes back to last code line (also scroll to last line)
            if (currentLineIndex == 0){
                posProgramLine = String.valueOf(programInputRowAdapter.getItemCount() - 1);
                programInputRowAdapter.notifyDataSetChanged();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        int position = Integer.parseInt(posProgramLine);
                        programInputRecyclerView.scrollToPosition(position);
                    }
                }, 200);
            }
            else {
                currentLineIndex--;
                posProgramLine = String.valueOf(currentLineIndex);
                programInputRowAdapter.notifyDataSetChanged();
            }
        });

        downLineButton = view.findViewById(R.id.downLineButton);
        downLineButton.setOnClickListener(v -> {
            int currentLineIndex = Integer.parseInt(posProgramLine);

            // If at last code line then goes back to first line (also scroll to first line)
            if (Integer.parseInt(posProgramLine) == programInputRowAdapter.getItemCount() - 1){
                posProgramLine = String.valueOf(0);
                programInputRowAdapter.notifyDataSetChanged();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        int position = Integer.parseInt(posProgramLine);
                        programInputRecyclerView.scrollToPosition(position);
                    }
                }, 200);
            }
            else {
                currentLineIndex++;
                posProgramLine = String.valueOf(currentLineIndex);
                programInputRowAdapter.notifyDataSetChanged();
            }
        });

        // Update added command to buffer
        handler = new Handler();

        // Cheat code
        newFormatToggle = view.findViewById(R.id.newFormatToggle);
        newFormatToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                leaveOneEmptyLineAfterPointDeclaration = true;
            } else {
                leaveOneEmptyLineAfterPointDeclaration = false;
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        addCommand();
    }

    // AUTO-SAVE
    @Override
    public void onPause() {
        super.onPause();
        // Call your save function here
        saveToDevice();
    }

    @Override
    public void onStop() {
        super.onStop();
        // Call your save function here
        saveToDevice();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Call your save function here
        saveToDevice();
    }

    // Initialize whenever this fragment show up
    @Override
    public void onHiddenChanged(boolean hidden){
        super.onHiddenChanged(hidden);
        if(!hidden){
            init();
        }
    }

    // Update all program's properties here
    private void init(){
        // 1. Program Name
        programNameEditText.setText(selectedProgramName);

        // 2. Program Input
        try{
            File programFile = new File(requireContext().getFilesDir(), "Program/" + selectedProgramName + "/" + selectedProgramName + ".txt");
            String programFileContent = readTextFile(programFile);
            programInputRowAdapter.setCodeContent(programFileContent);
            // programInputEditText.setText(programFileContent);
        }
        catch (IOException e){
            Toast.makeText(requireContext(), "Error loading program code: " + e.getMessage(), Toast.LENGTH_LONG).show();
            System.out.println("Error loading program code: " + e.getMessage());
        }

        // 3. Joint/World Point List
        try{
            File pointFile = new File(requireContext().getFilesDir(), "Program/" + selectedProgramName + "/" + selectedProgramName + ".pnt");
            String pointFileContent = readTextFile(pointFile);

            // Update Joint Point ViewModel
            jointDataSharedViewModel.clearJointData();
            jointDataSharedViewModel.updateJointData(decodeJointPointFileContent(pointFileContent));

            // Update World Point ViewModel
            worldDataSharedViewModel.clearWorldData();
            worldDataSharedViewModel.updateWorldData(decodeWorldPointFileContent(pointFileContent));
        }
        catch (IOException e){
            Toast.makeText(requireContext(), "Empty point data", Toast.LENGTH_SHORT).show();
            System.out.println("Error loading point data: " + e.getMessage());
        }

        // 4. Command List (Motion commands initially)
        showCommandList(selectedCommandListButton);

        // 5. Miscellaneous (Update program status SAVED/UNSAVED)
        String initialStatus = "Status: ";
        programStatusTextView.setText(initialStatus);
    }

    private String readTextFile (File file) throws IOException{
        try (BufferedSource source = Okio.buffer(Okio.source(file))) {
            return source.readUtf8(); // Read entire file content as a UTF-8 string
        }
    }

    private List<JointDataTableRowModel> decodeJointPointFileContent(String jointPointFileData) throws IOException{
        // Process file content
        String[] lines = jointPointFileData.split("\n");
        // Create ArrayLists to store joint points and world points
        List<String[]> jointPoints = new ArrayList<>();

        for (String line : lines){
            if (!line.contains("JOINT_LIST") && !line.contains("WORLD_LIST")){
                // Split the line by commas
                String[] parts = line.split(",");

                // Check if there are exactly 7 numbers
                if (parts.length == 7){
                    String[] values = new String[7];
                    for (int i=0; i < 7; i++){
                        if (i == 0){
                            // First component is #P<number> so take only the integer
                            values[i] = parts[i].substring(2);
                        }
                        else {
                            values[i] = parts[i];
                        }
                    }

                    // Check if the line starts with '#P' for joint points or 'P' for world points
                    if (line.startsWith("#P")) {
                        jointPoints.add(values);
                    }
                }
            }
        }

        // Assign to list of Joint Point to update later
        List<JointDataTableRowModel> jointPointModels = new ArrayList<>();
        for (int i=0; i < jointPoints.size(); i++){
            jointPointModels.add(new JointDataTableRowModel("","","","","","",""));
            jointPointModels.get(i).setNo(Integer.toString(i));
            jointPointModels.get(i).setJ1(jointPoints.get(i)[1]);
            jointPointModels.get(i).setJ2(jointPoints.get(i)[2]);
            jointPointModels.get(i).setJ3(jointPoints.get(i)[3]);
            jointPointModels.get(i).setJ4(jointPoints.get(i)[4]);
            jointPointModels.get(i).setJ5(jointPoints.get(i)[5]);
            jointPointModels.get(i).setJ6(jointPoints.get(i)[6]);
        }

        return jointPointModels;

        // DEBUG
//        for (String[] point : jointPoints) {
//            System.out.println(java.util.Arrays.toString(point));
//        }

//        System.out.println("Joint Model Assignment result: ");
//        for (JointDataTableRowModel jointPoint : jointPointModels){
//            System.out.println(jointPoint.getNo()
//                    + ", " + jointPoint.getJ1()
//                    + ", " + jointPoint.getJ2()
//                    + ", " + jointPoint.getJ3()
//                    + ", " + jointPoint.getJ4()
//                    + ", " + jointPoint.getJ5()
//                    + ", " + jointPoint.getJ6());
//        }
    }

    private List<WorldDataTableRowModel> decodeWorldPointFileContent(String worldPointFileData) throws IOException{
        // Process file content
        String[] lines = worldPointFileData.split("\n");
        // Create ArrayLists to store joint points and world points
        List<String[]> worldPoints = new ArrayList<>();

        for (String line : lines){
            if (!line.contains("JOINT_LIST") && !line.contains("WORLD_LIST")){
                // Split the line by commas
                String[] parts = line.split(",");

                // Check if there are exactly 7 numbers
                if (parts.length == 7){
                    String[] values = new String[7];
                    for (int i=0; i < 7; i++){
                        if (i == 0){
                            // First component is #P<number> so take only the integer
                            values[i] = parts[i].substring(1);
                        }
                        else {
                            values[i] = parts[i];
                        }
                    }

                    // Check if the line starts with '#P' for joint points or 'P' for world points
                    if (line.startsWith("P")) {
                        worldPoints.add(values);
                    }
                }
            }
        }

        // Assign to list of Joint Point to update later
        List<WorldDataTableRowModel> worldPointModels = new ArrayList<>();
        for (int i=0; i < worldPoints.size(); i++){
            worldPointModels.add(new WorldDataTableRowModel("","","","","","",""));
            worldPointModels.get(i).setNo(Integer.toString(i));
            worldPointModels.get(i).setX(worldPoints.get(i)[1]);
            worldPointModels.get(i).setY(worldPoints.get(i)[2]);
            worldPointModels.get(i).setZ(worldPoints.get(i)[3]);
            worldPointModels.get(i).setRx(worldPoints.get(i)[4]);
            worldPointModels.get(i).setRy(worldPoints.get(i)[5]);
            worldPointModels.get(i).setRz(worldPoints.get(i)[6]);
        }

        return worldPointModels;
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

    private void showCommandList(String buttonText){
        Fragment targetFragment = null;
        switch(buttonText){
            case "MOTION":
                targetFragment = new MotionCommandsFragment();
                break;
            case "I/O":
                targetFragment = new IOCommandsFragment();
                break;
            case "CONTROL":
                targetFragment = new ControlCommandsFragment();
                break;
            case "MATH":
                targetFragment = new MathCommandsFragment();
                break;
            case "CONFIG":
                targetFragment = new ConfigurationCommandsFragment();
                break;
            case "CONVEYOR":
                targetFragment = new ConveyorCommandsFragment();
                break;
            default:
                targetFragment = new MotionCommandsFragment();
                break;
        }

        firstCommandListButton.setBackgroundColor(Color.parseColor("#FFFFFF"));
        secondCommandListButton.setBackgroundColor(Color.parseColor("#FFFFFF"));
        thirdCommandListButton.setBackgroundColor(Color.parseColor("#FFFFFF"));

        if (buttonText.equals(firstCommandListButton.getText().toString())){
            firstCommandListButton.setBackgroundColor(Color.parseColor("#1496BB"));
        }
        else if (buttonText.equals(secondCommandListButton.getText().toString())){
            secondCommandListButton.setBackgroundColor(Color.parseColor("#1496BB"));
        }
        else if (buttonText.equals(thirdCommandListButton.getText().toString())){
            thirdCommandListButton.setBackgroundColor(Color.parseColor("#1496BB"));
        }

        // Switch to the target fragment
        if (targetFragment != null) {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.commandListFrameLayout, targetFragment)
                    .addToBackStack(null) // Add to back stack if you want to allow back navigation
                    .commit();
        }
    }

    private void saveToDevice(){
        String programName = programNameEditText.getText().toString();
        // String programInput = programInputEditText.getText().toString();
        String programInput = programInputRowAdapter.getCodeContent();

        // 1. Construct .txt program file
        File txtFile = createTempTextFile(this.requireContext(), programInput + "\n" + "\n" + "\n", "Anyname");
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
                pointBuilder.append("POINT #P[")
                        .append(i).append("] = [")
                        .append(currentJointPoint.get(i).getJ1()).append(", ")
                        .append(currentJointPoint.get(i).getJ2()).append(", ")
                        .append(currentJointPoint.get(i).getJ3()).append(", ")
                        .append(currentJointPoint.get(i).getJ4()).append(", ")
                        .append(currentJointPoint.get(i).getJ5()).append(", ")
                        .append(currentJointPoint.get(i).getJ6()).append("]\n");
            }

            // Declare World Point
            for (int i=0; i < currentWorldPoint.size(); i++){
                pointBuilder.append("POINT P[")
                        .append(i).append("] = [")
                        .append(currentWorldPoint.get(i).getX()).append(", ")
                        .append(currentWorldPoint.get(i).getY()).append(", ")
                        .append(currentWorldPoint.get(i).getZ()).append(", ")
                        .append(currentWorldPoint.get(i).getRx()).append(", ")
                        .append(currentWorldPoint.get(i).getRy()).append(", ")
                        .append(currentWorldPoint.get(i).getRz()).append("]\n");
            }

            // Append .txt program file to form the controller's file
            String pntDeclaration = pointBuilder.toString();
            File programFile = new File(requireContext().getFilesDir(), "Program/" + programName + "/" + programName + ".txt");
            String ctrlContent = constructCtrlFile(programFile, pntDeclaration);

            // Save controller's file
            File ctrlFile = createTempTextFile(this.requireContext(), refineCtrlFile(ctrlContent, leaveOneEmptyLineAfterPointDeclaration), "Anyname");
            writeToDir(this.requireContext(), "Program/" + programName, programName + "_Ctrl" + ".txt", ctrlFile);

            // Update success status
            String status= "Status: Saved to device";
            programStatusTextView.setText(status);
        }
        catch (NullPointerException e){
            // Update success status
            String status = "Status: Error saving program";
            programStatusTextView.setText(status);
            System.out.println("Warning: Empty point list");
        }
    }

    private String constructCtrlFile(File programFile, String translatedPoint) {
        StringBuilder result = new StringBuilder();

        try {
            String programText = readFile(programFile); // Exception sensitive
            String[] programLines = programText.split("\n");
            boolean inserted = false;

            for (String programLine : programLines) {
                result.append(programLine).append("\n");

                // Insert point declaration after "MAIN" line
                if (!inserted && programLine.contains("MAIN")) {
                    result.append(translatedPoint).append("\n");
                    inserted = true;
                }
            }
        } catch (IOException e) {
            Toast.makeText(requireContext(),"Error read for construct: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        return result.toString();
    }

    private void sendToController(){
        ErrorDialog errorDialog = new ErrorDialog(this.getContext(), "");
        InfoDialog infoDialog = new InfoDialog(this.requireContext(), "");

        // Define how many times to send program
        int partQuantity = 0;
        try {
            String currentProgramName = programNameEditText.getText().toString();
            String ctrlFilePath = "Program/" + currentProgramName + "/" + currentProgramName + "_Ctrl.txt";
            File ctrlFile = new File(this.requireContext().getFilesDir(), ctrlFilePath);

            byte[] ctrlFileBytes = readInternalFile(this.requireContext(), ctrlFilePath);
            // byte[] ctrlFileBytes = Files.readAllBytes(ctrlFile.toPath());
            System.out.println("Program has: " + ctrlFileBytes.length + " bytes");

            // Quantity of send program part call
            partQuantity = (ctrlFileBytes.length / 200) + 1;
            System.out.println("Part quantity is: " + partQuantity);
        } catch (Exception e) {
            Toast.makeText(this.requireContext(), "Did not build: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        // Send
        ConcurrentHashMap<String, TCPClient> activeConnections = GlobalData.getInstance().getActiveConnections();
        TCPClient tcpClient = activeConnections.get(activeConnectionKey);

        if (tcpClient == null){
            Toast.makeText(this.requireContext(), "Server not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Directly send end program part if .txt file <= 200 bytes
        String miscellaneous = "";
        if (partQuantity == 1) {
            tcpClient.sendProgramEndPart(0, programNameEditText.getText().toString(), this.requireContext(), miscellaneous);
            handler.postDelayed(() -> {
                Log.i(TAG, "sendProgramEndPartACK is: " + sendProgramEndPartACK);
                if (sendProgramEndPartACK == 0) {
                    Toast.makeText(this.requireContext(), "Sent program to controller", Toast.LENGTH_SHORT).show();
                    infoDialog.setInfoHint("Sent program to controller");
                    infoDialog.showNumpadPopup(this.requireActivity());
                }
                else {
                    Toast.makeText(this.requireContext(), "Error ACK sending program to controller", Toast.LENGTH_SHORT).show();;
                    errorDialog.setInfoHint("Error ACK sending program to controller");
                    errorDialog.showNumpadPopup(this.requireActivity());
                }
            }, 1000);
            return;
        }

        // Send multiple parts for .txt file > 200 bytes
        // Send normal part
        AtomicBoolean shouldStop = new AtomicBoolean(false);
        for (int i = 0; i <= partQuantity - 2; i++) {
            if (shouldStop.get()) {
                return;
            }

            tcpClient.sendProgramPart(i, programNameEditText.getText().toString(), this.requireContext());
            int currentPartIndex = i;
            handler.postDelayed(() -> {
                if (sendProgramPartACK != currentPartIndex) {
                    shouldStop.set(true);   // Flag stop sending if ACK signal is wrong
                    Toast.makeText(this.requireContext(), "Error ACK sending program to controller", Toast.LENGTH_SHORT).show();
                    errorDialog.setInfoHint("Error ACK sending program to controller");
                    errorDialog.showNumpadPopup(this.requireActivity());
                }

            }, 100);
        }

        if (shouldStop.get()) {
            Toast.makeText(this.requireContext(), "Sending program aborted: Wrong ACK signal", Toast.LENGTH_SHORT).show();
            return;
        }

        // Send ended part
        tcpClient.sendProgramEndPart(partQuantity - 1, programNameEditText.getText().toString(), this.requireContext(), miscellaneous);
        int tempPartQuantity = partQuantity;
        handler.postDelayed(() -> {
            if (sendProgramEndPartACK == tempPartQuantity - 1) {
                Toast.makeText(this.requireContext(), "Sent program to controller", Toast.LENGTH_SHORT).show();
                infoDialog.setInfoHint("Sent program to controller");
                infoDialog.showNumpadPopup(this.requireActivity());
            }
            else {
                Toast.makeText(this.requireContext(), "Error ACK sending program to controller", Toast.LENGTH_SHORT).show();
                errorDialog.setInfoHint("Error ACK sending program to controller");
                errorDialog.showNumpadPopup(this.requireActivity());
            }

        }, 1000);
    }   // Haven't added priority

    private void compile(){
        ConcurrentHashMap<String, TCPClient> activeConnections = GlobalData.getInstance().getActiveConnections();
        TCPClient tcpClient = activeConnections.get(activeConnectionKey);

        if (tcpClient == null && globalSelectedBTDevice == null) {
            Toast.makeText(this.requireContext(), "Server not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // TCP (Prioritized)
        PriorityCommunication.ChannelType channelType = PriorityCommunication.checkAvailableChannel();
        if (channelType == PriorityCommunication.ChannelType.TCP) {
            // Run program
            tcpClient.compileProgram();
            Toast.makeText(this.requireContext(), "Requested to COMPILE program", Toast.LENGTH_SHORT).show();
            recordOperation("request COMPILE PROGRAM");

            // Wait for 1 second before request result
            handler.postDelayed(() -> tcpClient.checkCompileResult(), 1000);

            // Wait for 200 milliseconds then update compile result
            handler.postDelayed(() -> programCompileStatusTextView.setText(compileResult), 200);
            return;
        }

        // Bluetooth
        if (channelType == PriorityCommunication.ChannelType.Bluetooth) {
            ConnectionFragment.bluetoothHelper.compileProgram(globalSelectedBTDevice);
            Toast.makeText(this.requireContext(), "Requested to COMPILE program", Toast.LENGTH_SHORT).show();
            recordOperation("request COMPILE PROGRAM");

            // Wait for 1 second before request result
            handler.postDelayed(() -> ConnectionFragment.bluetoothHelper.checkCompileResult(globalSelectedBTDevice), 1000);

            // Wait for 200 milliseconds then update compile result
            handler.postDelayed(() -> programCompileStatusTextView.setText(compileResult), 200);
            return;
        }

        showToast("No server available", false);
    }   // Added priority

    private void runProgram(){
        ConcurrentHashMap<String, TCPClient> activeConnections = GlobalData.getInstance().getActiveConnections();
        TCPClient tcpClient = activeConnections.get(activeConnectionKey);

        if (tcpClient == null && globalSelectedBTDevice == null) {
            Toast.makeText(this.requireContext(), "Server not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // TCP (Prioritized)
        PriorityCommunication.ChannelType channelType = PriorityCommunication.checkAvailableChannel();
        if (channelType == PriorityCommunication.ChannelType.TCP) {
            Log.i(TAG, "TCP available! Sending...");
            // Run program
            tcpClient.runRobotProgram();
            handler.postDelayed(() -> programRunningStatusTextView.setText(programRunningStatus), 1500);
            recordOperation("request RUN PROGRAM");

            InfoDialog infoDialog = new InfoDialog(this.requireContext(), "");
            infoDialog.setInfoHint("requested START PROGRAM");
            infoDialog.showNumpadPopup(this.requireActivity());
            return;
        }

        // Bluetooth
        if (channelType == PriorityCommunication.ChannelType.Bluetooth) {
            Log.i(TAG, "Bluetooth available! Sending...");
            ConnectionFragment.bluetoothHelper.runRobotProgram(globalSelectedBTDevice);
            handler.postDelayed(() -> programRunningStatusTextView.setText(programRunningStatus), 1500);
            recordOperation("request RUN PROGRAM");

            InfoDialog infoDialog = new InfoDialog(this.requireContext(), "");
            infoDialog.setInfoHint("requested START PROGRAM");
            infoDialog.showNumpadPopup(this.requireActivity());
            return;
        }

        showToast("No server available", false);
    }   // Added priority

    private void stopProgram(){
        ConcurrentHashMap<String, TCPClient> activeConnections = GlobalData.getInstance().getActiveConnections();
        TCPClient tcpClient = activeConnections.get(activeConnectionKey);

        if (tcpClient == null && globalSelectedBTDevice == null) {
            Toast.makeText(this.requireContext(), "Server not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // TCP (Prioritized)
        PriorityCommunication.ChannelType channelType = PriorityCommunication.checkAvailableChannel();
        if (channelType == PriorityCommunication.ChannelType.TCP) {
            // Run program
            tcpClient.stopRobotProgram();
            handler.postDelayed(() -> programRunningStatusTextView.setText(programRunningStatus), 1500);
            recordOperation("request STOP PROGRAM");
            return;
        }

        // Bluetooth
        if (channelType == PriorityCommunication.ChannelType.Bluetooth) {
            ConnectionFragment.bluetoothHelper.stopRobotProgram(globalSelectedBTDevice);
            handler.postDelayed(() -> programRunningStatusTextView.setText(programRunningStatus), 1500);
            recordOperation("request STOP PROGRAM");
            return;
        }

        showToast("No server available", false);
    }   // Added priority

    private void insertCodeLine(){
        // Add content in buffer to program
        boolean status = programInputRowAdapter.addCodeLine(programInputBuffer.getText().toString());

        // Clear buffer (if successfully added to program)
        if (status) {
            programInputBuffer.setText("");
        }

        // Scroll to position
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                int position = Integer.parseInt(posProgramLine);
                programInputRecyclerView.scrollToPosition(position + 1);
            }
        }, 200);
    }

    private void deleteCodeLine(){
        programInputRowAdapter.deleteCodeLine();
    }

    private void bufferUpdate(){
        programInputBuffer.setText(programInputRowAdapter.getSelectedContent());
        NumpadPopup numpadPopup = new NumpadPopup(this.requireActivity(), programInputBuffer, 1);
        numpadPopup.showNumpadPopup(this.requireActivity());
    }

    private void removeFocus(){
        posProgramLine = "-1";
        programInputRowAdapter.notifyDataSetChanged();
    }

    public void addCommand () {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (addedCommandText != null && !lastAddedCommand.equals(addedCommandText)){
                    try {
                        String currentText = programInputBuffer.getText().toString();
                        String resultText = currentText + " " + addedCommandText;
                        requireActivity().runOnUiThread(() -> programInputBuffer.setText(resultText));
                        lastAddedCommand = addedCommandText;
                    }
                    catch (Exception e){
                        System.out.println("Error watching cmd: " + e.getMessage());
                    }
                }
                handler.postDelayed(this, 100);
            }
        }, 100);
    }

    private void backToProgramList () {
        ((ProgramFragment) getParentFragment()).showProgramList();
        jointDataSharedViewModel.clearJointData();
        worldDataSharedViewModel.clearWorldData();
        programInputRowAdapter.clearCodeContent();

        handler.removeCallbacksAndMessages(null);
    }

    private String refineCtrlFile(String content, boolean leaveOneEmptyAfterPointDeclaration) {
        if (!content.contains("JOINT #P[500][3]\"")){
            content = "JOINT #P[500][3]" + "\n" + "TRANS P[500][6]" + "\n" + content;
        }

        // Normalize line endings to '\n' to avoid issues with mixed formats
        content = content.replace("\r\n", "\n").replace("\r", "\n");

        // Split the content into lines
        String[] lines = content.split("\n");

        // Use a StringBuilder to rebuild the content
        StringBuilder refinedContent = new StringBuilder();
        String previousLine = ""; // To track the previous line for fixing brackets
        boolean addedEmptyLineAfterPoint = false; // Track if an empty line was added after a point declaration

        for (String line : lines) {
            line = line.trim(); // Remove leading and trailing spaces

            if (!line.isEmpty()) { // Only process non-empty lines
                addedEmptyLineAfterPoint = false; // Reset the flag when a non-empty line is encountered

                // Check if the current line is a dropped closing square bracket
                if (line.equals("]") && !previousLine.isEmpty()) {
                    // Append the closing bracket to the previous line
                    refinedContent.setLength(refinedContent.length() - 1); // Remove the last newline
                    refinedContent.append("]").append("\n");
                } else {
                    // Append the line normally
                    refinedContent.append(line).append("\n");
                }
                previousLine = line;
            } else if (leaveOneEmptyAfterPointDeclaration && !addedEmptyLineAfterPoint && previousLine.endsWith("]")) {
                // Add a single empty line after a point declaration
                refinedContent.append("\n");
                addedEmptyLineAfterPoint = true;
            }
        }

        // Ensure the content ends with two newline characters
        String result = refinedContent.toString().trim(); // Remove trailing spaces or newlines
        if (!result.endsWith("\n\n")) {
            result += "\n\n";
        }

        return result;
    }

    private void showToast(String message, boolean longToast) {

        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(this.requireContext(), message, longToast ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show()
        );
    }
}