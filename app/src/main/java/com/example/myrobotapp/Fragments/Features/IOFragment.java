package com.example.myrobotapp.Fragments.Features;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.example.myrobotapp.Class.DataTable.ProgramInput.ProgramInputCodeLineModel;
import com.example.myrobotapp.Class.DataTable.ProgramInput.ProgramInputRowAdapter;
import com.example.myrobotapp.Class.IO.AnalogInputFragment;
import com.example.myrobotapp.Class.IO.AnalogOutputFragment;
import com.example.myrobotapp.Class.IO.DigitalInputFragment;
import com.example.myrobotapp.Class.IO.DigitalOutputFragment;
import com.example.myrobotapp.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link IOFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class IOFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public IOFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment IOFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static IOFragment newInstance(String param1, String param2) {
        IOFragment fragment = new IOFragment();
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

    FragmentManager fragmentManager;
    Fragment digitalInputFragment, digitalOutputFragment, analogInputFragment, analogOutputFragment;
    private Button selectedButton = null;
    Button digitalInputButton, digitalOutputButton, analogInputButton, analogOutputButton;
    FrameLayout ioFrameLayout;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_i_o, container, false);

        // Fragments switching
        fragmentManager = requireActivity().getSupportFragmentManager();
        digitalInputFragment = new DigitalInputFragment();
        digitalOutputFragment = new DigitalOutputFragment();
        analogInputFragment = new AnalogInputFragment();
        analogOutputFragment = new AnalogOutputFragment();

        digitalInputButton = view.findViewById(R.id.digitalInputButton);
        highlightButton(digitalInputButton);
        digitalOutputButton = view.findViewById(R.id.digitalOutputButton);
        analogInputButton = view.findViewById(R.id.analogInputButton);
        analogOutputButton = view.findViewById(R.id.analogOutputButton);
        ioFrameLayout = view.findViewById(R.id.ioFrameLayout);

        int[] ids = {R.id.digitalInputButton, R.id.digitalOutputButton, R.id.analogInputButton, R.id.analogOutputButton};

        for (int id : ids) {
            Button button = view.findViewById(id);
            String buttonText = button.getText().toString();

            button.setOnClickListener(v -> {
                switch (buttonText) {
                    case "DIGITAL INPUT":
                        showFragment(digitalInputFragment);
                        highlightButton(button);
                        break;
                    case "DIGITAL OUTPUT":
                        showFragment(digitalOutputFragment);
                        highlightButton(button);
                        break;
                    case "ANALOG INPUT":
                        showFragment(analogInputFragment);
                        highlightButton(button);
                        break;
                    case "ANALOG OUTPUT":
                        showFragment(analogOutputFragment);
                        highlightButton(button);
                        break;
                }
            });
        }

        fragmentManager.beginTransaction()
                .add(R.id.ioFrameLayout, digitalInputFragment, "DIGITAL_INPUT_FRAGMENT")
                .add(R.id.ioFrameLayout, digitalOutputFragment, "DIGITAL_OUTPUT_FRAGMENT")
                .add(R.id.ioFrameLayout, analogInputFragment, "ANALOG_INPUT_FRAGMENT")
                .add(R.id.ioFrameLayout, analogOutputFragment, "ANALOG_OUTPUT_FRAGMENT")
                // .hide(digitalInputFragment)
                .hide(digitalOutputFragment)
                .hide(analogInputFragment)
                .hide(analogOutputFragment)
                .commit();

        return view;
    }

    private void showFragment(Fragment fragment) {
        fragmentManager.beginTransaction()
                .hide(digitalInputFragment)
                .hide(digitalOutputFragment)
                .hide(analogInputFragment)
                .hide(analogOutputFragment)
                .show(fragment)
                .commit();
    }

    private void highlightButton(Button button) {
        // Reset the previously selected button
        if (selectedButton != null) {
            selectedButton.setBackgroundColor(Color.parseColor("#A9A9A9")); // Unselected color
        }

        // Highlight the currently selected button
        button.setBackgroundColor(Color.parseColor("#1496BB")); // Selected color
        selectedButton = button; // Update the selected button
    }
}