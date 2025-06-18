package com.example.myrobotapp.Fragments.Features.Settings;

import static com.example.myrobotapp.Class.FileManager.getFilesName;
import static com.example.myrobotapp.Class.FileManager.readFile;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myrobotapp.Class.DeviceTable.ActiveDevice.ActiveDeviceRowAdapter;
import com.example.myrobotapp.Class.DeviceTable.ActiveDevice.ActiveDeviceTableRowModel;
import com.example.myrobotapp.Class.DeviceTable.DeviceInfo.DeviceSharedViewModel;
import com.example.myrobotapp.Class.ViewPagerAdapter;
import com.example.myrobotapp.R;
import com.google.android.material.tabs.TabLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DevicesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DevicesFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public DevicesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DevicesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DevicesFragment newInstance(String param1, String param2) {
        DevicesFragment fragment = new DevicesFragment();
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

    public static final String TAG = "DevicesFragment";
    private final String CUSTOM_CONTROLLER_CONFIG_FILE_PATH = "ControllerSettings/custom/ControllerConfig.txt";

    private TabLayout tabLayout;
    ViewPager2 viewPager2;
    ViewPagerAdapter viewPagerAdapter;
    private TableLayout deviceTableLayout;
    private TableRow selectedLibraryTableRow = null;
    private int rowCount = 1;
    private DeviceSharedViewModel sharedViewModel;

    private Button addDeviceButton, removeDeviceButton;

    ActiveDeviceRowAdapter activeDeviceRowAdapter;
    RecyclerView activeDeviceRecyclerView;
    public static List<ActiveDeviceTableRowModel> activeDeviceList = new ArrayList<>();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_devices, container, false);

        // Tab Layouts for RxPDO, TxPDO, etc.
        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager2 = view.findViewById(R.id.viewPager2);
        viewPagerAdapter = new ViewPagerAdapter(this.requireActivity());
        viewPager2.setAdapter(viewPagerAdapter);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager2.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                tabLayout.getTabAt(position).select();
            }
        });

        deviceTableLayout = view.findViewById(R.id.deviceTableLayout);

        addDeviceButton = view.findViewById(R.id.addDeviceButton);
        addDeviceButton.setOnClickListener(v -> addActiveDevice());

        removeDeviceButton = view.findViewById(R.id.removeDeviceButton);
        removeDeviceButton.setOnClickListener(v -> removeActiveDevice());

        // Populate active device Recycler View
        activeDeviceRecyclerView = view.findViewById(R.id.activeDeviceRecyclerView);
        activeDeviceRowAdapter = new ActiveDeviceRowAdapter(this.requireContext(), activeDeviceList);
        activeDeviceRecyclerView.setAdapter(activeDeviceRowAdapter);
        activeDeviceRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        activeDeviceRowAdapter.notifyDataSetChanged();

        // Update Devices Table by default when entering fragment
        updateDeviceTable();
        updateActiveDeviceTable();

        return view;
    }

    // Update Device Table with Device model file's name
    public void updateDeviceTable(){
        String[] fileNames = getFilesName(this.requireContext(), "DeviceSettings");

        // If no files found, stop update.
        if (fileNames.length == 0){
            return;
        }

        // Print ID from 1 until the last index
        int fileCount = 1;
        for (String name : fileNames) {
            // Show name but remove file extension
            addNewRow(name.substring(0, name.lastIndexOf('.')), String.valueOf(fileCount));
            fileCount++;
        }
    }

    public void updateActiveDeviceTable() {
        File controllerConfigFile = new File(this.requireContext().getFilesDir(), CUSTOM_CONTROLLER_CONFIG_FILE_PATH);
        List<String> oldActiveDevices = new ArrayList<>();

        try {
            String controllerConfigFileText = readFile(controllerConfigFile);
            String[] controllerConfigLines = controllerConfigFileText.split("\n");
            int oldDeviceCount = 0;

            for (int i = 0; i < controllerConfigLines.length; i++) {
                String line = controllerConfigLines[i];
                if (line.startsWith("ECAT_DEVICE=")) {
                    oldDeviceCount = Integer.parseInt(line.split("=")[1]);

                    // Add all next devices then break
                    for (int j = i + 1; j < (i + 1) + oldDeviceCount; j++) {
                        oldActiveDevices.add(controllerConfigLines[j]);
                    }

                    break;
                }
            }
        }
        catch (Exception e) {
            Log.i(TAG, "Error loading old active devices: " + e.getMessage());
        }

        // Add all to adapter of ACTIVE DEVICES table
        for (String device: oldActiveDevices) {
            activeDeviceRowAdapter.addDevice(device);
        }
    }

    // Function to add a new row to the TableLayout
    private void addNewRow(String text, String index) {

        // Create a new TableRow
        TableRow newRow = new TableRow(getActivity());
        newRow.setBackgroundColor(Color.parseColor("#4E1496BB"));
        newRow.setClickable(true);
        newRow.setFocusable(true);
        newRow.setOnClickListener(v -> {
            highlightRow(newRow);
        });

        // Set layout parameters for the new row
        TableRow.LayoutParams params = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );
        newRow.setLayoutParams(params);

        // Create ID TextView
        TextView idTextView = new TextView(getActivity());
        idTextView.setText(index);
        idTextView.setPadding(8, 8, 8, 8);
        idTextView.setTextSize(12);
        idTextView.setTextColor(Color.BLACK);
        idTextView.setBackgroundColor(Color.parseColor("#4E1496BB"));
        idTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        // Set layout parameters with the specified weight
        TableRow.LayoutParams idParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.2f);
        idTextView.setLayoutParams(idParams);

        // Add the TextViews to the new TableRow
        newRow.addView(idTextView);
        newRow.addView(createTextView(text));

        // Add the new TableRow to the TableLayout
        deviceTableLayout.addView(newRow, deviceTableLayout.getChildCount());

        // Increment row count for the next added row
        rowCount++;
    }

    // Helper method to create a TextView
    private TextView createTextView(String text) {
        // Create Name TextView
        TextView textView = new TextView(getActivity());
        textView.setText(text);
        textView.setPadding(8, 8, 8, 8);
        textView.setTextSize(12);
        textView.setTextColor(Color.BLACK);
        textView.setBackgroundColor(Color.parseColor("#4E1496BB"));
        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        // Set layout parameters with the specified weight
        TableRow.LayoutParams params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.8f);
        textView.setLayoutParams(params);

        return textView;
    }

    private void addActiveDevice() {
        if (selectedLibraryTableRow == null) {
            showToast("No device selected", false);
            return;
        }

        TextView nameView = (TextView) selectedLibraryTableRow.getChildAt(1);
        String selectedDeviceName = nameView.getText().toString();

        activeDeviceRowAdapter.addDevice(selectedDeviceName);
        activeDeviceRowAdapter.notifyDataSetChanged();

        activeDeviceRowAdapter.showActiveDevice();
    }

    private void removeActiveDevice() {

        activeDeviceRowAdapter.removeDevice();
        activeDeviceRowAdapter.notifyDataSetChanged();
    }

    // Highlight  and display selected row's info for displaying Parameters
    private void highlightRow(TableRow tableRow) {
        // Reset the previously selected button
        if (selectedLibraryTableRow != null) {
            selectedLibraryTableRow.setBackgroundColor(Color.parseColor("#4E1496BB")); // Unselected color
        }

        // Highlight the currently selected button
        tableRow.setBackgroundColor(Color.parseColor("#A9A9A9")); // Selected color
        selectedLibraryTableRow = tableRow; // Update the selected button
    }

    private void showToast(String message, boolean longToast) {

        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(this.requireContext(), message, longToast ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show()
        );
    }
}