package com.example.myrobotapp.Class.CommandList;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.myrobotapp.Fragments.Features.Program.ProgramEditFragment;
import com.example.myrobotapp.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MotionCommandsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MotionCommandsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public MotionCommandsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MotionCommandsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MotionCommandsFragment newInstance(String param1, String param2) {
        MotionCommandsFragment fragment = new MotionCommandsFragment();
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

    public static String addedCommandText = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_motion_commands, container, false);

        int[] buttonIds = {
                            R.id.jMoveButton, R.id.lMoveButton, R.id.aMoveButton,
                            R.id.cMoveButton, R.id.homeButton
        };

        for (int buttonId : buttonIds){
            Button button = view.findViewById(buttonId);
            button.setOnClickListener(this::configureMotionCommandButton);
        }

        return view;
    }

    public void configureMotionCommandButton(View view) {

        Button button = (Button) view;

        String buttonText = button.getText().toString();

        switch (buttonText) {
            case "JMOVE":
                addedCommandText = "JMOVE #P[No.]";
                break;

            case "LMOVE":
                addedCommandText = "LMOVE P[No.]";
                break;

            case "AMOVE":
                addedCommandText = "AMOVE P[Mid-Position No.], P[Des-Position No.]";
                break;

            case "CMOVE":
                addedCommandText = "CMOVE P[Mid-position 1 No.], P[Mid-position 2 No.]";
                break;

            case "HOME":
                addedCommandText = "HOME HomeNo.";
                break;

            default:
                Toast.makeText(requireContext(), "Invalid command", Toast.LENGTH_SHORT).show();
                break;
        }

    }
}