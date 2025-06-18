package com.example.myrobotapp.Fragments.Features.Settings;

import static com.example.myrobotapp.Class.FileManager.readFile;
import static com.example.myrobotapp.Class.ProtocolReceive.programRunningStatus;
import static com.example.myrobotapp.Class.ProtocolReceive.sendProgramEndPartACK;
import static com.example.myrobotapp.Class.ProtocolReceive.sendProgramPartACK;
import static com.example.myrobotapp.Class.ProtocolSend.readInternalFile;
import static com.example.myrobotapp.Fragments.Features.HistoryFragment.recordOperation;
import static com.example.myrobotapp.Fragments.Features.Settings.AxisSettingFragment.axisAccTime;
import static com.example.myrobotapp.Fragments.Features.Settings.AxisSettingFragment.axisGearDenominator;
import static com.example.myrobotapp.Fragments.Features.Settings.AxisSettingFragment.axisGearNumerator;
import static com.example.myrobotapp.Fragments.Features.Settings.AxisSettingFragment.axisLimitN;
import static com.example.myrobotapp.Fragments.Features.Settings.AxisSettingFragment.axisLimitP;
import static com.example.myrobotapp.Fragments.Features.Settings.AxisSettingFragment.axisMAccTime;
import static com.example.myrobotapp.Fragments.Features.Settings.AxisSettingFragment.axisMSpeed;
import static com.example.myrobotapp.Fragments.Features.Settings.AxisSettingFragment.axisMaxRPM;
import static com.example.myrobotapp.Fragments.Features.Settings.AxisSettingFragment.axisPPR;
import static com.example.myrobotapp.Fragments.Features.Settings.AxisSettingFragment.axisRPM;
import static com.example.myrobotapp.Fragments.Features.Settings.AxisSettingFragment.axisServoOnMode;
import static com.example.myrobotapp.Fragments.Features.Settings.AxisSettingFragment.axisType;
import static com.example.myrobotapp.Fragments.Features.Settings.AxisSettingFragment.numAxis;
import static com.example.myrobotapp.Fragments.Features.Settings.ConnectionFragment.activeConnectionKey;
import static com.example.myrobotapp.Fragments.Features.Settings.ConnectionFragment.bluetoothHelper;
import static com.example.myrobotapp.Fragments.Features.Settings.ConnectionFragment.globalSelectedBTDevice;
import static com.example.myrobotapp.Fragments.Features.Settings.MotionFragment.accuracy;
import static com.example.myrobotapp.Fragments.Features.Settings.MotionFragment.manAccTime;
import static com.example.myrobotapp.Fragments.Features.Settings.MotionFragment.manLinSpeed;
import static com.example.myrobotapp.Fragments.Features.Settings.MotionFragment.manRotSpeed;
import static com.example.myrobotapp.Fragments.Features.Settings.MotionFragment.maxAccTime;
import static com.example.myrobotapp.Fragments.Features.Settings.MotionFragment.maxLinSpeed;
import static com.example.myrobotapp.Fragments.Features.Settings.MotionFragment.maxRotSpeed;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.myrobotapp.Class.Communication.PriorityCommunication;
import com.example.myrobotapp.Class.DeviceTable.ActiveDevice.ActiveDeviceTableRowModel;
import com.example.myrobotapp.Class.Dialog.ErrorDialog;
import com.example.myrobotapp.Class.Dialog.InfoDialog;
import com.example.myrobotapp.Class.FileManager;
import com.example.myrobotapp.Class.GlobalData;
import com.example.myrobotapp.Class.TCP.TCPClient;
import com.example.myrobotapp.Class.UserInput.NumpadPopup;
import com.example.myrobotapp.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RobotTypeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RobotTypeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public RobotTypeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RobotTypeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RobotTypeFragment newInstance(String param1, String param2) {
        RobotTypeFragment fragment = new RobotTypeFragment();
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

    private final String TAG = "RobotTypeFragmnet";
    private final String CUSTOM_CONTROLLER_CONFIG_FILE_PATH = "ControllerSettings/custom/ControllerConfig.txt";
    private final String CUSTOM_CONTROLLER_CONFIG_FOLDER_PATH = "ControllerSettings/custom/";
    private final String CUSTOM_CONTROLLER_CONFIG_FILE_NAME = "ControllerConfig.txt";

    // SYSTEM I/O
    public static int systemInput1, systemInput2, systemOutputType, systemOutput1, systemOutput2;


    EditText link1Length, link2Length, link3Length, link4Length, link5Length, link6Length;
    EditText[] linkLengthEditText;
    Spinner robotTypeDropDown;
    public static String[] robotHome1, robotHome2;
    public static String robotType;
    Button saveRobotTypeConfigButton, sendConfigToControllerButton;

    ImageView robotGeometryImageView;

    Handler handler = new Handler();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_robot_type, container, false);

        robotTypeDropDown = view.findViewById(R.id.robotTypeDropDown);
        String[] robotTypes = {"SCARA", "DELTA"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this.requireActivity(), R.layout.custom_spinner_item, robotTypes);

        // Robot image
        robotGeometryImageView = view.findViewById(R.id.robotGeomatryImageView);

        // Set dropdown adapter to Spinner
        robotTypeDropDown.setAdapter(adapter);

        // Set action on selected robot type
        robotTypeDropDown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                // Action based on selected item
                String selectedItem = robotTypes[i];
                Toast.makeText(requireContext(), "Selected: " + selectedItem, Toast.LENGTH_SHORT).show();

                // Example: Perform action based on selected item
                switch (selectedItem) {
                    case "SCARA":
                        robotGeometryImageView.setBackgroundResource(R.drawable.geometry_scara);
                        break;
                    case "Delta":
                        robotGeometryImageView.setBackgroundResource(R.drawable.geometry_delta);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // Set link length Edit Texts (L1 -> L6)
        linkLengthEditText = new EditText[]{link1Length, link2Length, link3Length, link4Length, link5Length, link6Length};
        int[] ids = {R.id.link_1_length, R.id.link_2_length, R.id.link_3_length, R.id.link_4_length, R.id.link_5_length, R.id.link_6_length};
        for (int i = 0; i < ids.length; i++) {
            linkLengthEditText[i] = view.findViewById(ids[i]);
            linkLengthEditText[i].setShowSoftInputOnFocus(false);
            linkLengthEditText[i].setFocusableInTouchMode(false);
            linkLengthEditText[i].setFocusable(false);

            int finalI = i;
            linkLengthEditText[i].setOnClickListener(v -> {
                NumpadPopup numpadPopup = new NumpadPopup(this.requireActivity(), linkLengthEditText[finalI], 1);
                numpadPopup.showNumpadPopup(this.requireActivity());
            });
        }

        saveRobotTypeConfigButton = view.findViewById(R.id.saveRobotTypeConfigButton);
        saveRobotTypeConfigButton.setOnClickListener(v -> saveConfiguration());

        sendConfigToControllerButton = view.findViewById(R.id.sendConfigToControllerButton);
        sendConfigToControllerButton.setOnClickListener(v -> sendConfigToController());

        loadConfiguration();

        return view;
    }

    private void loadConfiguration() {
        File controllerConfigFile = new File(this.requireContext().getFilesDir(), CUSTOM_CONTROLLER_CONFIG_FILE_PATH);
        List<String> linkConfigs = new ArrayList<>();

        try {
            String controllerConfigFileText = readFile(controllerConfigFile);
            String[] controllerConfigLines = controllerConfigFileText.split("\n");
            boolean inLinkParam = false;
            boolean inAxisDef = false;

            for (String line : controllerConfigLines) {
                // Iterate until inside of #motion parameter section
                if (line.contains("#Link parameter")) {
                    inLinkParam = true;

                } else if (inLinkParam) {
                    // Add robot type name + links length + 2 home position
                    linkConfigs.add(line);

                    if (linkConfigs.size() == 4) {
                        break;
                    }
                }
            }

            // Extract corresponding values
            for (String linkConfig : linkConfigs) {
                String[] currentConfig = linkConfig.split("=");
                String linkParamName = currentConfig[0].trim();
                String linkParamValue = currentConfig[1].trim();

                switch (linkParamName) {
                    case "ROBOT_TYPE":
                        robotType = currentConfig[1];
                        robotTypeDropDown.setSelection(robotType.equals("SCARA") ? 0 : 1);
                        break;

                    case "ROBOT_PARAM":
                        String[] linkLengths = linkParamValue.split(",");
                        for (int i = 0; i < linkLengths.length; i++) {
                            linkLengthEditText[i].setText(linkLengths[i]);
                        }
                        break;

                    case "ROBOT_HOME1":
                        robotHome1 = linkParamValue.split(",");
                        break;

                    case "ROBOT_HOME2":
                        robotHome2 = linkParamValue.split(",");
                        break;
                    default:
                        Toast.makeText(this.requireActivity(), "Error reading lines of Link Paramter", Toast.LENGTH_SHORT).show();
                }
            }

            System.out.println("Link configuration update: ");
            for (String line : linkConfigs) {
                System.out.println("Link value: " + line);
            }

        } catch (IOException e) {
            Toast.makeText(this.requireActivity(), "Error loading robot link configuration: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveConfiguration() {
        List<String> newConfigLines = new ArrayList<>();

        try {
            // Determine number of link lengths
            int linkLengthNum = 0;

            if (robotTypeDropDown.getSelectedItem().toString().equals("DELTA")) {
                linkLengthNum = 5;
            } else if (robotTypeDropDown.getSelectedItem().toString().equals("SCARA")) {
                linkLengthNum = 4;
            }

            // Don't care about old file. Build a whole new file from the template structure
            // 1. CONTROLLER_MODEL
            // Default: CONTROLLER_MODEL=LCXMC-MOTION
            newConfigLines.add("CONTROLLER_MODEL=LCXMC-MOTION");

            // 2. DEVICES
            newConfigLines.add("ECAT_DEVICE=" + DevicesFragment.activeDeviceList.size());
            for (ActiveDeviceTableRowModel device : DevicesFragment.activeDeviceList) {
                newConfigLines.add(device.getName());
            }

            // 3. AXIS DEFINITION
            newConfigLines.add("#Axis definition");
            newConfigLines.add("AXIS_COUNT=" + numAxis);

            // 4. AXIS LINK TYPE
            newConfigLines.add("#Axis link type");

            // AXIS_TYPE
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < axisType.size(); i++) {
                sb.append(axisType.get(i));
                if (i < axisType.size() - 1) sb.append(",");
            }
            newConfigLines.add("AXIS_TYPE=" + sb.toString());

            // AXIS_PPR
            sb.setLength(0);    // Clear string builder
            for (int i = 0; i < axisPPR.size(); i++) {
                sb.append(axisPPR.get(i));
                if (i < axisPPR.size() - 1) sb.append(",");
            }
            newConfigLines.add("AXIS_PPR=" + sb.toString());

            // AXIS_RPM
            sb.setLength(0);    // Clear string builder
            for (int i = 0; i < axisRPM.size(); i++) {
                sb.append(axisRPM.get(i));
                if (i < axisRPM.size() - 1) sb.append(",");
            }
            newConfigLines.add("AXIS_RPM=" + sb.toString());

            // AXIS_MAXRPM
            sb.setLength(0);    // Clear string builder
            for (int i = 0; i < axisMaxRPM.size(); i++) {
                sb.append(axisMaxRPM.get(i));
                if (i < axisMaxRPM.size() - 1) sb.append(",");
            }
            newConfigLines.add("AXIS_MAXRPM=" + sb.toString());

            // AXIS_GEAR_NUMERATOR
            sb.setLength(0);    // Clear string builder
            for (int i = 0; i < axisGearNumerator.size(); i++) {
                sb.append(axisGearNumerator.get(i));
                if (i < axisGearNumerator.size() - 1) sb.append(",");
            }
            newConfigLines.add("AXIS_GEAR_NUMERATOR=" + sb.toString());

            // AXIS_GEAR_DENOMINATOR
            sb.setLength(0);    // Clear string builder
            for (int i = 0; i < axisGearDenominator.size(); i++) {
                sb.append(axisGearDenominator.get(i));
                if (i < axisGearDenominator.size() - 1) sb.append(",");
            }
            newConfigLines.add("AXIS_GEAR_DENOMINATOR=" + sb.toString());

            // AXIS_ACCTIME
            sb.setLength(0);    // Clear string builder
            for (int i = 0; i < axisAccTime.size(); i++) {
                sb.append(axisAccTime.get(i));
                if (i < axisAccTime.size() - 1) sb.append(",");
            }
            newConfigLines.add("AXIS_ACCTIME=" + sb.toString());

            // AXIS_MACCTIME
            sb.setLength(0);    // Clear string builder
            for (int i = 0; i < axisMAccTime.size(); i++) {
                sb.append(axisMAccTime.get(i));
                if (i < axisMAccTime.size() - 1) sb.append(",");
            }
            newConfigLines.add("AXIS_MACCTIME=" + sb.toString());

            // AXIS_LIMITP
            sb.setLength(0);    // Clear string builder
            for (int i = 0; i < axisLimitP.size(); i++) {
                sb.append(axisLimitP.get(i));
                if (i < axisLimitP.size() - 1) sb.append(",");
            }
            newConfigLines.add("AXIS_LIMITP=" + sb.toString());

            // AXIS_LIMITN
            sb.setLength(0);    // Clear string builder
            for (int i = 0; i < axisLimitN.size(); i++) {
                sb.append(axisLimitN.get(i));
                if (i < axisLimitN.size() - 1) sb.append(",");
            }
            newConfigLines.add("AXIS_LIMITN=" + sb.toString());

            // AXIS_MSPEED
            sb.setLength(0);    // Clear string builder
            for (int i = 0; i < axisMSpeed.size(); i++) {
                sb.append(axisMSpeed.get(i));
                if (i < axisMSpeed.size() - 1) sb.append(",");
            }
            newConfigLines.add("AXIS_MSPEED=" + sb.toString());

            // SERVO_ON_MODE
            sb.setLength(0);    // Clear string builder
            for (int i = 0; i < axisServoOnMode.size(); i++) {
                sb.append(axisServoOnMode.get(i));
                if (i < axisServoOnMode.size() - 1) sb.append(",");
            }
            newConfigLines.add("AXIS_SERVO_ON_MODE=" + sb.toString());

            // 5. ZERO POSITION
            newConfigLines.add("#Zero Position");
            newConfigLines.add("ZERO_POSITION=-37373572,5815287,-20864068");
            newConfigLines.add("ZERO_POSITION=-37373572,5815287,-20864068");
            newConfigLines.add("AXIS_JOG_LEVEL=25,25,25");

            // 6. LINK_PARAMETER
            newConfigLines.add("#Link parameter");

            // ROBOT_TYPE
            newConfigLines.add("ROBOT_TYPE=" + robotTypeDropDown.getSelectedItem().toString());

            // ROBOT_PARAM
            sb.setLength(0);    // Clear string builder
            for (int j = 0; j < linkLengthNum; j++) {
                sb.append(linkLengthEditText[j].getText().toString());
                if (j != linkLengthNum - 1) sb.append(",");
            }

            newConfigLines.add("ROBOT_PARAM=" + sb.toString());

            // ROBOT_HOME1
            sb.setLength(0);    // Clear string builder
            for (int i = 0; i < robotHome1.length; i++) {
                sb.append(robotHome1[i]);
                if (i < robotHome1.length - 1) sb.append(",");
            }

            newConfigLines.add("ROBOT_HOME1=" + sb.toString());

            // ROBOT_HOME2
            sb.setLength(0);    // Clear string builder
            for (int i = 0; i < robotHome2.length; i++) {
                sb.append(robotHome2[i]);
                if (i < robotHome2.length - 1) sb.append(",");
            }

            newConfigLines.add("ROBOT_HOME2=" + sb.toString());

            // 7. MOTION_PARAMETER
            newConfigLines.add("#Motion parameter");
            newConfigLines.add("M_LSPEED=" + manLinSpeed);
            newConfigLines.add("M_RSPEED=" + manRotSpeed);
            newConfigLines.add("M_ACCTIME=" + manAccTime);
            newConfigLines.add("MAX_ACCTIME=" + maxAccTime);
            newConfigLines.add("MAX_LSPEED=" + maxLinSpeed);
            newConfigLines.add("MAX_RSPEED=" + maxRotSpeed);
            newConfigLines.add("ACCURACY=" + accuracy);

            // 8. PLC_PARAMETER
            newConfigLines.add("#PLC parameter");
            newConfigLines.add("PLC_ADDRESS=192.168.10.39");
            newConfigLines.add("PLC_PORT=2000");
            newConfigLines.add("PLC_TIMEOUT=1000");

            // 9. RC PARAMETER
            newConfigLines.add("#RC parameter");
            newConfigLines.add("RC_ADDRESS=192.168.10.20");
            newConfigLines.add("RC_PORT=2000");
            newConfigLines.add("RC_TIMEOUT=1000");

            // 10. SYSTEM I/O
            newConfigLines.add("#System I/O");
            newConfigLines.add("INPUT_1=" + systemInput1);
            newConfigLines.add("INPUT_2=" + systemInput2);
            newConfigLines.add("OUTPUT_TYPE=" + systemOutputType);
            newConfigLines.add("OUTPUT_1=" + systemOutput1);
            newConfigLines.add("OUTPUT_2=" + systemOutput2);

            File textFile = FileManager.createTempTextFile(this.requireContext(), TextUtils.join("\n", newConfigLines), "Anyname");
            FileManager.writeToDir(this.requireContext(), CUSTOM_CONTROLLER_CONFIG_FOLDER_PATH, CUSTOM_CONTROLLER_CONFIG_FILE_NAME, textFile);
            Toast.makeText(this.requireContext(), "Saved robot config", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.i(TAG, "Error saving ControllerConfig.txt:" + e.getMessage());
        }
    }

    private void sendConfigToController() {
        boolean isTCPAvailable = false;

        ConcurrentHashMap<String, TCPClient> activeConnections = GlobalData.getInstance().getActiveConnections();
        TCPClient tcpClient = activeConnections.get(activeConnectionKey);

        // TCP (Prioritized)
        PriorityCommunication.ChannelType channelType = PriorityCommunication.checkAvailableChannel();
        if (channelType == PriorityCommunication.ChannelType.TCP) {
            Log.i(TAG, "TCP available! Sending...");
            isTCPAvailable = true;
        }
        // Bluetooth
        else if (channelType == PriorityCommunication.ChannelType.Bluetooth) {
            Log.i(TAG, "Bluetooth available! Sending...");
            isTCPAvailable = false;
        } else {
            showToast("No server available", false);
            return;
        }

        ErrorDialog errorDialog = new ErrorDialog(this.getContext(), "");
        InfoDialog infoDialog = new InfoDialog(this.requireContext(), "");

        int partQuantity = 0;
        try {
            // Evaluate file size
            String configFilePath = CUSTOM_CONTROLLER_CONFIG_FOLDER_PATH + CUSTOM_CONTROLLER_CONFIG_FILE_NAME;
            byte[] configFileBytes = readInternalFile(this.requireContext(), configFilePath);
            partQuantity = (configFileBytes.length / 200) + 1;

            // Part quantity > 1
            // Send normal part
            AtomicBoolean shouldStop = new AtomicBoolean(false);
            for (int i = 0; i <= partQuantity - 2; i++) {
                if (shouldStop.get()) {
                    return;
                }

                if (isTCPAvailable) {
                    tcpClient.sendConfigPart(i, CUSTOM_CONTROLLER_CONFIG_FILE_NAME, this.requireContext(), true);
                }
                else {
                    bluetoothHelper.sendConfigPart(globalSelectedBTDevice, i, CUSTOM_CONTROLLER_CONFIG_FILE_NAME, this.requireContext(), true);
                }

//                int currentPartIndex = i;
//                handler.postDelayed(() -> {
//                    if (sendProgramPartACK != currentPartIndex) {
//                        shouldStop.set(true);   // Flag stop sending if ACK signal is wrong
//                        errorDialog.setInfoHint("Error ACK sending configurations to controller");
//                        errorDialog.showNumpadPopup(this.requireActivity());
//                    }
//
//                }, 100);

                long delayMillis = 30;
                long nextTime = System.currentTimeMillis() + delayMillis;

                for (int j = 0; j < 4; j++) {
                    while (System.currentTimeMillis() < nextTime) {

                    }
                    nextTime += delayMillis;
                }
            }

            if (shouldStop.get()) {
                Toast.makeText(this.requireContext(), "Sending configurations aborted: Wrong ACK signal", Toast.LENGTH_SHORT).show();
                return;
            }

            // Send ended part
            if (isTCPAvailable) {
                tcpClient.sendConfigEndPart(partQuantity - 1, CUSTOM_CONTROLLER_CONFIG_FILE_NAME, this.requireContext(), true, "miscellaneous");
            }
            else {
                bluetoothHelper.sendConfigEndPart(globalSelectedBTDevice, partQuantity - 1, CUSTOM_CONTROLLER_CONFIG_FILE_NAME, this.requireContext(), true, "miscellaneous");
            }

            int tempPartQuantity = partQuantity;
            handler.postDelayed(() -> {
                if (sendProgramEndPartACK == tempPartQuantity - 1) {
                    infoDialog.setInfoHint("Sent configurations to controller");
                    infoDialog.showNumpadPopup(this.requireActivity());
                } else {
                    errorDialog.setInfoHint("Error ACK sending configurations to controller");
                    errorDialog.showNumpadPopup(this.requireActivity());
                }

            }, 1000);

        } catch (Exception e) {
            showToast("Error sending configurations: " + e.getMessage(), false);
        }
    }

    private void showToast(String message, boolean longToast) {
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(this.requireContext(), message, longToast ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show()
        );
    }
}