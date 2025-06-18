package com.example.myrobotapp;

import static com.example.myrobotapp.Fragments.Features.HistoryFragment.recordOperation;
import static com.example.myrobotapp.Fragments.Features.Settings.ConnectionFragment.activeConnectionKey;
import static com.example.myrobotapp.Fragments.Features.Settings.ConnectionFragment.bluetoothHelper;
import static com.example.myrobotapp.Fragments.Features.Settings.ConnectionFragment.globalSelectedBTDevice;
import static com.example.myrobotapp.Fragments.Features.Settings.MotionFragment.manLinSpeed;
import static com.example.myrobotapp.Fragments.Features.Settings.MotionFragment.manRotSpeed;
import static com.example.myrobotapp.Fragments.Features.Settings.SpeedFragment.orgValue;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import com.example.myrobotapp.Class.Communication.PriorityCommunication;
import com.example.myrobotapp.Class.Dialog.ErrorDialog;
import com.example.myrobotapp.Class.GlobalData;
import com.example.myrobotapp.Class.ProtocolReceive;
import com.example.myrobotapp.Class.TCP.TCPClient;
import com.example.myrobotapp.Class.DeviceTable.DeviceInfo.DeviceSharedViewModel;
import com.example.myrobotapp.Fragments.Features.DataFragment;
import com.example.myrobotapp.Fragments.Features.HistoryFragment;
import com.example.myrobotapp.Fragments.Features.IOFragment;
import com.example.myrobotapp.Fragments.Features.JogFragment;
import com.example.myrobotapp.Fragments.Features.MonitorFragment;
import com.example.myrobotapp.Fragments.Features.ProgramFragment;
import com.example.myrobotapp.Fragments.Features.Settings.ConnectionFragment;
import com.example.myrobotapp.Fragments.Features.SettingsFragment;
import com.example.myrobotapp.Fragments.Features.TrackingFragment;

public class HomeScreenActivity extends AppCompatActivity implements ProtocolReceive.ErrorListener, JogFragment.JogModeChangeListener {
    private static final String TAG = "HomeScreenActivity";

    private Button selectedButton = null;
    private int servoState = 2;
    private int modeState = 2;

    public static float speedLevel;         // speed Level (in percentage)
    private int speedLevelIndex = 0;

    public static float jogVelocity;

    public static int coordinateState = 1;  // Joint Coordinate initially
    Button coordinateButton, servoButton, resetErrorButton, modeButton, speedButton;
    Button jogButton, programButton, dataButton, ioButton, trackingButton, monitorButton, historyButton, settingsButton;
    Button j1_Inc, j1_Dec, j2_Inc, j2_Dec, j3_Inc, j3_Dec, j4_Inc, j4_Dec, j5_Inc, j5_Dec, j6_Inc, j6_Dec;
    Button toggleJogLayoutButton;
    LinearLayout jogButtonLayout;
    View jogOverlayBlocker;
    private boolean isEditMode = false;

    // Define a Handler and Runnable for repeating actions
    private Handler repeatHandler = new Handler();
    private Runnable repeatRunnable;
    // Set the repeat time in milliseconds
    private static final long REPEAT_DELAY = 200; // Adjust this for the desired repeat interval

    // All about Fragments
    private FragmentManager fragmentManager;
    private Fragment jogFragment, programFragment, dataFragment, ioFragment, trackingFragment, monitorFragment, historyFragment, settingsFragment;

    // Executor service to handle multiple connections concurrently
    private ExecutorService executorService;

    // Shared Data
    private DeviceSharedViewModel sharedViewModel;

