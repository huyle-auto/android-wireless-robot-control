package com.example.myrobotapp.Fragments.Features;

import android.os.Bundle;

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

import com.example.myrobotapp.Class.DataTable.History.Alarm.AlarmLogAdapter;
import com.example.myrobotapp.Class.DataTable.History.Alarm.AlarmLogEntryModel;
import com.example.myrobotapp.Class.DataTable.History.Operation.OperationLogAdapter;
import com.example.myrobotapp.Class.DataTable.History.LogManager;
import com.example.myrobotapp.Class.DataTable.History.LogViewModel;
import com.example.myrobotapp.Class.DataTable.World.WorldDataSharedViewModel;
import com.example.myrobotapp.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HistoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HistoryFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HistoryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HistoryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HistoryFragment newInstance(String param1, String param2) {
        HistoryFragment fragment = new HistoryFragment();
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

    public static final String TAG = "HistoryFragment";

    private static final long SAVE_INTERVAL = 60000;   // 1 minutes
    private Handler saveHandler = new Handler(Looper.getMainLooper());
    private Runnable saveRunnable = new Runnable() {
        @Override
        public void run() {
            logManager.saveLogsToFile();
            saveHandler.postDelayed(this, SAVE_INTERVAL); // Re-run the task periodically
        }
    };

    public static LogViewModel logViewModel;
    public static LogManager logManager;
    private OperationLogAdapter operationLogAdapter;
    private AlarmLogAdapter alarmLogAdapter;

    public enum ControllerAlarmCode {
        NETWORK_NOT_READY(0x0001, "Network Not Ready - Controller error"),
        AXIS_SLAVE_ERROR(0x0002, "Axis Slave Error - Communication fault"),
        NEAR_SINGULARITY(0x0003, "Near Singularity - Kinematic limit issue"),
        AXIS_LIMIT_REACHED(0x0004, "Axis Limit Reached - Physical joint limit hit"),
        AXIS_PARAMETER_ERROR(0x0005, "Axis Parameter Error - Invalid axis parameter setting"),
        MOTION_PARAMETER_ERROR(0x0006, "Motion Parameter Error - Invalid motion parameter setting"),
        INCREMENTAL_PARAMETER_ERROR(0x0007, "Incremental Parameter Error - Invalid incremental parameter"),
        DIVIDED_BY_ZERO(0x0008, "Divided by Zero Occurrence - Operation divided by zero"),
        INVALID_SQRT(0x0009, "Invalid SQRT Instruction - Cannot execute SQRT instruction"),
        OUT_OF_WORKSPACE(0x000A, "Out of Workspace - Robot out of defined range"),
        GREATER_THAN_MAX_RANGE(0x000B, "Greater Than Max Range - Command position too large"),
        LESS_THAN_MIN_RANGE(0x000C, "Less Than Min Range - Command position too small");

        private final int code;
        private final String message;

        ControllerAlarmCode(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public int getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }

        // New method to get enum from short hex string like "02"
        public static ControllerAlarmCode fromShortCode(String shortCode) {
            try {
                int parsedCode = Integer.parseInt(shortCode, 16);
                for (ControllerAlarmCode alarm : values()) {
                    if (alarm.code == parsedCode) {
                        return alarm;
                    }
                }
            } catch (NumberFormatException e) {
                // Invalid hex input
                Log.i(TAG, "Received un-documented Controller's Error Code");
            }
            return null;
        }
    }

    public enum ServoAlarmCode {
        PARAM_CHECKSUM_ERROR(0xFF01, "Parameter Checksum Error - Error in parameter data in the Servo Drive"),
        FAILED_INIT(0xFF02, "Driver initialization failed - Power supply issue during initialization"),
        PARAM_OUT_OF_RANGE(0xFF03, "Parameter Setting Error - Out of range"),
        INVALID_SERVO_ON_COMMAND(0xFF04, "Invalid Servo ON Command - Sent after motor ON utility"),
        OVER_CURRENT(0xFF05, "Overcurrent Detected - Overcurrent through transformer or overheated sink"),
        IPM_ERROR_PROTECTION(0xFF06, "IPM Error Protection - Intelligent Power Module error"),
        MAIN_OVER_VOLTAGE(0xFF07, "Main Over voltage - Main circuit DC voltage is too high"),
        MAIN_UNDER_VOLTAGE(0xFF08, "Main Under voltage - Main circuit DC voltage is too low"),
        MOTOR_OVER_SPEED(0xFF09, "Motor Over Speed - Motor exceeded max speed"),
        MAX_SPEED_ERROR(0xFF0A, "Maximum Speed Setting Error - Exceeds max motor speed"),
        INSTANT_OVERLOAD(0xFF0B, "Instantaneous Overload - Excessive torque for seconds"),
        CONT_OVERLOAD(0xFF0C, "Continuous Overload - Torque exceeded rating over time"),
        INTERNAL_TEMP_SENSOR_ERROR(0xFF0D, "Internal Temp Sensor Error - Abnormal control PCB temp"),
        SERVO_FAN_STOPPED(0xFF0E, "Servo Drive Fan Stopped - Internal fan not running"),
        OVERHEAT(0xFF0F, "Overheat Alarm - Temperature input exceeded threshold"),
        MOTOR_OUT_OF_CONTROL(0xFF10, "Servomotor Out of Control - Motor ran uncontrollably"),
        POSITION_DEVIATION_OVERFLOW(0xFF11, "Position Deviation Overflow - Position error too large"),
        DEVIATION_OVERFLOW(0xFF12, "Deviation Overflow at Servo ON - Error at power on"),
        CURRENT_PHASE_U_FAILED(0xFF13, "Current Detection Error 1 - Phase U detection failed"),
        CURRENT_PHASE_V_FAILED(0xFF14, "Current Detection Error 2 - Phase V detection failed"),
        CONTROL_VOLTAGE_FAULT(0xFF15, "Control Voltage Fault - Invalid voltage"),
        ENCODER_COMM_FAULT(0xFF16, "Encoder Communication Error - Comm with encoder failed"),
        ENCODER_OVER_SPEED(0xFF17, "Encoder Overspeed - Encoder too fast at startup"),
        ENCODER_FULL_ABSOLUTE_STATUS(0xFF18, "Encoder Full Absolute Status - Motor rotated >100rpm at start"),
        ENCODER_COUNT_ERROR(0xFF19, "Encoder Counting Error - Data deviation on power-up"),
        ENCODER_COUNT_OVERFLOW(0xFF1A, "Encoder Counter Overflow - Multi-turn counter overflow"),
        ENCODER_OVERHEAT(0xFF1B, "Encoder Overheat - Temperature too high"),
        ENCODER_DATA_LOSS(0xFF1C, "Encoder Backup Alarm - Power loss caused data loss"),
        ENCODER_BATTERY_BELOW_275(0xFF1D, "Encoder Battery Error - Voltage below 2.75V"),
        ENCODER_BATTERY_BELOW_31(0xFF1E, "Encoder Battery Alarm - Voltage below 3.1V at power-on"),
        SOFTWARE_ERROR(0xFF1F, "Software Error - Driver/Software communication failed"),
        ESM_STATE_ERROR(0xFF20, "ESM State Transition Error - Unexpected command during operation");

        private final int code;
        private final String message;

        ServoAlarmCode(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public int getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }

        public static ServoAlarmCode fromShortCode(String shortCode) {
            try {
                int parsedCode = Integer.parseInt(shortCode, 16);
                for (ServoAlarmCode alarm : values()) {
                    if (alarm.code == parsedCode) {
                        return alarm;
                    }
                }
            } catch (NumberFormatException e) {
                // Handle invalid input gracefully
            }
            return null;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_history, container, false);
        logManager = new LogManager(this.getContext());
        logViewModel = new ViewModelProvider(this).get(LogViewModel.class);

        operationLogAdapter = new OperationLogAdapter();
        alarmLogAdapter = new AlarmLogAdapter();

        RecyclerView operationLogRecycler = view.findViewById(R.id.operationHistoryRecyclerView);
        operationLogRecycler.setAdapter(operationLogAdapter);
        operationLogRecycler.setLayoutManager(new LinearLayoutManager(this.getContext()));

        RecyclerView alarmLogRecycler = view.findViewById(R.id.alarmHistoryRecyclerView);
        alarmLogRecycler.setAdapter(alarmLogAdapter);
        alarmLogRecycler.setLayoutManager(new LinearLayoutManager(this.getContext()));

        logViewModel.getOperationLogs().observe(getViewLifecycleOwner(), operationLogAdapter::updateLogs);
        logViewModel.getAlarmLogs().observe(getViewLifecycleOwner(), alarmLogAdapter::updateLogs);

        recordOperation("GUI initializing...");

        startPeriodicSaving();

        return view;
    }

    public void startPeriodicSaving() {
        saveHandler.postDelayed(saveRunnable, SAVE_INTERVAL);
    }

    public void stopPeriodicSaving() {
        saveHandler.removeCallbacks(saveRunnable);
    }

    public static void recordOperation(String message) {
        logViewModel.addOperationLog(message);  // Update UI
        logManager.addOperationLog(message);
    }

    public static void recordAlarm(String code, String subCode, String message) {
        logViewModel.addAlarmLog(code, subCode, message);  // Update UI
        logManager.addAlarmLog(code, subCode, message);
    }

    @Override
    public void onStop() {
        super.onStop();
        logManager.saveLogsToFile();
        stopPeriodicSaving();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        logManager.saveLogsToFile();  // Save logs when the app is destroyed
        stopPeriodicSaving();
    }
}