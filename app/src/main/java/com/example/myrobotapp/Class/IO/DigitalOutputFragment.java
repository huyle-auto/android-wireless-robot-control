package com.example.myrobotapp.Class.IO;

import static com.example.myrobotapp.Class.ProtocolReceive.outputState;
import static com.example.myrobotapp.Fragments.Features.HistoryFragment.recordOperation;
import static com.example.myrobotapp.Fragments.Features.Settings.AxisSettingFragment.numAxis;
import static com.example.myrobotapp.Fragments.Features.Settings.ConnectionFragment.activeConnectionKey;
import static com.example.myrobotapp.Fragments.Features.Settings.ConnectionFragment.bluetoothHelper;
import static com.example.myrobotapp.Fragments.Features.Settings.ConnectionFragment.globalSelectedBTDevice;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myrobotapp.Class.Communication.PriorityCommunication;
import com.example.myrobotapp.Class.GlobalData;
import com.example.myrobotapp.Class.TCP.TCPClient;
import com.example.myrobotapp.Fragments.Features.Settings.ConnectionFragment;
import com.example.myrobotapp.Fragments.Features.Settings.RobotTypeFragment;
import com.example.myrobotapp.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DigitalOutputFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DigitalOutputFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public DigitalOutputFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DigitalOutputFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DigitalOutputFragment newInstance(String param1, String param2) {
        DigitalOutputFragment fragment = new DigitalOutputFragment();
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

    private static final String TAG = "DigitalOutputFragment";

    List<CheckBox> groupCheckBoxes;
    CheckBox currentlyChecked = null;
    List<SwitchCompat> bitToggles;
    int[] groupCheckBoxIds, bitToggleIds, doStatusIds;
    List<ImageView> doStatusBits;

    String currentChosenGroup = "01";
    TextView chosenGroupTextView, groupValueDecimal, groupValueHex;

    Handler handler = new Handler();
    boolean[] bitOutputArray = new boolean[32];
    boolean[] currentDoGroupState = new boolean[8];

    private List<ImageView[]> imageViewGroups;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_digital_output, container, false);

        groupCheckBoxes = new ArrayList<>();
        bitToggles = new ArrayList<>();
        doStatusBits = new ArrayList<>();

        groupCheckBoxIds = new int[]{R.id.DO_group01_CheckBox, R.id.DO_group02_CheckBox, R.id.DO_group03_CheckBox,
                R.id.DO_group04_CheckBox, R.id.DO_group05_CheckBox, R.id.DO_group06_CheckBox, R.id.DO_group07_CheckBox,
                R.id.DO_group08_CheckBox, R.id.DO_group09_CheckBox, R.id.DO_group10_CheckBox};

        bitToggleIds = new int[]{R.id.DO_Toggle_Bit_0, R.id.DO_Toggle_Bit_1, R.id.DO_Toggle_Bit_2,
                R.id.DO_Toggle_Bit_3, R.id.DO_Toggle_Bit_4, R.id.DO_Toggle_Bit_5,
                R.id.DO_Toggle_Bit_6, R.id.DO_Toggle_Bit_7};

        doStatusIds = new int[]{R.id.DO_status_Bit_0, R.id.DO_status_Bit_1, R.id.DO_status_Bit_2, R.id.DO_status_Bit_3,
                R.id.DO_status_Bit_4, R.id.DO_status_Bit_5, R.id.DO_status_Bit_6, R.id.DO_status_Bit_7};

        initGroups(view);
        initBitToggles(view);
        initDoStatus(view);
        initAllGroupStatus(view);

        chosenGroupTextView = view.findViewById(R.id.chosenGroupTextView);
        groupValueDecimal = view.findViewById(R.id.groupValueDecimal);
        groupValueHex = view.findViewById(R.id.groupValueHex);

        updateRegularDoValue();

        return view;
    }

    private void initGroups(View view) {
        // Assign group check boxes
        for (int groupCheckBoxId : groupCheckBoxIds) {
            CheckBox checkBox = view.findViewById(groupCheckBoxId);
            groupCheckBoxes.add(checkBox);
        }

        // Initially choose group 1
        groupCheckBoxes.get(0).setChecked(true);
        currentlyChecked = groupCheckBoxes.get(0);

        // When a group is chosen, uncheck all other groups
        // Define a listener for all checkboxes
        CompoundButton.OnCheckedChangeListener listener = (buttonView, isChecked) -> {
            if (isChecked) {
                // Uncheck all other checkboxes
                for (CheckBox cb : groupCheckBoxes) {
                    if (cb != buttonView) {
                        cb.setChecked(false);
                    }
                }
                // Update the currently checked CheckBox
                currentlyChecked = (CheckBox) buttonView;
            } else {
                // Prevent unchecking the currently checked CheckBox
                if (currentlyChecked == buttonView) {
                    buttonView.setChecked(true);
                }
            }

            // Update the current chosen group value
            currentChosenGroup = currentlyChecked.getTag().toString().replaceAll("\\D+", "");
            chosenGroupTextView.setText("GROUP: " + currentChosenGroup);

            // Update actual toggle state every time user change group
            // updateActualToggleState();

        };

        // Attach the listener to each checkbox
        for (CheckBox checkBox : groupCheckBoxes) {
            checkBox.setOnCheckedChangeListener(listener);
        }
    }

    private void initBitToggles(View view) {
        // Assign bit toggle switches
        for (int bitToggleId : bitToggleIds) {
            SwitchCompat switchCompat = view.findViewById(bitToggleId);
            bitToggles.add(switchCompat);
        }

        for (SwitchCompat bitToggle : bitToggles) {
            bitToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
                ConcurrentHashMap<String, TCPClient> activeConnections = GlobalData.getInstance().getActiveConnections();
                TCPClient tcpClient = activeConnections.get(activeConnectionKey);

                if (tcpClient == null && globalSelectedBTDevice == null) {
                    Toast.makeText(this.requireContext(), "Server not found", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Get the current group index
                int groupIndex = Integer.parseInt(currentChosenGroup);

                // Get the toggle's bit index (based on group and switch ID/tag)
                String bitToggleTag = getResources().getResourceEntryName(bitToggle.getId());
                int bitIndex = Integer.parseInt(bitToggleTag.replaceAll("\\D+", ""));
                bitIndex += 8 * (groupIndex - 1); // Adjust for group offset

                // Update the boolean array for the corresponding bit
                bitOutputArray[bitIndex] = isChecked;

                // Calculate the output value for the entire 32-bit array
                int outputValue = 0;
                for (int i = 0; i < bitOutputArray.length; i++) {
                    if (bitOutputArray[i]) {
                        outputValue += 1 << i; // Shift according to the bit's actual index
                    }
                }

                // Update UI and send data to the server
                groupValueDecimal.setText("VALUE (DEC): " + outputValue);
                groupValueHex.setText("VALUE (HEX): " + Integer.toHexString(outputValue).toUpperCase());

                // Send
                // TCP (Prioritized)
                PriorityCommunication.ChannelType channelType = PriorityCommunication.checkAvailableChannel();
                if (channelType == PriorityCommunication.ChannelType.TCP) {
                    tcpClient.setOutputValue(0, outputValue);
                    return;
                }

                // Bluetooth
                if (channelType == PriorityCommunication.ChannelType.Bluetooth) {
                    bluetoothHelper.setOutputValue(globalSelectedBTDevice,0, outputValue);
                    return;
                }

                showToast("No server available", false);
            });
        }

    }

    private void initDoStatus(View view) {
        for (int imageViewId : doStatusIds) {
            ImageView imageView = view.findViewById(imageViewId);
            doStatusBits.add(imageView);
        }
    }

    private void initAllGroupStatus(View view) {
        // Initialize the list
        imageViewGroups = new ArrayList<>();

        // Get the GridLayout
        GridLayout gridLayout = view.findViewById(R.id.doGridLayout); // Replace with your actual GridLayout ID

        // Initialize the groups
        for (int i = 0; i < 10; i++) { // Assuming 10 groups
            imageViewGroups.add(new ImageView[8]); // 8 bits per group
        }

        // Loop through the children of the GridLayout
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            View child = gridLayout.getChildAt(i);
            if (child instanceof ImageView) {
                ImageView imageView = (ImageView) child;

                // Extract the group number and bit index from the ID
                String resourceName = getResources().getResourceEntryName(imageView.getId());
                String[] parts = resourceName.split("_");

                if (parts.length == 3 && parts[0].equals("DO")) {
                    int groupNumber = Integer.parseInt(parts[1].substring(parts[1].length() - 2)); // Extract group number
                    int bitIndex = Integer.parseInt(parts[2].substring(parts[2].length() - 1)); // Extract bit index

                    // Store the ImageView in the corresponding group and bit index
                    imageViewGroups.get(groupNumber - 1)[bitIndex] = imageView; // Adjusting for zero-based index
                }
            }
        }

        updateAllGroupDoValue();
    }

    private void updateAllGroupDoValue() {
        for (int group = 1; group <= 4; group++) { // Start from 1
            for (int bit = 0; bit < 8; bit++) {
                // Calculate the index in inputState
                int index = (group - 1) * 8 + bit; // Adjust for zero-based index

                // Check if the index is within the valid range of inputState
                if (index >= 0 && index < outputState.length) {
                    // Get the corresponding ImageView
                    ImageView imageView = imageViewGroups.get(group - 1)[bit]; // Adjust for zero-based index
                    if (imageView != null) {
                        // Update the ImageView based on the inputState
                        if (outputState[index]) {
                            // Set the ImageView to represent a "true" state
                            imageView.setImageTintList(ColorStateList.valueOf(Color.parseColor("#32CD32"))); // Replace with your true image
                        } else {
                            // Set the ImageView to represent a "false" state
                            imageView.setImageTintList(ColorStateList.valueOf(Color.parseColor("#A9A9A9"))); // Replace with your false image
                        }
                    }
                } else {
                    // Optionally handle out-of-range cases
                    // Log or print a message if needed
                    System.out.println("Index out of range: " + index);
                }
            }
        }
    }

    private void updateRegularDoValue() {
        // handler = new Handler();
        // Update current chosen group
        // Constantly check for compile result
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Repetitively check for state and set colors for status bits

                // Extract the current group bits then assign to currentDoGroupState then set ImageView color
                int startIndex = (Integer.parseInt(currentChosenGroup) - 1) * 8;
                // int endIndex = Integer.parseInt(currentChosenGroup) * 8 - 1;

                // Currently does not work with group index > 4 --> outputState has only 32 bits
                if (startIndex < 32) {
                    System.arraycopy(outputState, startIndex, currentDoGroupState, 0, 8);
                }

                for (int i = 0; i < doStatusBits.size(); i++) {
                    if (currentDoGroupState[i]) {
//                        int onColor = ContextCompat.getColor(requireContext(), R.color.lime);
//                        doStatusBits.get(i).setColorFilter(onColor);
                        doStatusBits.get(i).setImageTintList(ColorStateList.valueOf(Color.parseColor("#32CD32")));
                    }
                    else {
//                        int offColor = ContextCompat.getColor(requireContext(), R.color.lime);
//                        doStatusBits.get(i).setColorFilter(offColor);
                        doStatusBits.get(i).setImageTintList(ColorStateList.valueOf(Color.parseColor("#A9A9A9")));
                    }
                }

                new Handler(Looper.getMainLooper()).post(() -> updateAllGroupDoValue());

                // Schedule the next execution
                handler.postDelayed(this, 400);
            }
        }, 400);
    }

    private void updateActualToggleState() {
        int startIndex = (Integer.parseInt(currentChosenGroup) - 1) * 8;
        // int endIndex = Integer.parseInt(currentChosenGroup) * 8 - 1;

        System.arraycopy(outputState, startIndex, currentDoGroupState, 0, 8);

        // Update right-hand side toggles state
        for (int i = 0; i < bitToggles.size(); i++) {
            bitToggles.get(i).setChecked(currentDoGroupState[i]);
        }
    }

    private void showToast(String message, boolean longToast) {

        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(this.requireContext(), message, longToast ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show()
        );
    }
}