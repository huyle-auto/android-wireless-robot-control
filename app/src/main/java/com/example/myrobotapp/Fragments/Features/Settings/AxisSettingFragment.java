package com.example.myrobotapp.Fragments.Features.Settings;

import static com.example.myrobotapp.Class.FileManager.readFile;

import android.app.Activity;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myrobotapp.Class.UserInput.NumpadPopup;
import com.example.myrobotapp.HomeScreenActivity;
import com.example.myrobotapp.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AxisSettingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AxisSettingFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AxisSettingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AxisSettingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AxisSettingFragment newInstance(String param1, String param2) {
        AxisSettingFragment fragment = new AxisSettingFragment();
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

    public static final String TAG = "AxisSettingFragment";

    TextView robotAxisCountTextView, inputModuleCountTextView, outputModuleCountTextView, counterModuleCountTextView;
    List<TableRow> listAxisTableRow;
    private int childCount;

    List<String[]> listAxisConfig;
    public static List<String> axisType, axisPPR, axisRPM, axisMaxRPM, axisGearNumerator, axisGearDenominator, axisAccTime, axisMAccTime, axisLimitN, axisLimitP, axisMSpeed, axisServoOnMode;
    public static int numAxis, numInput, numOutput, numCounter;

    ImageButton axisIncButton, axisDecButton, inputIncButton, inputDecButton, outputIncButton, outputDecButton, counterIncButton, counterDecButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_axis_setting, container, false);

        // numAxis = 4;

        // Axis/Input/Output/Counter count
        robotAxisCountTextView = view.findViewById(R.id.robotAxisCountTextView);

        inputModuleCountTextView = view.findViewById(R.id.inputModuleCountTextView);
        outputModuleCountTextView = view.findViewById(R.id.outputModuleCountTextView);
        counterModuleCountTextView = view.findViewById(R.id.counterModuleCountTextView);

        axisIncButton = view.findViewById(R.id.axisIncButton);
        axisDecButton = view.findViewById(R.id.axisDecButton);
        inputIncButton = view.findViewById(R.id.inputIncButton);
        inputDecButton = view.findViewById(R.id.inputDecButton);
        outputIncButton = view.findViewById(R.id.outputIncButton);
        outputDecButton = view.findViewById(R.id.outputDecButton);
        counterIncButton = view.findViewById(R.id.counterIncButton);
        counterDecButton = view.findViewById(R.id.counterDecButton);

        axisIncButton.setOnClickListener(this::changeAxisCount);
        axisDecButton.setOnClickListener(this::changeAxisCount);
        inputIncButton.setOnClickListener(this::changeAxisCount);
        inputDecButton.setOnClickListener(this::changeAxisCount);
        outputIncButton.setOnClickListener(this::changeAxisCount);
        outputDecButton.setOnClickListener(this::changeAxisCount);
        counterIncButton.setOnClickListener(this::changeAxisCount);
        counterDecButton.setOnClickListener(this::changeAxisCount);

        // Wrap axis Table Rows
        listAxisTableRow = new ArrayList<>();

        int[] ids = {R.id.axis_1_Setting_TableRow,
                R.id.axis_2_Setting_TableRow,
                R.id.axis_3_Setting_TableRow,
                R.id.axis_4_Setting_TableRow,
                R.id.axis_5_Setting_TableRow,
                R.id.axis_6_Setting_TableRow};

        for (int i = 0; i < ids.length; i++) {
            listAxisTableRow.add(view.findViewById(ids[i]));
        }

        childCount = listAxisTableRow.get(0).getChildCount();

        // Wrap axis Properties
        listAxisConfig = new ArrayList<>(Collections.nCopies(12, null));

        loadConfiguration();
        init();
        addTextWatchersToTableCells();
        updateAxisLinkType();

        return view;
    }

    private void loadConfiguration() {
        String controllerConfigFilePath = "ControllerSettings/custom/ControllerConfig.txt";
        File controllerConfigFile = new File(this.requireContext().getFilesDir(), controllerConfigFilePath);
        List<String> axisConfigs = new ArrayList<>();

        try {
            String controllerConfigFileText = readFile(controllerConfigFile);
            String[] controllerConfigLines = controllerConfigFileText.split("\n");

            boolean inAxisConfig = false;
            boolean inAxisDef = false;

            int oldAxisCount = 0;

            for (String line : controllerConfigLines) {
                if (line.startsWith("AXIS_COUNT")) {
                    String[] oldAxisCountArray = line.trim().split("=");
                    oldAxisCount = Integer.parseInt(oldAxisCountArray[1]);
                    numAxis = oldAxisCount;
                    robotAxisCountTextView.setText(String.valueOf(numAxis));
                }

                // Iterate until inside of "#Axis link type" section
                if (line.contains("#Axis link type")) {
                    inAxisConfig = true;

                } else if (inAxisConfig) {
                    // Add axisConfig lines
                    axisConfigs.add(line);

                    if (axisConfigs.size() == 12) {
                        break;
                    }
                }
            }

            // Extract corresponding values
            for (String axisConfig : axisConfigs) {
                String[] currentConfig = axisConfig.split("=");
                String axisConfigName = currentConfig[0].trim();
                String axisConfigValue = currentConfig[1].trim();

                switch (axisConfigName) {
                    case "AXIS_TYPE":
                        // Each list element is an array of String where each of array's element is an axis' config value
                        listAxisConfig.set(0, axisConfigValue.split(","));
                        break;
                    case "AXIS_PPR":
                        listAxisConfig.set(1, axisConfigValue.split(","));
                        break;
                    case "AXIS_RPM":
                        listAxisConfig.set(2, axisConfigValue.split(","));
                        break;
                    case "AXIS_MAXRPM":
                        listAxisConfig.set(3, axisConfigValue.split(","));
                        break;
                    case "AXIS_GEAR_NUMERATOR":
                        listAxisConfig.set(4, axisConfigValue.split(","));
                        break;
                    case "AXIS_GEAR_DENOMINATOR":
                        listAxisConfig.set(5, axisConfigValue.split(","));
                        break;
                    case "AXIS_ACCTIME":
                        listAxisConfig.set(6, axisConfigValue.split(","));
                        break;
                    case "AXIS_MACCTIME":
                        listAxisConfig.set(7, axisConfigValue.split(","));
                        break;
                    case "AXIS_LIMITP":
                        listAxisConfig.set(8, axisConfigValue.split(","));
                        break;
                    case "AXIS_LIMITN":
                        listAxisConfig.set(9, axisConfigValue.split(","));
                        break;
                    case "AXIS_MSPEED":
                        listAxisConfig.set(10, axisConfigValue.split(","));
                        break;
                    case "AXIS_SERVO_ON_MODE":
                        listAxisConfig.set(11, axisConfigValue.split(","));
                        break;
                }
            }

            // DEBUG
            System.out.println("axisConfigs update: ");
            for (String line : axisConfigs) {
                System.out.println("axisConfigs value: " + line);
            }

            // CLEAR ALL TABLE VALUE
            for (int i = 1; i < childCount; i++) {
                for (int j = 1; j <= 6; j++) {
                    TableRow tableRow = listAxisTableRow.get(j - 1);
                    EditText editText = (EditText) tableRow.getChildAt(i);
                    editText.setText("");
                }
            }

            // ASSIGN VALUES (column-by-column)
            Log.i(TAG, "Num Axis is: " + numAxis);
            for (int i = 1; i < childCount; i++) {
                for (int j = 1; j <= numAxis; j++) {

                    TableRow tableRow = listAxisTableRow.get(j - 1);
                    EditText editText = (EditText) tableRow.getChildAt(i);
                    editText.setText(listAxisConfig.get(i - 1)[j - 1]);
                }
            }

        } catch (IOException e) {
            Toast.makeText(this.requireActivity(), "Error loading axis configuration: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Every value can be modified using NUMPAD POPUP
    private void init() {
        // Initiate static fields
        axisType = new ArrayList<>();
        axisPPR = new ArrayList<>();
        axisRPM = new ArrayList<>();
        axisMaxRPM = new ArrayList<>();
        axisGearNumerator = new ArrayList<>();
        axisGearDenominator = new ArrayList<>();
        axisAccTime = new ArrayList<>();
        axisMAccTime = new ArrayList<>();
        axisLimitN = new ArrayList<>();
        axisLimitP = new ArrayList<>();
        axisMSpeed = new ArrayList<>();
        axisServoOnMode = new ArrayList<>();

        // Assign numpad popup
//        for (TableRow tableRow : listAxisTableRow) {
//            for (int i = 1; i < childCount; i++) {
//                EditText editText = (EditText) tableRow.getChildAt(i);
//                NumpadPopup numpadPopup = new NumpadPopup(getContext(), editText, 1);
//                numpadPopup.showNumpadPopup(this.requireActivity());
//            }
//        }
    }

    private void addTextWatchersToTableCells() {
        for (int i = 0; i < 6; i++) {
            TableRow row = listAxisTableRow.get(i);

            // Start at 1 to skip the ID TextView (first column)
            for (int j = 1; j < childCount; j++) {
                View child = row.getChildAt(j);
                if (child instanceof EditText) {
                    EditText editText = (EditText) child;

                    editText.addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {}

                        @Override
                        public void afterTextChanged(Editable s) {
                            updateAxisLinkType();  // <-- call your desired method here
                        }
                    });
                }
            }
        }
    }

    private void changeAxisCount(View view) {
        ImageButton button = (ImageButton) view;
        String buttonText = getResources().getResourceEntryName(button.getId());

        switch (buttonText) {
            case "axisIncButton":
                if (numAxis == 4) {
                    return;
                }
                numAxis++;
                robotAxisCountTextView.setText(String.valueOf(numAxis));
                System.out.println("Num Axis is: " + numAxis);
//                loadConfiguration();

                break;
            case "axisDecButton":
                if (numAxis == 1) {
                    return;
                }
                numAxis--;
                robotAxisCountTextView.setText(String.valueOf(numAxis));
                System.out.println("Num Axis is: " + numAxis);
//                loadConfiguration();

                break;

            case "inputIncButton":
                numInput++;
                inputModuleCountTextView.setText(String.valueOf(numInput));
                System.out.println("Num Input is: " + numInput);
                break;

            case "inputDecButton":
                if (numInput == 0) {
                    return;
                }
                numInput--;
                inputModuleCountTextView.setText(String.valueOf(numInput));
                System.out.println("Num Input is: " + numInput);
                break;

            case "outputIncButton":
                numOutput++;
                outputModuleCountTextView.setText(String.valueOf(numOutput));
                System.out.println("Num Output is: " + numOutput);
                break;

            case "outputDecButton":
                if (numOutput == 0) {
                    return;
                }
                numOutput--;
                outputModuleCountTextView.setText(String.valueOf(numOutput));
                System.out.println("Num Output is: " + numOutput);
                break;

            case "counterIncButton":
                numCounter++;
                counterModuleCountTextView.setText(String.valueOf(numCounter));
                System.out.println("Num Counter is: " + numCounter);
                break;

            case "counterDecButton":
                if (numCounter == 0) {
                    return;
                }
                numCounter--;
                counterModuleCountTextView.setText(String.valueOf(numCounter));
                System.out.println("Num Counter is: " + numCounter);
                break;

            default:
                break;
        }

        updateAxisLinkType();
    }

    private void updateAxisLinkType() {
        // AXIS_TYPE
        axisType.clear();
        for (int i = 0; i < numAxis; i++) {
            TableRow row = listAxisTableRow.get(i);
            EditText cell = (EditText) row.getChildAt(1);
            axisType.add(cell.getText().toString().trim());
        }

        // AXIS_PPR
        axisPPR.clear();
        for (int i = 0; i < numAxis; i++) {
            TableRow row = listAxisTableRow.get(i);
            EditText cell = (EditText) row.getChildAt(2);
            axisPPR.add(cell.getText().toString().trim());
        }

        // AXIS_RPM
        axisRPM.clear();
        for (int i = 0; i < numAxis; i++) {
            TableRow row = listAxisTableRow.get(i);
            EditText cell = (EditText) row.getChildAt(3);
            axisRPM.add(cell.getText().toString().trim());
        }

        // AXIS_MAXRPM
        axisMaxRPM.clear();
        for (int i = 0; i < numAxis; i++) {
            TableRow row = listAxisTableRow.get(i);
            EditText cell = (EditText) row.getChildAt(4);
            axisMaxRPM.add(cell.getText().toString().trim());
        }

        // GEAR_NUMERATOR
        axisGearNumerator.clear();
        for (int i = 0; i < numAxis; i++) {
            TableRow row = listAxisTableRow.get(i);
            EditText cell = (EditText) row.getChildAt(5);
            axisGearNumerator.add(cell.getText().toString().trim());
        }

        // GEAR_DENOMINATOR
        axisGearDenominator.clear();
        for (int i = 0; i < numAxis; i++) {
            TableRow row = listAxisTableRow.get(i);
            EditText cell = (EditText) row.getChildAt(6);
            axisGearDenominator.add(cell.getText().toString().trim());
        }

        // AXIS_ACCTIME
        axisAccTime.clear();
        for (int i = 0; i < numAxis; i++) {
            TableRow row = listAxisTableRow.get(i);
            EditText cell = (EditText) row.getChildAt(7);
            axisAccTime.add(cell.getText().toString().trim());
        }

        // AXIS_MACCTIME
        axisMAccTime.clear();
        for (int i = 0; i < numAxis; i++) {
            TableRow row = listAxisTableRow.get(i);
            EditText cell = (EditText) row.getChildAt(8);
            axisMAccTime.add(cell.getText().toString().trim());
        }

        // AXIS_LIMITP
        axisLimitP.clear();
        for (int i = 0; i < numAxis; i++) {
            TableRow row = listAxisTableRow.get(i);
            EditText cell = (EditText) row.getChildAt(9);
            axisLimitP.add(cell.getText().toString().trim());
        }

        // AXIS_LIMITN
        axisLimitN.clear();
        for (int i = 0; i < numAxis; i++) {
            TableRow row = listAxisTableRow.get(i);
            EditText cell = (EditText) row.getChildAt(10);
            axisLimitN.add(cell.getText().toString().trim());
        }

        // AXIS_MSPEED
        axisMSpeed.clear();
        for (int i = 0; i < numAxis; i++) {
            TableRow row = listAxisTableRow.get(i);
            EditText cell = (EditText) row.getChildAt(11);
            axisMSpeed.add(cell.getText().toString().trim());
        }

        // AXIS_SERVO_ON_MODE
        axisServoOnMode.clear();
        for (int i = 0; i < numAxis; i++) {
            TableRow row = listAxisTableRow.get(i);
            EditText cell = (EditText) row.getChildAt(12);
            axisServoOnMode.add(cell.getText().toString().trim());
        }
    }
}