    // Suppress back button (navigation bar)
    private OnBackPressedCallback callback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // MUST BE CALLED BEFORE setContentView to disable insets
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        setContentView(R.layout.activity_homescreen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Create and add the back press callback
        callback = new OnBackPressedCallback(true /* enabled */) {
            @Override
            public void handleOnBackPressed() {
                // Do nothing, just consume the event
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

        // Prevent closing screen for inactivities
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //        if (savedInstanceState == null) {
        //            // Do your onCreate stuff because there is no bundle
        //        }
        // Do stuff that needs to be done even if there is a saved instance, or do nothing

        // Initialize all fragments and Fragment Manager
        fragmentManager = getSupportFragmentManager();

        if (savedInstanceState == null) {
            jogFragment = new JogFragment();
            programFragment = new ProgramFragment();
            dataFragment = new DataFragment();
            ioFragment = new IOFragment();
            trackingFragment = new TrackingFragment();
            monitorFragment = new MonitorFragment();
            historyFragment = new HistoryFragment();
            settingsFragment = new SettingsFragment();

            fragmentManager.beginTransaction()
                    .add(R.id.fragment_container, jogFragment, "JOG")
                    .add(R.id.fragment_container, programFragment, "PROGRAM").hide(programFragment)
                    .add(R.id.fragment_container, dataFragment, "DATA").hide(dataFragment)
                    .add(R.id.fragment_container, ioFragment, "IO").hide(ioFragment)
                    .add(R.id.fragment_container, trackingFragment, "TRACKING").hide(trackingFragment)
                    .add(R.id.fragment_container, monitorFragment, "MONITOR").hide(monitorFragment)
                    .add(R.id.fragment_container, historyFragment, "HISTORY").hide(historyFragment)
                    .add(R.id.fragment_container, settingsFragment, "SETTINGS").hide(settingsFragment)
                    .commit();
        }
        else {
            // Restore existing fragments
            jogFragment = (JogFragment) fragmentManager.findFragmentByTag("JOG");
            programFragment = (ProgramFragment) fragmentManager.findFragmentByTag("PROGRAM");
            dataFragment = (DataFragment) fragmentManager.findFragmentByTag("DATA");
            ioFragment = (IOFragment) fragmentManager.findFragmentByTag("IO");
            trackingFragment = (TrackingFragment) fragmentManager.findFragmentByTag("TRACKING");
            monitorFragment = (MonitorFragment) fragmentManager.findFragmentByTag("MONITOR");
            historyFragment = (HistoryFragment) fragmentManager.findFragmentByTag("HISTORY");
            settingsFragment = (SettingsFragment) fragmentManager.findFragmentByTag("SETTINGS");
        }

        // General buttons (on activity)
        servoButton = findViewById(R.id.servoButton);
        resetErrorButton = findViewById(R.id.resetErrorButton);
        modeButton = findViewById(R.id.modeButton);
        coordinateButton = findViewById(R.id.coordinateButton);
        speedButton = findViewById(R.id.speedButton);

        // Assign all fragments buttons
        jogButton = findViewById(R.id.jogButton);
        highlightButton(jogButton);

        programButton = findViewById(R.id.programButton);
        dataButton = findViewById(R.id.dataButton);
        ioButton = findViewById(R.id.ioButton);
        trackingButton = findViewById(R.id.trackingButton);
        monitorButton = findViewById(R.id.monitorButton);
        historyButton = findViewById(R.id.historyButton);
        settingsButton = findViewById(R.id.settingsButton);

        // Assign all buttons and values from their elements
        j1_Inc = findViewById(R.id.j1_IncButton);
        j1_Dec = findViewById(R.id.j1_DecButton);
        j2_Inc = findViewById(R.id.j2_IncButton);
        j2_Dec = findViewById(R.id.j2_DecButton);
        j3_Inc = findViewById(R.id.j3_IncButton);
        j3_Dec = findViewById(R.id.j3_DecButton);
        j4_Inc = findViewById(R.id.j4_IncButton);
        j4_Dec = findViewById(R.id.j4_DecButton);
        j5_Inc = findViewById(R.id.j5_IncButton);
        j5_Dec = findViewById(R.id.j5_DecButton);
        j6_Inc = findViewById(R.id.j6_IncButton);
        j6_Dec = findViewById(R.id.j6_DecButton);

        toggleJogLayoutButton = findViewById(R.id.toggleJogLayoutButton);
        toggleJogLayoutButton.setOnClickListener(v -> {
            if (jogButtonLayout.getVisibility() == View.GONE) {
                jogButtonLayout.setVisibility(View.VISIBLE);    // Set Visible
                toggleJogLayoutButton.setText("▶"); // Rotate arrow
            }
            else {
                jogButtonLayout.setVisibility(View.GONE);   // Set Invisible
                toggleJogLayoutButton.setText("◀");   // Rotate arrow
            }

            // Ensure toggle button collapse with JOG buttons layout
            if (isEditMode && jogButtonLayout.getVisibility() == View.GONE) {
                jogOverlayBlocker.setVisibility(View.GONE);
            }
            else if (isEditMode && jogButtonLayout.getVisibility() == View.VISIBLE) {
                jogOverlayBlocker.setVisibility(View.VISIBLE);
            }
        });
        jogButtonLayout = findViewById(R.id.jogButtonLayout);

        // Set button click listeners
        jogButton.setOnClickListener(v -> switchFragment(jogFragment, jogButton));
        programButton.setOnClickListener(v -> switchFragment(programFragment, programButton));
        dataButton.setOnClickListener(v -> switchFragment(dataFragment, dataButton));
        ioButton.setOnClickListener(v -> switchFragment(ioFragment, ioButton));
        trackingButton.setOnClickListener(v -> switchFragment(trackingFragment, trackingButton));
        monitorButton.setOnClickListener(v -> switchFragment(monitorFragment, monitorButton));
        historyButton.setOnClickListener(v -> switchFragment(historyFragment, historyButton));
        settingsButton.setOnClickListener(v -> switchFragment(settingsFragment, settingsButton));
//        jogButton.setOnClickListener(v -> switchFragment(JogFragment.class, jogButton, "JOG"));
//        programButton.setOnClickListener(v -> switchFragment(ProgramFragment.class, programButton, "PROGRAM"));
//        dataButton.setOnClickListener(v -> switchFragment(DataFragment.class, dataButton, "DATA"));
//        ioButton.setOnClickListener(v -> switchFragment(IOFragment.class, ioButton, "IO"));
//        trackingButton.setOnClickListener(v -> switchFragment(TrackingFragment.class, trackingButton, "TRACKING"));
//        monitorButton.setOnClickListener(v -> switchFragment(MonitorFragment.class, monitorButton, "MONITOR"));
//        historyButton.setOnClickListener(v -> switchFragment(HistoryFragment.class, historyButton, "HISTORY"));
//        settingsButton.setOnClickListener(v -> switchFragment(SettingsFragment.class, settingsButton, "SETTINGS"));

        // Configure buttons functionality
        configureCoordinateButton();
        configureServoButton();
        configureResetErrorButton();
        configureModeButton();
        configureSpeedButton();

        // Assign functions for all JOG buttons
        j1_Inc.setOnTouchListener(this::onButtonTouch);
        j1_Dec.setOnTouchListener(this::onButtonTouch);
        j2_Inc.setOnTouchListener(this::onButtonTouch);
        j2_Dec.setOnTouchListener(this::onButtonTouch);
        j3_Inc.setOnTouchListener(this::onButtonTouch);
        j3_Dec.setOnTouchListener(this::onButtonTouch);
        j4_Inc.setOnTouchListener(this::onButtonTouch);
        j4_Dec.setOnTouchListener(this::onButtonTouch);
        j5_Inc.setOnTouchListener(this::onButtonTouch);
        j5_Dec.setOnTouchListener(this::onButtonTouch);
        j6_Inc.setOnTouchListener(this::onButtonTouch);
        j6_Dec.setOnTouchListener(this::onButtonTouch);

        enableImmersiveMode();

        // Shared Data
        sharedViewModel = new ViewModelProvider(this).get(DeviceSharedViewModel.class);

        // Initial speed Level is LOW
        speedLevel = orgValue[0];
        speedLevel = speedLevel / 100;

        // Register error listener
        ProtocolReceive.errorListener = this;

        // Dynamic sizing for overlay layer of Jog Buttons
        jogOverlayBlocker = findViewById(R.id.jogOverlayBlocker);
        jogOverlayBlocker.setOnClickListener(v -> {
            showToast("Cannot JOG in EDIT MODE");
        });

        jogButtonLayout.post(() -> {
            ViewGroup.LayoutParams sizeParams = jogOverlayBlocker.getLayoutParams();
            sizeParams.width = jogButtonLayout.getWidth();
            sizeParams.height = jogButtonLayout.getHeight();
            jogOverlayBlocker.setLayoutParams(sizeParams);
        });
    }

    // Method to handle button touch events
    private boolean onButtonTouch(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                jogPress(view); // Start repeating action on button press
                return true; // Indicate that the touch event is handled
            case MotionEvent.ACTION_UP:
                jogRelease(view);
            case MotionEvent.ACTION_CANCEL:
//                jogRelease(view); // Stop repeating action on button release
//                return true; // Indicate that the touch event is handled
        }
        return false; // Return false for other actions
    }

    private void jogPress(View view) {
        String buttonTag = (String) view.getTag();

        // Only allow JOGGING in Manual mode
        if (modeState == 2) {
            Toast.makeText(this, "Switch to MANUAL mode to JOG", Toast.LENGTH_SHORT).show();
            return;
        }

        // SERVO ON before JOGGING
        if (servoState == 2) {
            Toast.makeText(this, "Turn SERVO ON to JOG", Toast.LENGTH_SHORT).show();
            return;
        }

        // Select jogging type and corresponding speed unit
        // CURRENTLY ONLY JOINT COORDINATE IS AVAILABLE !!
        // 1 = JOINT: Rotation speed (deg/s)
        // 2 = 3 = 4 = TOOL/USER/WORLD: Linear speed (mm/s)
        if (coordinateState == 1) {
            jogVelocity = manRotSpeed * speedLevel;
        } else {
            jogVelocity = manLinSpeed * speedLevel;
        }

        String[] tagProperties = buttonTag.split("_");  // [0] = "J1", "J2", ...
        // [1] = "Increase" / "Decrease"
        int axisNum = Integer.parseInt(tagProperties[0].substring(1));
        int axisDirection = tagProperties[1].equals("Increase") ? 2 : 1;    // 1: Decrease, 2: Increase

        System.out.println("Request Jog axis: " + axisNum + " with velocity: " + jogVelocity + " deg/s");

        ConcurrentHashMap<String, TCPClient> activeConnections = GlobalData.getInstance().getActiveConnections();
        TCPClient tcpClient = activeConnections.get(activeConnectionKey);

        if (tcpClient == null && globalSelectedBTDevice == null) {
            Toast.makeText(this, "Server not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // TCP (Prioritized)
        PriorityCommunication.ChannelType channelType = PriorityCommunication.checkAvailableChannel();
        if (channelType == PriorityCommunication.ChannelType.TCP) {
            tcpClient.jog(coordinateState, axisNum, 1, axisDirection, jogVelocity);
            return;
        }

        // Bluetooth
        if (channelType == PriorityCommunication.ChannelType.Bluetooth) {
            ConnectionFragment.bluetoothHelper.jog(globalSelectedBTDevice, coordinateState, axisNum, 1, axisDirection, jogVelocity);
            return;
        }

        showToast("No server available");
    }

    private void jogRelease(View view) {
        String buttonTag = (String) view.getTag();

        // Only allow JOGGING in Manual mode
        if (modeState == 2) {
            Toast.makeText(this, "Switch to MANUAL mode to JOG", Toast.LENGTH_SHORT).show();
            return;
        }

        // SERVO ON before JOGGING
        if (servoState == 2) {
            Toast.makeText(this, "Turn SERVO ON to JOG", Toast.LENGTH_SHORT).show();
            return;
        }

        // Select jogging type and corresponding speed unit
        // CURRENTLY ONLY JOINT COORDINATE IS AVAILABLE !!
        // 1 = JOINT: Rotation speed (deg/s)
        // 2 = 3 = 4 = TOOL/USER/WORLD: Linear speed (mm/s)
        if (coordinateState == 1) {
            jogVelocity = manRotSpeed * speedLevel;
        } else {
            jogVelocity = manLinSpeed * speedLevel;
        }

        String[] tagProperties = buttonTag.split("_");  // [0] = "J1", "J2", ...
        // [1] = "Increase" / "Decrease"
        int axisNum = Integer.parseInt(tagProperties[0].substring(1));
        int axisDirection = tagProperties[1].equals("Increase") ? 2 : 1;    // 1: Decrease, 2: Increase

        ConcurrentHashMap<String, TCPClient> activeConnections = GlobalData.getInstance().getActiveConnections();
        TCPClient tcpClient = activeConnections.get(activeConnectionKey);

        if (tcpClient == null && globalSelectedBTDevice == null) {
            Toast.makeText(this, "Server not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // TCP (Prioritized)
        PriorityCommunication.ChannelType channelType = PriorityCommunication.checkAvailableChannel();
        if (channelType == PriorityCommunication.ChannelType.TCP) {
            tcpClient.jog(coordinateState, axisNum, 2, axisDirection, jogVelocity);
            return;
        }

        // Bluetooth
        if (channelType == PriorityCommunication.ChannelType.Bluetooth) {
            ConnectionFragment.bluetoothHelper.jog(globalSelectedBTDevice, coordinateState, axisNum, 2, axisDirection, jogVelocity);
            return;
        }

        showToast("No server available");
    }

    public DeviceSharedViewModel getSharedViewModel() {
        return sharedViewModel;
    }

    private void enableImmersiveMode() {
        View decorView = getWindow().getDecorView();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            final WindowInsetsController insetsController = getWindow().getInsetsController();
            if (insetsController != null) {
                insetsController.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                insetsController.setSystemBarsBehavior(
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

            decorView.setOnSystemUiVisibilityChangeListener(visibility -> {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    decorView.setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                }
            });
        }
    }

    private void showFragment(Fragment fragment) {
        fragmentManager.beginTransaction()
                .hide(jogFragment)
                .hide(programFragment)
                .hide(dataFragment)
                .hide(ioFragment)
                .hide(trackingFragment)
                .hide(monitorFragment)
                .hide(historyFragment)
                .hide(settingsFragment)
                .show(fragment)
                .commit();
    }

    private void switchFragment(Fragment fragment, Button button) {
        fragmentManager.beginTransaction()
                .hide(jogFragment)
                .hide(programFragment)
                .hide(dataFragment)
                .hide(ioFragment)
                .hide(trackingFragment)
                .hide(monitorFragment)
                .hide(historyFragment)
                .hide(settingsFragment)
                .show(fragment)
                .commit();

        highlightButton(button);
    }

//    private void switchFragment(Class<? extends Fragment> fragmentClass, Button button, String tag) {
//        Fragment fragment = fragmentManager.findFragmentByTag(tag);
//
//        if (fragment == null) {
//            try {
//                fragment = fragmentClass.newInstance();
//                fragmentManager.beginTransaction()
//                        .add(R.id.fragment_container, fragment, tag)
//                        .commit();
//            } catch (Exception e) {
//                Log.e(TAG, "Error in fragment loading: " + e.getMessage());
//            }
//        }
//
//        // Hide all other fragments
//        for (Fragment f : fragmentManager.getFragments()) {
//            if (f != fragment) {
//                fragmentManager.beginTransaction().hide(f).commit();
//            }
//        }
//
//        // Show the selected fragment
//        fragmentManager.beginTransaction().show(fragment).commit();
//
//        highlightButton(button);
//    }


    private void highlightButton(Button button) {
        // Reset the previously selected button
        if (selectedButton != null) {
            selectedButton.setBackgroundColor(Color.parseColor("#A9A9A9")); // Unselected color
        }

        // Highlight the currently selected button
        button.setBackgroundColor(Color.parseColor("#00BFFF")); // Selected color
        selectedButton = button; // Update the selected button
    }

    private void configureServoButton() {
        servoButton.setOnClickListener(v -> {
            ConcurrentHashMap<String, TCPClient> activeConnections = GlobalData.getInstance().getActiveConnections();
            TCPClient tcpClient = activeConnections.get(activeConnectionKey);

            if (tcpClient == null && globalSelectedBTDevice == null) {
                Toast.makeText(this, "Server not found", Toast.LENGTH_SHORT).show();
                return;
            }

            // TCP (Prioritized)
            PriorityCommunication.ChannelType channelType = PriorityCommunication.checkAvailableChannel();
            if (channelType == PriorityCommunication.ChannelType.TCP) {
                Log.i(TAG, "TCP available! Sending...");
                servoState = (servoState == 2) ? 1 : 2; // Toggling between Servo ON (1) and OFF (2) state. OFF initially
                tcpClient.setServo(0, servoState);

                String servoButtonText = "";
                if (servoState == 1) {
                    servoButtonText = "SERVO ON";
                    servoButton.setText(servoButtonText);
                    recordOperation("SERVO POWER ON");
                } else if (servoState == 2) {
                    servoButtonText = "SERVO OFF";
                    servoButton.setText(servoButtonText);
                    recordOperation("SERVO POWER OFF");
                }
                return;
            }

            // Bluetooth
            if (channelType == PriorityCommunication.ChannelType.Bluetooth) {
                Log.i(TAG, "Bluetooth available! Sending...");
                servoState = (servoState == 2) ? 1 : 2; // Toggling between Servo ON (1) and OFF (2) state. OFF initially
                ConnectionFragment.bluetoothHelper.setServo(globalSelectedBTDevice, 0, servoState);

                String servoButtonText = "";
                if (servoState == 1) {
                    servoButtonText = "SERVO ON";
                    servoButton.setText(servoButtonText);
                    recordOperation("SERVO POWER ON");
                } else if (servoState == 2) {
                    servoButtonText = "SERVO OFF";
                    servoButton.setText(servoButtonText);
                    recordOperation("SERVO POWER OFF");
                }
                return;
            }

            showToast("No server available");
        });
    }   // Added priority

    private void configureResetErrorButton() {
        resetErrorButton.setOnClickListener(v -> {
            ConcurrentHashMap<String, TCPClient> activeConnections = GlobalData.getInstance().getActiveConnections();
            TCPClient tcpClient = activeConnections.get(activeConnectionKey);

            if (tcpClient == null && globalSelectedBTDevice == null) {
                Toast.makeText(this, "Server not found", Toast.LENGTH_SHORT).show();
                return;
            }

            // TCP (Prioritized)
            PriorityCommunication.ChannelType channelType = PriorityCommunication.checkAvailableChannel();
            if (channelType == PriorityCommunication.ChannelType.TCP) {
                Log.i(TAG, "TCP available! Sending...");
                tcpClient.resetErrors();
                recordOperation("request RESET ERROR");
                return;
            }

            // Bluetooth
            if (channelType == PriorityCommunication.ChannelType.Bluetooth) {
                Log.i(TAG, "Bluetooth available! Sending...");
                ConnectionFragment.bluetoothHelper.resetErrors(globalSelectedBTDevice);
                recordOperation("request RESET ERROR");
                return;
            }

            showToast("No server available");
        });
    }   // Added priority

    private void configureModeButton() {
        modeButton.setOnClickListener(v -> {
            ConcurrentHashMap<String, TCPClient> activeConnections = GlobalData.getInstance().getActiveConnections();
            TCPClient tcpClient = activeConnections.get(activeConnectionKey);

            if (tcpClient == null && globalSelectedBTDevice == null) {
                Toast.makeText(this, "Server not found", Toast.LENGTH_SHORT).show();
                return;
            }

            // TCP (Prioritized)
            PriorityCommunication.ChannelType channelType = PriorityCommunication.checkAvailableChannel();
            if (channelType == PriorityCommunication.ChannelType.TCP) {
                Log.i(TAG, "TCP available! Sending...");
                modeState = (modeState == 2) ? 1 : 2;   // Toggling between AUTO and MANUAL state. AUTO initially
                tcpClient.setMode(modeState);

                if (modeState == 1) {
                    modeButton.setText("MANUAL");
                    recordOperation("request mode MANUAL");
                }
                else if (modeState == 2) {
                    modeButton.setText("AUTO");
                    recordOperation("request mode AUTO");
                }

                return;
            }

            // Bluetooth
            if (channelType == PriorityCommunication.ChannelType.Bluetooth) {
                Log.i(TAG, "Bluetooth available! Sending...");
                modeState = (modeState == 2) ? 1 : 2;   // Toggling between AUTO and MANUAL state. AUTO initially
                ConnectionFragment.bluetoothHelper.setMode(globalSelectedBTDevice, modeState);

                if (modeState == 1) {
                    modeButton.setText("MANUAL");
                    recordOperation("request mode MANUAL");
                }
                else if (modeState == 2) {
                    modeButton.setText("AUTO");
                    recordOperation("request mode AUTO");
                }

                return;
            }

            showToast("No server available");
        });
    }   // Added priority

    private void configureCoordinateButton() {
        coordinateButton.setOnClickListener(v -> {
            // Toggling between 1: JOINT, 2: WORLD, 3: TOOL, 4: USER
            if (coordinateState < 4) {
                coordinateState++;
            } else {
                coordinateState = 1;
            }

            String stateText = "";
            switch (coordinateState) {
                case 1:
                    stateText = "Joint";
                    recordOperation("Changed to JOINT coordinate");
                    break;
                case 2:
                    stateText = "World";
                    recordOperation("Changed to WORLD coordinate");
                    break;
                case 3:
                    stateText = "Tool";
                    recordOperation("Changed to TOOL coordinate");
                    break;
                case 4:
                    stateText = "User";
                    recordOperation("Changed to USER coordinate");
                    break;
            }
            coordinateButton.setText(stateText);
        });
    }

    private void configureSpeedButton() {
        speedButton.setOnClickListener(v -> {
            if (speedLevelIndex < 3) {
                speedLevelIndex++;
                speedLevel = orgValue[speedLevelIndex];
                speedLevel = speedLevel / 100;
                System.out.println("Current Speed Level: " + speedLevel + " with manRotSpeed value: " + manRotSpeed);
            } else {
                speedLevelIndex = 0;
                speedLevel = orgValue[speedLevelIndex];
                speedLevel = speedLevel / 100;
                System.out.println("Current Speed Level: " + speedLevel + " with manRotSpeed value: " + manRotSpeed);
            }

            switch (speedLevelIndex) {
                case 0:
                    speedButton.setText("LOW SPEED");
                    recordOperation("Jog speed level set to LOW");
                    break;
                case 1:
                    speedButton.setText("MEDIUM SPEED");
                    recordOperation("Jog speed level set to MEDIUM");
                    break;
                case 2:
                    speedButton.setText("HIGH SPEED");
                    recordOperation("Jog speed level set to HIGH");
                    break;
                case 3:
                    speedButton.setText("TOP SPEED");
                    recordOperation("Jog speed level set to TOP");
                    break;
            }
        });
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            enableImmersiveMode();
        }
    }

    private void showToast(String message) {
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        );
    }

    private ErrorDialog errorDialog = null;
    private long lastPopupTime = 0;

    @Override
    public void onErrorReceived(String errorCode, int axisPosition) {
        long currentPopupTime = System.currentTimeMillis();

        if ((currentPopupTime - lastPopupTime < 30000) || errorDialog != null) {
            return; // Throttled or already showing
        }

//        boolean canShowDialog = false;
//        if (errorDialog == null) {
//            canShowDialog = true; // First dialog
//        } else if (!errorDialog.popupWindow.isShowing() && (currentPopupTime - lastPopupTime >= 15000)) {
//            canShowDialog = true; // Not showing, and throttling condition passed
//        }
//
//        if (canShowDialog) {
            runOnUiThread(() -> {
                String message;

                if (axisPosition == 0) {    // Controller's
                    int errorValue = Integer.parseInt(errorCode, 16);
                    String formattedCode = String.format("%02X", errorValue);
                    HistoryFragment.ControllerAlarmCode controllerAlarmCode = HistoryFragment.ControllerAlarmCode.fromShortCode(formattedCode);

                    // Retrieve message from code
                    if (controllerAlarmCode != null) {
                        message = controllerAlarmCode.getMessage();
                    }
                    else {
                        return;
                    }
                }
                else {  // Servo Driver's
                    int errorValue = Integer.parseInt(errorCode, 16);
                    String formattedCode = String.format("FF%02X", errorValue);
                    HistoryFragment.ServoAlarmCode servoAlarmCode = HistoryFragment.ServoAlarmCode.fromShortCode(formattedCode);

                    // Retrieve message from code
                    if (servoAlarmCode != null) {
                        message = servoAlarmCode.getMessage();
                    }
                    else {
                        return;
                    }
                }

                // Show dialog
                errorDialog = new ErrorDialog(this, "showError");
                errorDialog.setInfoHint("Error with Robot Controller and/or Servo Driver(s)\n\nError Code: " + message);
                errorDialog.showNumpadPopup(this);
                lastPopupTime = System.currentTimeMillis();

                // Format once again before showing onto HistoryFragment
                int errorValue = Integer.parseInt(errorCode, 16);
                String formattedFinalCode = String.format("%02X", errorValue);
                HistoryFragment.recordAlarm(axisPosition == 0 ? "0x00" + formattedFinalCode : "0xFF" + formattedFinalCode, String.valueOf(axisPosition), message);

                errorDialog.popupWindow.setOnDismissListener(() -> {
                    errorDialog = null; // clean up
                });
            });
//        }
    }

    @Override
    public void onLiveModeEntered() {
        // Remove overlay blocker
        jogOverlayBlocker.setVisibility(View.GONE);
        isEditMode = false;
    }

    @Override
    public void onEditModeEntered() {
        // Add overlay blocker
        if (!(jogButtonLayout.getVisibility() == View.GONE)) {
            jogOverlayBlocker.setVisibility(View.VISIBLE);
        }

        isEditMode = true;
    }

//    (DEPRECATED)
//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        // Do nothing
//    }

}

