package com.example.myrobotapp.Fragments.Features;


import static com.example.myrobotapp.Class.ProtocolReceive.axis1Pos;
import static com.example.myrobotapp.Class.ProtocolReceive.axis2Pos;
import static com.example.myrobotapp.Class.ProtocolReceive.axis3Pos;
import static com.example.myrobotapp.Class.ProtocolReceive.axis4Pos;

import static com.example.myrobotapp.Class.ProtocolReceive.axis5Pos;
import static com.example.myrobotapp.Class.ProtocolReceive.axis6Pos;
import static com.example.myrobotapp.Class.ProtocolReceive.joint1Pos;
import static com.example.myrobotapp.Class.ProtocolReceive.joint2Pos;
import static com.example.myrobotapp.Class.ProtocolReceive.joint3Pos;
import static com.example.myrobotapp.Class.ProtocolReceive.joint4Pos;
import static com.example.myrobotapp.Class.ProtocolReceive.joint5Pos;
import static com.example.myrobotapp.Class.ProtocolReceive.joint6Pos;
import static com.example.myrobotapp.Class.ProtocolReceive.rX;
import static com.example.myrobotapp.Class.ProtocolReceive.rY;
import static com.example.myrobotapp.Class.ProtocolReceive.rZ;
import static com.example.myrobotapp.Class.ProtocolReceive.xPos;
import static com.example.myrobotapp.Class.ProtocolReceive.yPos;
import static com.example.myrobotapp.Class.ProtocolReceive.zPos;
import static com.example.myrobotapp.Fragments.Features.DataFragment.jointDataSharedViewModel;
import static com.example.myrobotapp.Fragments.Features.HistoryFragment.recordOperation;
import static com.example.myrobotapp.Fragments.Features.Settings.AxisSettingFragment.numAxis;
import static com.example.myrobotapp.Fragments.Features.Settings.ConnectionFragment.activeConnectionKey;
import static com.example.myrobotapp.Fragments.Features.Settings.ConnectionFragment.globalSelectedBTDevice;
import static com.example.myrobotapp.HomeScreenActivity.speedLevel;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.myrobotapp.Class.Communication.PriorityCommunication;
import com.example.myrobotapp.Class.DataTable.Joint.JointDataSharedViewModel;
import com.example.myrobotapp.Class.DataTable.Joint.JointDataTableRowModel;
import com.example.myrobotapp.Class.DataTable.World.WorldDataSharedViewModel;
import com.example.myrobotapp.Class.DataTable.World.WorldDataTableRowModel;
import com.example.myrobotapp.Class.GlobalData;
import com.example.myrobotapp.Class.Privilege.PrivilegeManager;
import com.example.myrobotapp.Class.Privilege.SessionManager;
import com.example.myrobotapp.Class.TCP.TCPClient;
import com.example.myrobotapp.Class.UserInput.NumpadPopup;
import com.example.myrobotapp.Fragments.Features.Settings.ConnectionFragment;
import com.example.myrobotapp.Fragments.Features.Settings.RobotTypeFragment;
import com.example.myrobotapp.R;

