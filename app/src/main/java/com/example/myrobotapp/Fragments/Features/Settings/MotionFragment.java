package com.example.myrobotapp.Fragments.Features.Settings;

import static com.example.myrobotapp.Class.FileManager.createTempTextFile;
import static com.example.myrobotapp.Class.FileManager.readFile;
import static com.example.myrobotapp.Class.FileManager.writeToDir;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.myrobotapp.Class.UserInput.NumpadPopup;
import com.example.myrobotapp.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MotionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MotionFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public MotionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MotionFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MotionFragment newInstance(String param1, String param2) {
        MotionFragment fragment = new MotionFragment();
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

    EditText manualLinearSpeedEditText, manualRotationSpeedEditText,
             manualAccTimeEditText, maxAccTimeEditText,
             maxLinearSpeedEditText, maxRotationSpeedEditText,
             accuracyEditText;

    public static float manLinSpeed, manRotSpeed,
                         manAccTime, maxAccTime,
                         maxLinSpeed, maxRotSpeed,
                         accuracy;

    Button saveMotionConfigButton, loadDefaultMotionConfigButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_motion, container, false);

        // Assign Edit Text fields
        int[] ids = {
                        R.id.manual_linear_speed_EditText, R.id.manual_rotation_speed_EditText,
                        R.id.manual_acc_time_EditText, R.id.max_acc_time_EditText,
                        R.id.max_linear_speed_EditText, R.id.max_rotation_speed_EditText,
                        R.id.accuracy_EditText
        };

        manualLinearSpeedEditText = view.findViewById(ids[0]);
        manualRotationSpeedEditText = view.findViewById(ids[1]);
        manualAccTimeEditText = view.findViewById(ids[2]);
        maxAccTimeEditText = view.findViewById(ids[3]);
        maxLinearSpeedEditText = view.findViewById(ids[4]);
        maxRotationSpeedEditText = view.findViewById(ids[5]);
        accuracyEditText = view.findViewById(ids[6]);

        saveMotionConfigButton = view.findViewById(R.id.saveMotionConfigButton);
        saveMotionConfigButton.setOnClickListener(v -> {
            saveConfiguration();
        });

        loadDefaultMotionConfigButton = view.findViewById(R.id.loadDefaultMotionConfigButton);
        loadDefaultMotionConfigButton.setOnClickListener(v -> {
            loadConfiguration();
        });

        configureMotionValues();
        loadConfiguration();

        return view;
    }

    private void configureMotionValues(){
        manualLinearSpeedEditText.setOnClickListener(v -> {
            NumpadPopup numpadPopup = new NumpadPopup(this.requireActivity(), manualLinearSpeedEditText, 2);
            numpadPopup.showNumpadPopup(this.requireActivity());
            manualLinearSpeedEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    manLinSpeed = Float.parseFloat(manualLinearSpeedEditText.getText().toString());
                }
            });
        });

        manualRotationSpeedEditText.setOnClickListener(v -> {
            NumpadPopup numpadPopup = new NumpadPopup(this.requireActivity(), manualRotationSpeedEditText, 2);
            numpadPopup.showNumpadPopup(this.requireActivity());
            manualRotationSpeedEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    manRotSpeed = Float.parseFloat(manualRotationSpeedEditText.getText().toString());
                }
            });
        });

        manualAccTimeEditText.setOnClickListener(v -> {
            NumpadPopup numpadPopup = new NumpadPopup(this.requireActivity(), manualAccTimeEditText, 2);
            numpadPopup.showNumpadPopup(this.requireActivity());
            manualAccTimeEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    manAccTime = Float.parseFloat(manualAccTimeEditText.getText().toString());
                }
            });
        });

        maxAccTimeEditText.setOnClickListener(v -> {
            NumpadPopup numpadPopup = new NumpadPopup(this.requireActivity(), maxAccTimeEditText, 2);
            numpadPopup.showNumpadPopup(this.requireActivity());
            maxAccTimeEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    maxAccTime = Float.parseFloat(maxAccTimeEditText.getText().toString());
                }
            });
        });

        maxLinearSpeedEditText.setOnClickListener(v -> {
            NumpadPopup numpadPopup = new NumpadPopup(this.requireActivity(), maxLinearSpeedEditText, 2);
            numpadPopup.showNumpadPopup(this.requireActivity());
            maxLinearSpeedEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    maxLinSpeed = Float.parseFloat(maxLinearSpeedEditText.getText().toString());
                }
            });
        });

        maxRotationSpeedEditText.setOnClickListener(v -> {
            NumpadPopup numpadPopup = new NumpadPopup(this.requireActivity(), maxRotationSpeedEditText, 2);
            numpadPopup.showNumpadPopup(this.requireActivity());
            maxRotationSpeedEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    maxRotSpeed = Float.parseFloat(maxRotationSpeedEditText.getText().toString());
                }
            });
        });

        accuracyEditText.setOnClickListener(v -> {
            NumpadPopup numpadPopup = new NumpadPopup(this.requireActivity(), accuracyEditText, 2);
            numpadPopup.showNumpadPopup(this.requireActivity());
            accuracyEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    accuracy = Float.parseFloat(accuracyEditText.getText().toString());
                }
            });
        });
    }

    private void loadConfiguration () {
        String controllerConfigFilePath = "ControllerSettings/custom/ControllerConfig.txt";
        File controllerConfigFile = new File(this.requireContext().getFilesDir(), controllerConfigFilePath);
        List<String> motionConfigs = new ArrayList<>();

        try {
            String controllerConfigFileText = readFile(controllerConfigFile);
            String[] controllerConfigLines = controllerConfigFileText.split("\n");
            boolean inMotionParam = false;

            for (String line : controllerConfigLines) {
                // Iterate until inside of #motion parameter section
                if (line.contains("#Motion parameter")) {
                    inMotionParam = true;

                }

                else if (inMotionParam){
                    // Add motion config lines
                    motionConfigs.add(line);

                    if (motionConfigs.size() == 7) {
                        break;
                    }
                }
            }

            // Extract corresponding values
            for (String motionConfig : motionConfigs) {
                String[] currentConfig = motionConfig.split("=");
                String motionName = currentConfig[0].trim();
                String motionValue  = currentConfig[1].trim();

                switch (currentConfig[0]) {
                    case "M_LSPEED":
                        manualLinearSpeedEditText.setText(motionValue);
                        manLinSpeed = Float.parseFloat(manualLinearSpeedEditText.getText().toString());
                        break;
                    case "M_RSPEED":
                        manualRotationSpeedEditText.setText(motionValue);
                        manRotSpeed = Float.parseFloat(manualRotationSpeedEditText.getText().toString());
                        break;
                    case "M_ACCTIME":
                        manualAccTimeEditText.setText(motionValue);
                        manAccTime = Float.parseFloat(manualAccTimeEditText.getText().toString());
                        break;
                    case "MAX_ACCTIME":
                        maxAccTimeEditText.setText(motionValue);
                        maxAccTime = Float.parseFloat(maxAccTimeEditText.getText().toString());
                        break;
                    case "MAX_LSPEED":
                        maxLinearSpeedEditText.setText(motionValue);
                        maxLinSpeed = Float.parseFloat(maxLinearSpeedEditText.getText().toString());
                        break;
                    case "MAX_RSPEED":
                        maxRotationSpeedEditText.setText(motionValue);
                        maxRotSpeed = Float.parseFloat(maxRotationSpeedEditText.getText().toString());
                        break;
                    case "ACCURACY":
                        accuracyEditText.setText(motionValue);
                        accuracy = Float.parseFloat(accuracyEditText.getText().toString());
                        break;
                }
            }

            // DEBUG
            System.out.println("Motion configuration update: ");
            for (String line : motionConfigs) {
                System.out.println("Motion value: " + line);
            }

        } catch (IOException e) {
            Toast.makeText(this.requireActivity(), "Error loading motion configuration: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveConfiguration () {
        String controllerConfigFilePath = "ControllerSettings/custom/ControllerConfig.txt";
        File controllerConfigFile = new File(this.requireContext().getFilesDir(), controllerConfigFilePath);
        StringBuilder stringBuilder = new StringBuilder();

        try {
            String controllerConfigFileText = readFile(controllerConfigFile);
            String[] controllerConfigLines = controllerConfigFileText.split("\n");
            boolean inMotionParam = false;
            String[] currentConfig;
            String newConfigLine = "";

            for (String line : controllerConfigLines) {
                // Iterate until inside of #motion parameter section
                if (line.contains("#Motion parameter")) {
                    inMotionParam = true;
                }

                else if (line.contains("#PLC parameter")) {
                    inMotionParam = false;
                }

                else if (inMotionParam){
                    currentConfig = line.split("=");

                    switch (currentConfig[0].trim()) {
                        case "M_LSPEED":
                            currentConfig[1] = manualLinearSpeedEditText.getText().toString();
                            break;
                        case "M_RSPEED":
                            currentConfig[1] = manualRotationSpeedEditText.getText().toString();
                            break;
                        case "M_ACCTIME":
                            currentConfig[1] = manualAccTimeEditText.getText().toString();
                            break;
                        case "MAX_ACCTIME":
                            currentConfig[1] = maxAccTimeEditText.getText().toString();
                            break;
                        case "MAX_LSPEED":
                            currentConfig[1] = maxLinearSpeedEditText.getText().toString();
                            break;
                        case "MAX_RSPEED":
                            currentConfig[1] = maxRotationSpeedEditText.getText().toString();
                            break;
                        case "ACCURACY":
                            currentConfig[1] = accuracyEditText.getText().toString();
                            break;
                    }
                    newConfigLine = currentConfig[0] + "=" + currentConfig[1];
                    stringBuilder.append(newConfigLine).append("\n");
                    continue;
                }
                // Append every single line
                stringBuilder.append(line).append("\n");
            }

            // Save to ControllerSettings/custom/
            File txtFile = createTempTextFile(this.requireContext(), stringBuilder.toString(), "Anyname");
            String destFolderName = "ControllerSettings/custom";
            String destFileName = "ControllerConfig.txt";
            writeToDir(this.requireContext(), destFolderName, destFileName, txtFile);
            Toast.makeText(this.requireActivity(), "Saved motion config to device", Toast.LENGTH_SHORT).show();


        } catch (IOException e) {
            Toast.makeText(this.requireActivity(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }
}