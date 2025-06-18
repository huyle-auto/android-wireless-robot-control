package com.example.myrobotapp.Fragments.Features;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.myrobotapp.Class.Privilege.PrivilegeManager;
import com.example.myrobotapp.Class.Privilege.SessionManager;
import com.example.myrobotapp.Fragments.Features.Settings.AxisSettingFragment;
import com.example.myrobotapp.Fragments.Features.Settings.ConnectionFragment;
import com.example.myrobotapp.Fragments.Features.Settings.DevicesFragment;
import com.example.myrobotapp.Fragments.Features.Settings.MotionFragment;
import com.example.myrobotapp.Fragments.Features.Settings.RebootFragment;
import com.example.myrobotapp.Fragments.Features.Settings.RobotTypeFragment;
import com.example.myrobotapp.Fragments.Features.Settings.SpeedFragment;
import com.example.myrobotapp.Fragments.Features.Settings.UserFragment;
import com.example.myrobotapp.R;

import java.lang.reflect.Field;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsFragment extends Fragment implements SessionManager.RoleChangeListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SettingsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsFragment newInstance(String param1, String param2) {
        SettingsFragment fragment = new SettingsFragment();
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

    private Button selectedButton = null;
    Button robotTypeButton, connectionButton, devicesButton, speedButton, axisSettingButton, motionButton, rebootButton,
            userButton;
    private FragmentManager fragmentManager;
    private Fragment robotTypeFragment, connectionFragment, devicesFragment, speedFragment, axisSettingFragment, motionFragment,
                        rebootFragment, userFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_settings, container, false);

        // Initialize all fragments and Fragment Manager
        fragmentManager = getChildFragmentManager();

        robotTypeFragment = new RobotTypeFragment();
        connectionFragment = new ConnectionFragment();
        devicesFragment = new DevicesFragment();
        speedFragment = new SpeedFragment();
        motionFragment = new MotionFragment();
        axisSettingFragment = new AxisSettingFragment();
        rebootFragment = new RebootFragment();
        userFragment = new UserFragment();

        // Assign menu buttons from UI
        robotTypeButton = view.findViewById(R.id.robotTypeButton);
        highlightButton(robotTypeButton);

        connectionButton = view.findViewById(R.id.connectionButton);
        devicesButton = view.findViewById(R.id.devicesButton);
        speedButton = view.findViewById(R.id.speedButton);
        axisSettingButton = view.findViewById(R.id.axisSettingButton);
        motionButton = view.findViewById(R.id.motionButton);
        rebootButton = view.findViewById(R.id.rebootButton);
        userButton = view.findViewById(R.id.userButton);

        // Default fragment is "JogFragment"
        fragmentManager.beginTransaction()
                .add(R.id.fragment_container, robotTypeFragment, "ROBOTTYPE_FRAGMENT")
                .add(R.id.fragment_container, connectionFragment, "CONNECTION_FRAGMENT")
                .add(R.id.fragment_container, devicesFragment, "DEVICES_FRAGMENT")
                .add(R.id.fragment_container, speedFragment, "SPEED_FRAGMENT")
                .add(R.id.fragment_container, axisSettingFragment, "AXIS_SETTING_FRAGMENT")
                .add(R.id.fragment_container, motionFragment, "MOTION_FRAGMENT")
                .add(R.id.fragment_container, rebootFragment, "REBOOT_FRAGMENT")
                .add(R.id.fragment_container, userFragment, "USER_FRAGMENT")
//                .add(R.id.fragment_container, ioFragment, "IO_FRAGMENT")

                .hide(connectionFragment)
                .hide(devicesFragment)
                .hide(axisSettingFragment)
                .hide(speedFragment)
                .hide(motionFragment)
                .hide(rebootFragment)
                .hide(userFragment)

                .commit();

        // Configure fragments switching
        robotTypeButton.setOnClickListener(v -> {
            showFragment(robotTypeFragment);
            highlightButton(robotTypeButton);
        });

        connectionButton.setOnClickListener(v -> {
            showFragment(connectionFragment);
            highlightButton(connectionButton);
        });

        devicesButton.setOnClickListener(v -> {
            showFragment(devicesFragment);
            highlightButton(devicesButton);
        });

        speedButton.setOnClickListener(v -> {
            showFragment(speedFragment);
            highlightButton(speedButton);
        });

        axisSettingButton.setOnClickListener(v -> {
            showFragment(axisSettingFragment);
            highlightButton(axisSettingButton);
        });

        motionButton.setOnClickListener(v -> {
            showFragment(motionFragment);
            highlightButton(motionButton);
        });

        rebootButton.setOnClickListener(v -> {
            showFragment(rebootFragment);
            highlightButton(rebootButton);
        });

        userButton.setOnClickListener(v -> {
            showFragment(userFragment);
            highlightButton(userButton);
        });

        return view;
    }

    private void showFragment(Fragment fragment) {
        fragmentManager.beginTransaction()
                .hide(robotTypeFragment)
                .hide(connectionFragment)
                .hide(devicesFragment)
                .hide(speedFragment)
                .hide(axisSettingFragment)
                .hide(motionFragment)
                .hide(rebootFragment)
                .hide(userFragment)
                .show(fragment)
                .commit();
    }

    private void highlightButton(Button button) {
        // Reset the previously selected button
        if (selectedButton != null) {
            selectedButton.setBackgroundColor(Color.parseColor("#A9A9A9")); // Unselected color
        }

        // Highlight the currently selected button
        button.setBackgroundColor(Color.parseColor("#00BFFF")); // Selected color
        selectedButton = button; // Update the selected button
    }

    private void updateUI(String role) {
        // Get the list of allowed fields for the current role
        List<String> unAllowedFields = PrivilegeManager.getUnAllowedFields(role, "SettingsFragment");

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