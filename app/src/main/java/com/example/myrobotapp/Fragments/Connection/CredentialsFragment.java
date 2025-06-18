package com.example.myrobotapp.Fragments.Connection;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.myrobotapp.Class.TCP.TCPClient;
import com.example.myrobotapp.Class.UserInput.NumpadPopup;
import com.example.myrobotapp.R;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CredentialsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CredentialsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public CredentialsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ConnectionFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CredentialsFragment newInstance(String param1, String param2) {
        CredentialsFragment fragment = new CredentialsFragment();
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

    Button connectButton;
    Button disconnectButton;
    EditText editText_IPAddress;
    EditText editText_Port;
    TextView TextView_status;
    TextView TextView_activeConnections;

    // Share IP Address and Port
    public static String sharedConnectionKey = "";

    // Executor service to handle multiple connections concurrently
    private ExecutorService executorService;

    // A map to keep track of active connections (IP + port -> TCPClient)
    private ConcurrentHashMap<String, TCPClient> activeConnections;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_credentials, container, false);

        // Initialize connection components
        connectButton = view.findViewById(R.id.connectButton);
        disconnectButton = view.findViewById(R.id.disconnectButton);
        editText_IPAddress = view.findViewById(R.id.IP_Address);
        editText_IPAddress.setOnClickListener(v -> {
            NumpadPopup numpadPopup = new NumpadPopup(this.requireActivity(), editText_IPAddress, 0);
            numpadPopup.showNumpadPopup(this.requireActivity());
        });
        editText_Port = view.findViewById(R.id.Port);
        editText_Port.setOnClickListener(v -> {
            NumpadPopup numpadPopup = new NumpadPopup(this.requireActivity(), editText_Port, 0);
            numpadPopup.showNumpadPopup(this.requireActivity());
        });
        TextView_status = view.findViewById(R.id.status);

        // Initialize ExecutorService and activeConnections
        executorService = Executors.newCachedThreadPool();
        activeConnections = new ConcurrentHashMap<>();

        // Press connectButton --> connect to TCP servers
        connectButton.setOnClickListener(this::connectToServers);
        disconnectButton.setOnClickListener(this::disconnectToServer);

        return view;
    }

    public static boolean validateIPAddress(final String ip) {
        String PATTERN = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";

        return ip.matches(PATTERN);
    }

    public void connectToServers(View view){

        // Handling invalid IPv4
        String ipAddress = editText_IPAddress.getText().toString().trim();
        if (!validateIPAddress(ipAddress)){
            TextView_status.setText("Status: Invalid IPv4 address !");
            return;
        }

        // Handling port number Exception
        int port;
        try {
            port = Integer.parseInt(editText_Port.getText().toString());
        }
        catch (NumberFormatException e){
            TextView_status.setText("Status: Invalid Port number !");
            return;
        }

        // Create a unique key for each connection using IP + Port
        String connectionKey = ipAddress + ":" + port;

        // Check if a connection to this server already exists
        if (activeConnections.containsKey(connectionKey) && activeConnections.get(connectionKey).isConnected()) {
            String status = "Status: Already connected to " + connectionKey;
            TextView_status.setText(status);
            return;
        }

        // If not connected, run connection in the background
        executorService.submit(() -> {
            TCPClient tcpClient = new TCPClient();
            try {
                tcpClient.startConnection(ipAddress, port);
                activeConnections.put(connectionKey, tcpClient); // Save the connection in the map
                requireActivity().runOnUiThread(() -> TextView_status.setText("Status: Connected to host " + connectionKey));
                sharedConnectionKey = connectionKey;

                // Continuously monitor connection
                //monitorConnection(connectionKey, tcpClient);
                // Show active connections to UI
                //showActiveConnections(view);
            }
            catch (IOException e) {
                requireActivity().runOnUiThread(() -> TextView_status.setText("Status: Failed to connect to host " + connectionKey));
            }
        });
    }

    public void disconnectToServer(View view){

        // Handling invalid IPv4
        String ipAddress = editText_IPAddress.getText().toString().trim();
        if (!validateIPAddress(ipAddress)){
            requireActivity().runOnUiThread(() ->TextView_status.setText("Status: Invalid IPv4 address !"));
            return;
        }

        // Handling port number Exception
        int port;
        try {
            port = Integer.parseInt(editText_Port.getText().toString());
        }
        catch (NumberFormatException e){
            requireActivity().runOnUiThread(() ->TextView_status.setText("Status: Invalid Port number !"));
            return;
        }

        // Get the specific tcpClient from hashmap using connectionKey
        // Caution (completed): Block pressing disconnectButton if client already disconnected (otherwise IOException)
        new Thread(()->{
            String connectionKey = ipAddress + ":" + port;
            TCPClient tcpClient = activeConnections.get(connectionKey);
            if (tcpClient == null){
                String status = "Status: No active connection to host " + connectionKey;
                requireActivity().runOnUiThread(() ->TextView_status.setText(status));
                return;
            }
            if (tcpClient.isAlive()){
//                tcpClient.stopConnection();
                activeConnections.remove(connectionKey);
                String status = "Status: Disconnected to host " + connectionKey;
                requireActivity().runOnUiThread(() ->TextView_status.setText(status));

                // Show active connections to UI
                //showActiveConnections(view);
            }
        }).start();

    }

    // (Deprecated)...
    // Heartbeat Mechanism: Handling server close / false connection interruption
    // Upcoming Feature: Switch to Bluetooth connection and vice versa if one dies
    private void monitorConnection(String connectionKey, TCPClient tcpClient ){
        executorService.submit(() -> {
            while (tcpClient.isConnected()){
                try{
                    Thread.sleep(1000);
                }
                catch (InterruptedException e){
                    Thread.currentThread().interrupt();
                    break;
                }

                // If server disconnected, remove connectionKey from hashmap
                if(!tcpClient.isConnected()){
                    requireActivity().runOnUiThread(() -> {
                        String status = "Status: Disconnected to host " + connectionKey;
                        TextView_status.setText(status);
                        activeConnections.remove(connectionKey);
                    });
                }
            }
        });
    }

    private void showActiveConnections (View view){
        TextView_activeConnections = view.findViewById(R.id.activeConnections);
        String currentAC = activeConnections.toString();
        TextView_activeConnections.setText(currentAC);
    }
}