import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link JogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class JogFragment extends Fragment implements SessionManager.RoleChangeListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public JogFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment JogFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static JogFragment newInstance(String param1, String param2) {
        JogFragment fragment = new JogFragment();
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

    public static final String TAG = "JogFragment";

    EditText x_value, y_value, z_value, rX_value, rY_value, rZ_value;
    Button saveJJ_Button, gotoJointPosButton;
    EditText j1_value, j2_value, j3_value, j4_value, j5_value, j6_value;
    Button saveLJ_Button, gotoWorldPosButton;
    Button saveAxesZeroValueButton;
    Button moveToHome1Button, moveToHome2Button;
    Button saveJointHome1Button, saveJointHome2Button;
    Button saveWorldHome1Button, saveWorldHome2Button;  // currently not supported


    // Define a Handler and Runnable for repeating actions
    private Handler handler = new Handler();
    private Runnable updatePositionTask;
    private final int UPDATE_POS_CYCLE = 200;
    DecimalFormat decimalFormat = new DecimalFormat("0.00", new DecimalFormatSymbols(Locale.US));
    Button liveModeButton, editModeButton;

    public interface JogModeChangeListener {
        void onLiveModeEntered();
        void onEditModeEntered();
    }
    private JogModeChangeListener jogModeChangeListener;
    View manualJointJogOverlayBlocker, manualLinearJogOverlayBlocker;
    public static String[] zeroPosition;


    private WorldDataSharedViewModel worldDataSharedViewModel;
    private int worldPointRowCount;

    // Set the repeat time in milliseconds
    private static final long REPEAT_DELAY = 200; // Adjust this for the desired repeat interval

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_jog, container, false);
        worldDataSharedViewModel = new ViewModelProvider(requireActivity()).get(WorldDataSharedViewModel.class);

        // Assign point value and corresponding Save Button
        // JOINT
        j1_value = view.findViewById(R.id.joint1_value);
        j1_value.setOnClickListener(v -> {
            NumpadPopup numpadPopup = new NumpadPopup(this.requireActivity(), j1_value, 1);
            numpadPopup.showNumpadPopup(this.requireActivity());
        });
        j2_value = view.findViewById(R.id.joint2_value);
        j2_value.setOnClickListener(v -> {
            NumpadPopup numpadPopup = new NumpadPopup(this.requireActivity(), j2_value, 1);
            numpadPopup.showNumpadPopup(this.requireActivity());
        });
        j3_value = view.findViewById(R.id.joint3_value);
        j3_value.setOnClickListener(v -> {
            NumpadPopup numpadPopup = new NumpadPopup(this.requireActivity(), j3_value, 1);
            numpadPopup.showNumpadPopup(this.requireActivity());
        });
        j4_value = view.findViewById(R.id.joint4_value);
        j4_value.setOnClickListener(v -> {
            NumpadPopup numpadPopup = new NumpadPopup(this.requireActivity(), j4_value, 1);
            numpadPopup.showNumpadPopup(this.requireActivity());
        });
        j5_value = view.findViewById(R.id.joint5_value);
        j5_value.setOnClickListener(v -> {
            NumpadPopup numpadPopup = new NumpadPopup(this.requireActivity(), j5_value, 1);
            numpadPopup.showNumpadPopup(this.requireActivity());
        });
        j6_value = view.findViewById(R.id.joint6_value);
        j6_value.setOnClickListener(v -> {
            NumpadPopup numpadPopup = new NumpadPopup(this.requireActivity(), j6_value, 1);
            numpadPopup.showNumpadPopup(this.requireActivity());
        });

        gotoJointPosButton = view.findViewById(R.id.gotoJointPosButton);
        gotoJointPosButton.setOnClickListener(v -> {

            // Show World Point List
            worldDataSharedViewModel = new ViewModelProvider(requireActivity()).get(WorldDataSharedViewModel.class);
            worldDataSharedViewModel.getInputWorldData().observe(this.requireActivity(), data -> {
                // Extract each element
                if (data != null) {
                    System.out.println("World Point List:");
                    for (WorldDataTableRowModel item : data) {
                        // Process each item
                        // For example, concatenate to display in TextView
                        System.out.print(item.getX()+","+ item.getY()+ ","+ item.getZ()+"," + item.getRx()+","+ item.getRy()+"," +item.getRz()); // Display each item in a new line
                        System.out.print("\n");
                    }
                }
            });

            // Show Joint Point List
            jointDataSharedViewModel = new ViewModelProvider(requireActivity()).get(JointDataSharedViewModel.class);
            jointDataSharedViewModel.getInputJointData().observe(this.requireActivity(), data -> {
                // Extract each element
                if (data != null) {
                    System.out.println("Joint Point List:");
                    for (JointDataTableRowModel item : data) {
                        // Process each item
                        // For example, concatenate to display in TextView
                        System.out.print(item.getJ1()+","+ item.getJ2()+ ","+ item.getJ3()+"," + item.getJ4()+","+ item.getJ5()+"," +item.getJ6()); // Display each item in a new line
                        System.out.print("\n");
                    }
                }
            });

            moveToJointPos();
        });

        saveJJ_Button = view.findViewById(R.id.saveJJ_Button);
        saveJJ_Button.setOnClickListener(v -> {
            saveJointPoint();
        });

        // WORLD
        x_value = view.findViewById(R.id.x_value);
        x_value.setOnClickListener(v -> {
            NumpadPopup numpadPopup = new NumpadPopup(this.requireActivity(), x_value, 1);
            numpadPopup.showNumpadPopup(this.requireActivity());
        });
        y_value = view.findViewById(R.id.y_value);
        y_value.setOnClickListener(v -> {
            NumpadPopup numpadPopup = new NumpadPopup(this.requireActivity(), y_value, 1);
            numpadPopup.showNumpadPopup(this.requireActivity());
        });
        z_value = view.findViewById(R.id.z_value);
        z_value.setOnClickListener(v -> {
            NumpadPopup numpadPopup = new NumpadPopup(this.requireActivity(), z_value, 1);
            numpadPopup.showNumpadPopup(this.requireActivity());
        });
        rX_value = view.findViewById(R.id.rX_value);
        rX_value.setOnClickListener(v -> {
            NumpadPopup numpadPopup = new NumpadPopup(this.requireActivity(), rX_value, 1);
            numpadPopup.showNumpadPopup(this.requireActivity());
        });
        rY_value = view.findViewById(R.id.rY_value);
        rY_value.setOnClickListener(v -> {
            NumpadPopup numpadPopup = new NumpadPopup(this.requireActivity(), rY_value, 1);
            numpadPopup.showNumpadPopup(this.requireActivity());
        });
        rZ_value = view.findViewById(R.id.rZ_value);
        rZ_value.setOnClickListener(v -> {
            NumpadPopup numpadPopup = new NumpadPopup(this.requireActivity(), rZ_value, 1);
            numpadPopup.showNumpadPopup(this.requireActivity());
        });
        gotoWorldPosButton = view.findViewById(R.id.gotoWorldPosButton);
        gotoWorldPosButton.setOnClickListener(v -> moveToWorldPos());
        saveLJ_Button = view.findViewById(R.id.saveLJ_Button);
        saveLJ_Button.setOnClickListener(v -> {
            saveWorldPoint();
        });

        saveAxesZeroValueButton = view.findViewById(R.id.saveAxesZeroValueButton);
        saveAxesZeroValueButton.setOnClickListener(v -> saveAxesZeroValue());

        moveToHome1Button = view.findViewById(R.id.moveToHome1Button);
        moveToHome1Button.setOnClickListener(v -> moveToHome(moveToHome1Button));

        moveToHome2Button = view.findViewById(R.id.moveToHome2Button);
        moveToHome2Button.setOnClickListener(v -> moveToHome(moveToHome2Button));

        saveJointHome1Button = view.findViewById(R.id.saveJointHome1Button);
        saveJointHome1Button.setOnClickListener(v -> saveJointHomePosition());

        saveJointHome2Button = view.findViewById(R.id.saveJointHome2Button);
        saveJointHome2Button.setOnClickListener(v -> saveJointHomePosition());

        // Overlay blocker
        manualJointJogOverlayBlocker = view.findViewById(R.id.manualJointJogOverlayBlocker);
        manualLinearJogOverlayBlocker = view.findViewById(R.id.manualLinearJogOverlayBlocker);

        // JOG MODE
        liveModeButton = view.findViewById(R.id.liveModeButton);
        liveModeButton.setOnClickListener(v -> {
            // Toast
            showToast("LIVE MODE enabled", false);

            // Change button highlight
            liveModeButton.setBackgroundColor(Color.parseColor("#1496BB"));
            editModeButton.setBackgroundColor(Color.parseColor("#778899"));

            // Enable position update task
            startUpdatePosition();

            // Disable position boxes
            manualJointJogOverlayBlocker.setVisibility(View.VISIBLE);
            manualLinearJogOverlayBlocker.setVisibility(View.VISIBLE);

            // Notify listener
            jogModeChangeListener.onLiveModeEntered();
        });

        editModeButton = view.findViewById(R.id.editModeButton);
        editModeButton.setOnClickListener(v -> {
            // Toast
            showToast("EDIT MODE enabled", false);

            // Change button highlight
            liveModeButton.setBackgroundColor(Color.parseColor("#778899"));
            editModeButton.setBackgroundColor(Color.parseColor("#1496BB"));

            // Disable position update task
            stopUpdatePosition();

            // Enable position boxes
            manualJointJogOverlayBlocker.setVisibility(View.GONE);
            manualLinearJogOverlayBlocker.setVisibility(View.GONE);

            // Notify listener
            jogModeChangeListener.onEditModeEntered();
        });

        createUpdatePositionTask();
        startUpdatePosition();

        return view;
    }

    private void saveWorldPoint(){
        List<String> worldPosFields = new ArrayList<>();
        worldPosFields.add(x_value.getText().toString());
        worldPosFields.add(y_value.getText().toString());
        worldPosFields.add(z_value.getText().toString());
        worldPosFields.add(rX_value.getText().toString());
        worldPosFields.add(rY_value.getText().toString());
        worldPosFields.add(rZ_value.getText().toString());

        if (!containAllNumbers(worldPosFields)) {   // Avoid error converting when fields are "Not updated"
            showToast("Position(s) may contain invalid value", false);
            return;
        }

        if(x_value.getText().toString().isEmpty() ||
                y_value.getText().toString().isEmpty() ||
                z_value.getText().toString().isEmpty() ||
                rX_value.getText().toString().isEmpty() ||
                rY_value.getText().toString().isEmpty() ||
                rZ_value.getText().toString().isEmpty()){
            Toast.makeText(this.getActivity(), "Cannot save empty parameters(s) point", Toast.LENGTH_SHORT).show();
            return;
        }
        WorldDataTableRowModel data = new WorldDataTableRowModel("",
                                                        x_value.getText().toString(),
                                                        y_value.getText().toString(),
                                                        z_value.getText().toString(),
                                                        rX_value.getText().toString(),
                                                        rY_value.getText().toString(),
                                                        rZ_value.getText().toString()
                                                                                        );
        worldDataSharedViewModel.addWorldData(data);
    }

    private void saveJointPoint() {
        List<String> jointPosFields = new ArrayList<>();
        jointPosFields.add(j1_value.getText().toString());
        jointPosFields.add(j2_value.getText().toString());
        jointPosFields.add(j3_value.getText().toString());
        jointPosFields.add(j4_value.getText().toString());
        jointPosFields.add(j5_value.getText().toString());
        jointPosFields.add(j6_value.getText().toString());

        if (!containAllNumbers(jointPosFields)) {   // Avoid error converting when fields are "Not updated"
            showToast("Position(s) may contain invalid value", false);
            return;
        }

        if (j1_value.getText().toString().isEmpty() ||
                j2_value.getText().toString().isEmpty() ||
                j3_value.getText().toString().isEmpty() ||
                j4_value.getText().toString().isEmpty() ||
                j5_value.getText().toString().isEmpty() ||
                j6_value.getText().toString().isEmpty()) {
            Toast.makeText(this.getActivity(), "Cannot save empty parameters(s) point", Toast.LENGTH_SHORT).show();
            return;
        }

        JointDataTableRowModel data = new JointDataTableRowModel("",
                                                                j1_value.getText().toString(),
                                                                j2_value.getText().toString(),
                                                                j3_value.getText().toString(),
                                                                j4_value.getText().toString(),
                                                                j5_value.getText().toString(),
                                                                j6_value.getText().toString()
        );
        jointDataSharedViewModel.addJointData(data);
    }

    private void moveToJointPos() {
        List<String> jointPosFields = new ArrayList<>();
        jointPosFields.add(j1_value.getText().toString());
        jointPosFields.add(j2_value.getText().toString());
        jointPosFields.add(j3_value.getText().toString());
        jointPosFields.add(j4_value.getText().toString());
        jointPosFields.add(j5_value.getText().toString());
        jointPosFields.add(j6_value.getText().toString());

        if (!containAllNumbers(jointPosFields)) {   // Avoid error converting when fields are "Not updated"
            showToast("Please update Robot position", false);
            return;
        }

        ConcurrentHashMap<String, TCPClient> activeConnections = GlobalData.getInstance().getActiveConnections();
        TCPClient tcpClient = activeConnections.get(activeConnectionKey);

        if (tcpClient == null){
            Toast.makeText(this.requireContext(), "Server not found", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] jointPosition = {j1_value.getText().toString(),
                                    j2_value.getText().toString(),
                                        j3_value.getText().toString(),
                                            j4_value.getText().toString(),
                                                j5_value.getText().toString(),
                                                    j6_value.getText().toString()
        };

        tcpClient.moveToPosition(1, speedLevel, jointPosition);
        Toast.makeText(this.requireContext(), "Requested robot move to JOINT position", Toast.LENGTH_SHORT).show();
    }   // Haven't added priority

    private void moveToWorldPos() {
        List<String> worldPosFields = new ArrayList<>();
        worldPosFields.add(x_value.getText().toString());
        worldPosFields.add(y_value.getText().toString());
        worldPosFields.add(z_value.getText().toString());
        worldPosFields.add(rX_value.getText().toString());
        worldPosFields.add(rY_value.getText().toString());
        worldPosFields.add(rZ_value.getText().toString());

        if (!containAllNumbers(worldPosFields)) {   // Avoid error converting when fields are "Not updated"
            showToast("Please update Robot position", false);
            return;
        }

        ConcurrentHashMap<String, TCPClient> activeConnections = GlobalData.getInstance().getActiveConnections();
        TCPClient tcpClient = activeConnections.get(activeConnectionKey);

        if (tcpClient == null){
            Toast.makeText(this.requireContext(), "Server not found", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] worldPosition = {x_value.getText().toString(),
                                    y_value.getText().toString(),
                                        z_value.getText().toString(),
                                            rX_value.getText().toString(),
                                                rY_value.getText().toString(),
                                                    rZ_value.getText().toString()
        };

        tcpClient.moveToPosition(2, speedLevel, worldPosition);
        Toast.makeText(this.requireContext(), "Requested robot move to WORLD position", Toast.LENGTH_SHORT).show();
    }   // Haven't added priority

    private void saveAxesZeroValue () {
        ConcurrentHashMap<String, TCPClient> activeConnections = GlobalData.getInstance().getActiveConnections();
        TCPClient tcpClient = activeConnections.get(activeConnectionKey);

        if (tcpClient == null){
            Toast.makeText(this.requireContext(), "Server not found", Toast.LENGTH_SHORT).show();
            return;
        }
        int[] axesZeroValues = {Integer.parseInt(axis1Pos),
                                    Integer.parseInt(axis2Pos),
                                        Integer.parseInt(axis3Pos)
//                                            Integer.parseInt(axis4Pos),
//                                                Integer.parseInt(axis5Pos),
//                                                    Integer.parseInt(axis6Pos)
        };

//        List<Integer> axesZeroValues = new ArrayList<>();
//        axesZeroValues.add(Integer.parseInt(axis1Pos);
//        axesZeroValues.add(Integer.parseInt(axis2Pos);
//        axesZeroValues.add(Integer.parseInt(axis3Pos);
//        axesZeroValues.add(Integer.parseInt(axis4Pos);
//        axesZeroValues.add(Integer.parseInt(axis5Pos);
//        axesZeroValues.add(Integer.parseInt(axis6Pos);

        tcpClient.saveAxesZeroValue(3, axesZeroValues);
    }   // Hard-coded numAxis

    private void moveToHome(Button button) {
        String buttonText = button.getText().toString().replaceAll("\\D+", "");

        ConcurrentHashMap<String, TCPClient> activeConnections = GlobalData.getInstance().getActiveConnections();
        TCPClient tcpClient = activeConnections.get(activeConnectionKey);

        if (tcpClient == null && globalSelectedBTDevice == null) {
            Toast.makeText(this.requireContext(), "Server not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // TCP (Prioritized)
        PriorityCommunication.ChannelType channelType = PriorityCommunication.checkAvailableChannel();
        if (channelType == PriorityCommunication.ChannelType.TCP) {
            tcpClient.moveToHome(Integer.parseInt(buttonText) - 1, speedLevel);
            recordOperation("request MOVE TO HOME");
            return;
        }

        // Bluetooth
        if (channelType == PriorityCommunication.ChannelType.Bluetooth) {
            ConnectionFragment.bluetoothHelper.moveToHome(globalSelectedBTDevice, Integer.parseInt(buttonText) - 1, speedLevel);
            recordOperation("request MOVE TO HOME");
            return;
        }

        showToast("No server available", false);
    }   // Added priority

    private void saveJointHomePosition() {
        ConcurrentHashMap<String, TCPClient> activeConnections = GlobalData.getInstance().getActiveConnections();
        TCPClient tcpClient = activeConnections.get(activeConnectionKey);

        if (tcpClient == null && globalSelectedBTDevice == null) {
            Toast.makeText(this.requireContext(), "Server not found", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> jointPosFields = new ArrayList<>();
        jointPosFields.add(j1_value.getText().toString());
        jointPosFields.add(j2_value.getText().toString());
        jointPosFields.add(j3_value.getText().toString());
        jointPosFields.add(j4_value.getText().toString());
        jointPosFields.add(j5_value.getText().toString());
        jointPosFields.add(j6_value.getText().toString());

        if (!containAllNumbers(jointPosFields)) {   // Avoid error converting when fields are "Not updated"
            showToast("Please update Robot position", false);
            return;
        }

        List<String> home1Position = new ArrayList<>();
        home1Position.add(j1_value.getText().toString());
        home1Position.add(j2_value.getText().toString());
        home1Position.add(j3_value.getText().toString());
        home1Position.add(j4_value.getText().toString());
        home1Position.add(j5_value.getText().toString());
        home1Position.add(j6_value.getText().toString());
        String[] home1PositionArray = home1Position.subList(0, numAxis).toArray(new String[numAxis]);

        List<String> home2Position = new ArrayList<>();
        home2Position.add(j1_value.getText().toString());
        home2Position.add(j2_value.getText().toString());
        home2Position.add(j3_value.getText().toString());
        home2Position.add(j4_value.getText().toString());
        home2Position.add(j5_value.getText().toString());
        home2Position.add(j6_value.getText().toString());
        home2Position.subList(0, numAxis);
        String[] home2PositionArray = home1Position.subList(0, numAxis).toArray(new String[numAxis]);

        // TCP (Prioritized)
        PriorityCommunication.ChannelType channelType = PriorityCommunication.checkAvailableChannel();
        if (channelType == PriorityCommunication.ChannelType.TCP) {
            tcpClient.saveRobotHomeValue(numAxis, home1PositionArray, home2PositionArray);
            RobotTypeFragment.robotHome1 = home1PositionArray;
            recordOperation("request SAVE HOME positions");
            return;
        }

        // Bluetooth
        if (channelType == PriorityCommunication.ChannelType.Bluetooth) {
            ConnectionFragment.bluetoothHelper.saveRobotHomeValue(globalSelectedBTDevice, numAxis, home1PositionArray, home2PositionArray);
            RobotTypeFragment.robotHome2 = home2PositionArray;
            recordOperation("request SAVE HOME positions");
            return;
        }

        showToast("No server available", false);
    }   // Added priority

    private void createUpdatePositionTask() {
        updatePositionTask = new Runnable() {
            @Override
            public void run() {
                // Your repetitive code here
                // 1. Joint Point
                j1_value.setText(positionFormatter(joint1Pos));
                j2_value.setText(positionFormatter(joint2Pos));
                j3_value.setText(positionFormatter(joint3Pos));
                j4_value.setText(positionFormatter(joint4Pos));
                j5_value.setText(positionFormatter(joint5Pos));
                j6_value.setText(positionFormatter(joint6Pos));

                // 2. Linear Point
                x_value.setText(positionFormatter(xPos));
                y_value.setText(positionFormatter(yPos));
                z_value.setText(positionFormatter(zPos));
                rX_value.setText(positionFormatter(rX));
                rY_value.setText(positionFormatter(rY));
                rZ_value.setText(positionFormatter(rZ));

                // Schedule the next execution
                handler.postDelayed(updatePositionTask, UPDATE_POS_CYCLE);
            }
        };
    }

    private void startUpdatePosition() {
        handler.postDelayed(updatePositionTask, UPDATE_POS_CYCLE);
    }

    private void stopUpdatePosition() {
        handler.removeCallbacks(updatePositionTask);
    }

    private String positionFormatter(String pos) {
        if (pos != null && !pos.isEmpty()) {
            try {
                return decimalFormat.format(Float.parseFloat(pos));
            }
            catch (Exception e) {
                Log.i(TAG, "Error in formatting positions: " + e.getMessage());
            }
        }

        return "0.00";
    }

    private void updateUI(String role) {
        // Get the list of allowed fields for the current role
        List<String> unAllowedFields = PrivilegeManager.getUnAllowedFields(role, "JogFragment");

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
                    Log.i(TAG, "");
                }
            }
        }
    }

    private boolean containAllNumbers(List<String> fields) {
        for (String str : fields) {
            try {
                Float.parseFloat(str); // or Double.parseDouble(str)
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return true;
    }

    private void showToast(String message, boolean longToast) {

        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(this.requireContext(), message, longToast ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof JogModeChangeListener) {
            jogModeChangeListener = (JogModeChangeListener) context;
        } else {
            throw new ClassCastException(context + " must implement JogModeChangeListener");
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

        stopUpdatePosition();
    }
}