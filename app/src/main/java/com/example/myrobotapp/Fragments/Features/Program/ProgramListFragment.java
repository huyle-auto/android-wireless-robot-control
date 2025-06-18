package com.example.myrobotapp.Fragments.Features.Program;

import static com.example.myrobotapp.Class.DataTable.ProgramFile.ProgramDataTableRowAdapter.posProgramTable;
import static com.example.myrobotapp.Class.DataTable.ProgramFile.ProgramDataTableRowAdapter.selectedProgramName;
import static com.example.myrobotapp.Class.FileManager.duplicateFolder;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myrobotapp.Class.DataTable.ProgramFile.ProgramDataSharedViewModel;
import com.example.myrobotapp.Class.DataTable.ProgramFile.ProgramDataTableRowAdapter;
import com.example.myrobotapp.Class.DataTable.ProgramFile.ProgramDataTableRowModel;
import com.example.myrobotapp.Class.Dialog.DialogPopup;
import com.example.myrobotapp.Class.Dialog.ErrorDialog;
import com.example.myrobotapp.Class.Dialog.InfoDialog;
import com.example.myrobotapp.Class.Privilege.PrivilegeManager;
import com.example.myrobotapp.Class.Privilege.SessionManager;
import com.example.myrobotapp.Fragments.Features.ProgramFragment;
import com.example.myrobotapp.R;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProgramListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProgramListFragment extends Fragment implements SessionManager.RoleChangeListener{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ProgramListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProgramListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProgramListFragment newInstance(String param1, String param2) {
        ProgramListFragment fragment = new ProgramListFragment();
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
        // Register listener
        SessionManager.registerListener(this);
    }

    private Handler handler;
    private Runnable task;

    FrameLayout programListFrameLayout;
    public static ProgramDataSharedViewModel programDataSharedViewModel;
    private ProgramDataTableRowAdapter programDataTableRowAdapter;
    private RecyclerView programListRecyclerView;

    Button newProgramButton, duplicateProgramButton, editProgramButton, runProgramButton, deleteProgramButton;
    Dialog dialog;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_program_list, container, false);

        // Assign Buttons and methods
        newProgramButton = view.findViewById(R.id.newProgramButton);
        newProgramButton.setOnClickListener(v -> newProgram());
        duplicateProgramButton = view.findViewById(R.id.duplicateProgramButton);
        duplicateProgramButton.setOnClickListener(v -> duplicateProgram());
        editProgramButton = view.findViewById(R.id.editProgramButton);
        editProgramButton.setOnClickListener(v -> showProgramEdit());
        runProgramButton = view.findViewById(R.id.runProgramButton);
        runProgramButton.setOnClickListener(v -> runProgram());
        deleteProgramButton = view.findViewById(R.id.deleteProgramButton);
        deleteProgramButton.setOnClickListener(v -> deleteProgram());

        // Setup Shared Data and Recycler View
        programListRecyclerView = view.findViewById(R.id.programListRecyclerView);
        programDataSharedViewModel = new ViewModelProvider(requireActivity()).get(ProgramDataSharedViewModel.class);
        programDataSharedViewModel.getProgram().observe(getViewLifecycleOwner(), this::setProgramListRecyclerView);

        programListFrameLayout = view.findViewById(R.id.programListFrameLayout);
        // Remove focus on selected program
        programListFrameLayout.setOnClickListener(v -> {
            posProgramTable = "-1";
        });

        // Startup
