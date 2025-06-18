package com.example.myrobotapp.Fragments.Features.Settings;

import static com.example.myrobotapp.Class.ProtocolReceive.programRunningStatus;
import static com.example.myrobotapp.Fragments.Features.HistoryFragment.recordOperation;
import static com.example.myrobotapp.Fragments.Features.Settings.ConnectionFragment.activeConnectionKey;
import static com.example.myrobotapp.Fragments.Features.Settings.ConnectionFragment.globalSelectedBTDevice;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.myrobotapp.Class.Communication.PriorityCommunication;
import com.example.myrobotapp.Class.Dialog.InfoDialog;
import com.example.myrobotapp.Class.GlobalData;
import com.example.myrobotapp.Class.TCP.TCPClient;
import com.example.myrobotapp.Class.Dialog.DialogPopup;
import com.example.myrobotapp.R;

import java.util.concurrent.ConcurrentHashMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RebootFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RebootFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public RebootFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RebootFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RebootFragment newInstance(String param1, String param2) {
        RebootFragment fragment = new RebootFragment();
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

    Button rebootButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_reboot, container, false);

        rebootButton = view.findViewById(R.id.rebootButton);
        rebootButton.setOnClickListener(v -> {
            ConcurrentHashMap<String, TCPClient> activeConnections = GlobalData.getInstance().getActiveConnections();
            TCPClient tcpClient = activeConnections.get(activeConnectionKey);

            if (tcpClient == null && globalSelectedBTDevice == null) {
                Toast.makeText(this.requireContext(), "Server not found", Toast.LENGTH_SHORT).show();
                return;
            }

            // TCP (Prioritized)
            PriorityCommunication.ChannelType channelType = PriorityCommunication.checkAvailableChannel();
            if (channelType == PriorityCommunication.ChannelType.TCP) {
                // Run program
                tcpClient.rebootFirst();
                DialogPopup dialogPopup = new DialogPopup(this.requireContext(), "REBOOT");
                dialogPopup.showNumpadPopup(this.requireActivity());
                recordOperation("request REBOOT CONTROLLER");
                return;
            }

            // Bluetooth
            if (channelType == PriorityCommunication.ChannelType.Bluetooth) {
                ConnectionFragment.bluetoothHelper.rebootFirst(globalSelectedBTDevice);
                DialogPopup dialogPopup = new DialogPopup(this.requireContext(), "REBOOT");
                dialogPopup.showNumpadPopup(this.requireActivity());
                recordOperation("request REBOOT CONTROLLER");
                return;
            }

            showToast("No server available", false);
        });

        return view;
    }

    private void showToast(String message, boolean longToast) {
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(this.requireContext(), message, longToast ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show()
        );
    }
}