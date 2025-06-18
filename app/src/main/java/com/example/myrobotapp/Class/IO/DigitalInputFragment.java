package com.example.myrobotapp.Class.IO;

import static com.example.myrobotapp.Class.ProtocolReceive.inputState;
import static com.example.myrobotapp.Class.ProtocolReceive.outputState;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;

import com.example.myrobotapp.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DigitalInputFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DigitalInputFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public DigitalInputFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DigitalInputFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DigitalInputFragment newInstance(String param1, String param2) {
        DigitalInputFragment fragment = new DigitalInputFragment();
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

    private List<ImageView[]> imageViewGroups;
    Handler handler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_digital_input, container, false);

        // Initialize the list
        imageViewGroups = new ArrayList<>();

        // Get the GridLayout
        GridLayout gridLayout = view.findViewById(R.id.diGridLayout); // Replace with your actual GridLayout ID

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

                if (parts.length == 3 && parts[0].equals("DI")) {
                    int groupNumber = Integer.parseInt(parts[1].substring(parts[1].length() - 2)); // Extract group number
                    int bitIndex = Integer.parseInt(parts[2].substring(parts[2].length() - 1)); // Extract bit index

                    // Store the ImageView in the corresponding group and bit index
                    imageViewGroups.get(groupNumber - 1)[bitIndex] = imageView; // Adjusting for zero-based index
                }
            }
        }

        updateDiState();

        return view;
    }

    private void updateDiState() {
        handler = new Handler();
        // Update current chosen group
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                for (int group = 1; group <= 4; group++) { // Start from 1
                    for (int bit = 0; bit < 8; bit++) {
                        // Calculate the index in inputState
                        int index = (group - 1) * 8 + bit; // Adjust for zero-based index

                        // Check if the index is within the valid range of inputState
                        if (index >= 0 && index < inputState.length) {
                            // Get the corresponding ImageView
                            ImageView imageView = imageViewGroups.get(group - 1)[bit]; // Adjust for zero-based index
                            if (imageView != null) {
                                // Update the ImageView based on the inputState
                                if (inputState[index]) {
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

                // Schedule the next execution
                handler.postDelayed(this, 400);
            }
        }, 400);
    }
}