package com.example.myrobotapp.Fragments.Features.Settings.CustomDevices;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.myrobotapp.Class.DeviceTable.DeviceInfo.DeviceSharedViewModel;
import com.example.myrobotapp.Class.DeviceTable.DeviceInfo.DeviceTableRowAdapter;
import com.example.myrobotapp.Class.DeviceTable.DeviceInfo.DeviceTableRowModel;
import com.example.myrobotapp.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RxPDOFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RxPDOFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public RxPDOFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RxPDOFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RxPDOFragment newInstance(String param1, String param2) {
        RxPDOFragment fragment = new RxPDOFragment();
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

    RecyclerView recyclerView;
    DeviceTableRowAdapter adapter;
    List<DeviceTableRowModel> tableDataList;
    private DeviceSharedViewModel sharedViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_rx_p_d_o, container, false);
        recyclerView = view.findViewById(R.id.rxPDORecyclerView);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(DeviceSharedViewModel.class);

        // Initialize RecyclerView and Adapter
        tableDataList = new ArrayList<>();
        adapter = new DeviceTableRowAdapter(tableDataList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
        recyclerView.setAdapter(adapter);

        // When data in the fragment is modified, update the ViewModel
        sharedViewModel.setTable1Data(tableDataList);

        return view;
    }

    private void init(){
        new Thread(() -> {
            for (int i = 0; i < 15; i++){
                addNewRow();
            }
        }).start();
    }

    // Function to add a new row to the TableLayout
    private void addNewRow() {
        // DATA: [entryID],[group],[index],[Sub Index], [size], [role]

        // Create a new TableRow
        TableRow newRow = new TableRow(getActivity());
        newRow.setClickable(true);
        newRow.setFocusable(true);

        // Set layout parameters for the new row
        TableRow.LayoutParams params = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );
        newRow.setLayoutParams(params);

        // Create and add TextViews using the helper method
        newRow.addView(createEditText(Integer.toString(1), 0.2f));
        newRow.addView(createEditText("2", 0.8f));
        newRow.addView(createEditText("2", 0.8f));
        newRow.addView(createEditText("2", 0.8f));
        newRow.addView(createEditText("2", 0.8f));
        newRow.addView(createEditText("2", 0.8f));

//        // Add the new TableRow to the TableLayout
//        rxPDOTableLayout.addView(newRow, rxPDOTableLayout.getChildCount());
//
//        // Increment row count for the next added row
//        entryId++;
    }

    // Helper method to create a EditText
    private TextView createEditText(String text, float weight) {
        // Create the EditText
        EditText editText = new EditText(getActivity());
        //editText.setTag();
        editText.setText(text);
        editText.setPadding(8, 8, 8, 8);
        editText.setTextSize(10);
        editText.setTextColor(Color.BLACK);
        editText.setBackgroundColor(Color.parseColor("#4E1496BB"));
        editText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        // Set layout parameters with the specified weight
        TableRow.LayoutParams params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, weight);
        editText.setLayoutParams(params);

        return editText;
    }

}