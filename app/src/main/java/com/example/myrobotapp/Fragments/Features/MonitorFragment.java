package com.example.myrobotapp.Fragments.Features;

import static com.example.myrobotapp.Class.ProtocolReceive.axis1ErrCode;
import static com.example.myrobotapp.Class.ProtocolReceive.axis1Pos;
import static com.example.myrobotapp.Class.ProtocolReceive.axis2ErrCode;
import static com.example.myrobotapp.Class.ProtocolReceive.axis2Pos;
import static com.example.myrobotapp.Class.ProtocolReceive.axis3ErrCode;
import static com.example.myrobotapp.Class.ProtocolReceive.axis3Pos;
import static com.example.myrobotapp.Class.ProtocolReceive.axis4ErrCode;
import static com.example.myrobotapp.Class.ProtocolReceive.axis4Pos;
import static com.example.myrobotapp.Class.ProtocolReceive.axis5ErrCode;
import static com.example.myrobotapp.Class.ProtocolReceive.axis5Pos;
import static com.example.myrobotapp.Class.ProtocolReceive.axis6ErrCode;
import static com.example.myrobotapp.Class.ProtocolReceive.axis6Pos;
import static com.example.myrobotapp.Class.ProtocolReceive.ctrlErrorCode;
import static com.example.myrobotapp.Class.ProtocolReceive.joint1Pos;
import static com.example.myrobotapp.Class.ProtocolReceive.joint2Pos;
import static com.example.myrobotapp.Class.ProtocolReceive.joint3Pos;
import static com.example.myrobotapp.Class.ProtocolReceive.joint4Pos;
import static com.example.myrobotapp.Class.ProtocolReceive.joint5Pos;
import static com.example.myrobotapp.Class.ProtocolReceive.joint6Pos;
import static com.example.myrobotapp.Class.ProtocolReceive.rX;
import static com.example.myrobotapp.Class.ProtocolReceive.rY;
import static com.example.myrobotapp.Class.ProtocolReceive.rZ;
import static com.example.myrobotapp.Class.ProtocolReceive.regularDataStatus;
import static com.example.myrobotapp.Class.ProtocolReceive.xPos;
import static com.example.myrobotapp.Class.ProtocolReceive.yPos;
import static com.example.myrobotapp.Class.ProtocolReceive.zPos;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.example.myrobotapp.HomeScreenActivity;
import com.example.myrobotapp.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MonitorFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MonitorFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public MonitorFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MonitorFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MonitorFragment newInstance(String param1, String param2) {
        MonitorFragment fragment = new MonitorFragment();
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

    public static final String TAG = "MonitorFragment";
    EditText ctrlErrCode;

    List<EditText> axisErrCodeList = new ArrayList<>();
    List<EditText> axisPosList = new ArrayList<>();
    List<EditText> tcpPosList = new ArrayList<>();
    List<EditText> jointPosList = new ArrayList<>();

    TextView regularDataStatusTextView;
    Handler handler;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_monitor, container, false);

        ctrlErrCode = view.findViewById(R.id.ctrlErrCode);

        int[] axisErrCode = {
                R.id.axis1ErrCode, R.id.axis2ErrCode, R.id.axis3ErrCode, R.id.axis4ErrCode, R.id.axis5ErrCode, R.id.axis6ErrCode
        };

        int[] axisPos = {
                R.id.axis1Pos, R.id.axis2Pos, R.id.axis3Pos, R.id.axis4Pos, R.id.axis5Pos, R.id.axis6Pos
        };

        int[] tcpPos = {
                R.id.tcpXPos, R.id.tcpYPos, R.id.tcpZPos, R.id.tcpRxPos, R.id.tcpRyPos, R.id.tcpRzPos
        };

        int[] jointPos = {
                R.id.joint1Pos, R.id.joint2Pos, R.id.joint3Pos, R.id.joint4Pos, R.id.joint5Pos, R.id.joint6Pos
        };

        for (int id : axisErrCode) {
            EditText editText = view.findViewById(id);
            axisErrCodeList.add(editText);
        }

        for (int id : axisPos) {
            EditText editText = view.findViewById(id);
            axisPosList.add(editText);
        }

        for (int id : tcpPos) {
            EditText editText = view.findViewById(id);
            tcpPosList.add(editText);
        }

        for (int id : jointPos) {
            EditText editText = view.findViewById(id);
            jointPosList.add(editText);
        }

        regularDataStatusTextView = view.findViewById(R.id.regularDataStatusTextView);

        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Your repetitive code here
                updateRegularData();

                // Schedule the next execution
                handler.postDelayed(this, 300);
            }
        }, 300);

        return view;
    }

    public void updateRegularData () {
        regularDataStatusTextView.setText(regularDataStatus);

        // Ctrl's error code
        ctrlErrCode.setText(ctrlErrorCode);

        // Axis error code (currently in Decimal, should be shown in Hex)
        axisErrCodeList.get(0).setText(axis1ErrCode);
        axisErrCodeList.get(1).setText(axis2ErrCode);
        axisErrCodeList.get(2).setText(axis3ErrCode);
        axisErrCodeList.get(3).setText(axis4ErrCode);
        axisErrCodeList.get(4).setText(axis5ErrCode);
        axisErrCodeList.get(5).setText(axis6ErrCode);

        // Actual axis position
        axisPosList.get(0).setText(axis1Pos);
        axisPosList.get(1).setText(axis2Pos);
        axisPosList.get(2).setText(axis3Pos);
        axisPosList.get(3).setText(axis4Pos);
        axisPosList.get(4).setText(axis5Pos);
        axisPosList.get(5).setText(axis6Pos);

        // TCP position
        tcpPosList.get(0).setText(xPos);
        tcpPosList.get(1).setText(yPos);
        tcpPosList.get(2).setText(zPos);
        tcpPosList.get(3).setText(rX);
        tcpPosList.get(4).setText(rY);
        tcpPosList.get(5).setText(rZ);

        // Joint position
        jointPosList.get(0).setText(joint1Pos);
        jointPosList.get(1).setText(joint2Pos);
        jointPosList.get(2).setText(joint3Pos);
        jointPosList.get(3).setText(joint4Pos);
        jointPosList.get(4).setText(joint5Pos);
        jointPosList.get(5).setText(joint6Pos);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null); // Remove all callbacks
        }
    }
}