//        Handler handler = new Handler(Looper.getMainLooper());
//        Runnable task = new Runnable() {
//            @Override
//            public void run() {
//                // Perform the task
//                updateProgramList();
//
//                // Re-post the task with a delay
//                handler.postDelayed(this, 800); // 1-second delay
//            }
//        };
//
//        handler.post(task);

        return view;
    }

    // Safely update program list (without crashing due to unattached fragment and requireContext() accordingly)
    @Override
    public void onStart() {
        super.onStart();
        handler = new Handler(Looper.getMainLooper());
        task = new Runnable() {
            @Override
            public void run() {
                if (isAdded()) { // Ensure fragment is attached
                    updateProgramList();
                    handler.postDelayed(this, 3000);
                }
            }
        };
        handler.post(task);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (handler != null && task != null) {
            handler.removeCallbacks(task);
        }
    }

    private void setProgramListRecyclerView(List<ProgramDataTableRowModel> inputProgram) {
        programListRecyclerView.setHasFixedSize(true);;
        programListRecyclerView.setLayoutManager(new LinearLayoutManager(this.requireActivity()));

        List<ProgramDataTableRowModel> dataList = getProgramList(inputProgram);
        programDataTableRowAdapter = new ProgramDataTableRowAdapter(this.requireActivity(), dataList);
        programListRecyclerView.setAdapter(programDataTableRowAdapter);
        programDataTableRowAdapter.notifyDataSetChanged();
    }

    private List<ProgramDataTableRowModel> getProgramList(List<ProgramDataTableRowModel> inputProgram) {
        List<ProgramDataTableRowModel> programList = new ArrayList<>();

        for (int i=0; i<inputProgram.size(); i++){
            if (inputProgram.size() >= 0){

                String programName = inputProgram.get(i).getProgramName();
                String tag = inputProgram.get(i).getTag();
                String timeStamp = inputProgram.get(i).getTimeStamp();
                String attribute = inputProgram.get(i).getAttribute();

                // Add row Data to list of rows
                programList.add(new ProgramDataTableRowModel(programName, tag, timeStamp, attribute));
            }
        }
        return programList;
    }

    public void updateProgramList(){
        // Update whenever files directory is changed. Create/Delete program files will add/delete files in internal storage
        // instead of updating to ViewModel (which update from internal storage itself)
        // Let's first iterate through all files in internal storage
        File directory = new File(requireContext().getFilesDir(), "Program/");
        ArrayList<String> fileNames = new ArrayList<>();
        ArrayList<String> timeStamps = new ArrayList<>();

        if (directory.exists() && directory.isDirectory()) {

            // "files" variable here is actually folders
            File[] files = directory.listFiles();
            if (files != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd   HH:mm:ss", Locale.getDefault());

                for (File file : files) {
                    if (file.isDirectory()) {
                        fileNames.add(file.getName());

                        // Target program .txt file for accurate timestamp
                        File txtFile = new File(file, file.getName() + ".txt");
                        timeStamps.add(sdf.format(txtFile.lastModified()));
                    }
                }
            } else {
                System.out.println("No files found in the directory.");
            }
        } else {
            System.out.println("Directory does not exist.");
        }

        // UPDATE TO PROGRAM LIST TABLE (maybe online first load, remember to load current files to ViewModel too)
        List<ProgramDataTableRowModel> programs = new ArrayList<>(fileNames.size());
        for (int i=0; i < fileNames.size(); i++){
            ProgramDataTableRowModel program = new ProgramDataTableRowModel(fileNames.get(i), "", timeStamps.get(i), "");
            programs.add(program);
        }

        programDataSharedViewModel.updateProgram(programs);
    }

    private void newProgram(){
        // Create empty .txt file and empty .pnt file
        // Ctrl's file will be created after saving in Edit UI
        DialogPopup dialogPopup = new DialogPopup(this.requireContext(), "NEW PROGRAM");
        dialogPopup.showNumpadPopup(this.requireActivity());
    }

    private void duplicateProgram(){
        duplicateFolder(this.requireContext(), "Program/" + selectedProgramName);
    }

    private void showProgramEdit(){
        if (!("-1".equals(posProgramTable))){
            ((ProgramFragment) getParentFragment()).showProgramEdit();
        }
        else {
            Toast.makeText(this.requireContext(), "Choose a program first", Toast.LENGTH_SHORT).show();
        }
    }

    private void runProgram(){
        InfoDialog infoDialog = new InfoDialog(this.requireContext(), "");
        infoDialog.setInfoHint("Program has been started");
        infoDialog.showNumpadPopup(this.requireActivity());
    }

    private void deleteProgram(){
        DialogPopup dialogPopup = new DialogPopup(this.requireContext(), "DELETE PROGRAM");
        dialogPopup.showNumpadPopup(this.requireActivity());
    }

    private void updateUI(String role) {
        // Get the list of allowed fields for the current role
        List<String> unAllowedFields = PrivilegeManager.getUnAllowedFields(role, "ProgramListFragment");

        // Debug
        System.out.println("Un-allowed fields are: ");
        for (String field : unAllowedFields) {
            System.out.println(field + ", ");
        }

        // Get all declared fields in the current class
        Field[] fields = this.getClass().getDeclaredFields();

        for (Field field : fields) {
            // Only process fields that are UI components (e.g., Button, EditText, RecyclerView)
            if (Button.class.isAssignableFrom(field.getType()) ||
                    EditText.class.isAssignableFrom(field.getType()) ||
                    RecyclerView.class.isAssignableFrom(field.getType())) {

                try {
                    field.setAccessible(true); // Allow access to private fields
                    Object fieldInstance = field.get(this); // Get the field instance

                    if (fieldInstance instanceof View) {
                        View view = (View) fieldInstance;
                        String fieldName = field.getName(); // Get the field's name

                        // Enable or disable the field based on the role's allowed fields
                        view.setEnabled(!unAllowedFields.contains(fieldName));
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onRoleChanged(String newRole) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> updateUI(newRole));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Remove listener (prevent memory leaks)
        SessionManager.unregisterListener(this);
    }
}