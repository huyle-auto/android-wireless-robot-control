package com.example.myrobotapp.Fragments.Features;

import static com.example.myrobotapp.Class.DataTable.Joint.JointDataTableRowAdapter.posJointTable;
import static com.example.myrobotapp.Class.DataTable.World.WorldDataTableRowAdapter.posWorldTable;
import static com.example.myrobotapp.Fragments.Features.Settings.ConnectionFragment.activeConnectionKey;
import static com.example.myrobotapp.HomeScreenActivity.speedLevel;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.myrobotapp.Class.DataTable.Joint.JointDataSharedViewModel;
import com.example.myrobotapp.Class.DataTable.Joint.JointDataTableRowAdapter;
import com.example.myrobotapp.Class.DataTable.Joint.JointDataTableRowModel;
import com.example.myrobotapp.Class.DataTable.World.WorldDataSharedViewModel;
import com.example.myrobotapp.Class.DataTable.World.WorldDataTableRowAdapter;
import com.example.myrobotapp.Class.DataTable.World.WorldDataTableRowModel;
import com.example.myrobotapp.Class.GlobalData;
import com.example.myrobotapp.Class.Privilege.PrivilegeManager;
import com.example.myrobotapp.Class.Privilege.SessionManager;
import com.example.myrobotapp.Class.TCP.TCPClient;
import com.example.myrobotapp.R;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DataFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DataFragment extends Fragment implements SessionManager.RoleChangeListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public DataFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DataFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DataFragment newInstance(String param1, String param2) {
        DataFragment fragment = new DataFragment();
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

    FrameLayout dataFrameLayout;
    public static JointDataSharedViewModel jointDataSharedViewModel;
    private JointDataTableRowAdapter jointDataTableRowAdapter;
    public static WorldDataSharedViewModel worldDataSharedViewModel;
    private WorldDataTableRowAdapter worldDataTableRowAdapter;
    private RecyclerView worldPointRecyclerView, jointPointRecyclerView;
    private Button addWorldPointButton,  deleteWorldPointButton, goToWorldPointButton;
    private Button addJointPointButton,  deleteJointPointButton, goToJointPointButton;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_data, container, false);

        // Assign buttons
        // JOINT

        // WORLD: ADD + DELETE + GO TO button
        addWorldPointButton = view.findViewById(R.id.addWorldPointButton);
        addWorldPointButton.setOnClickListener(v -> addWorldRow());

        deleteWorldPointButton = view.findViewById(R.id.deleteWorldPointButton);
        deleteWorldPointButton.setOnClickListener(v -> deleteWorldRow());

        goToWorldPointButton = view.findViewById(R.id.goToWorldPointButton);
        goToWorldPointButton.setOnClickListener(v -> goToWorldPosition());

        // JOINT: ADD + DELETE + GO TO button
        addJointPointButton = view.findViewById(R.id.addJointPointButton);
        addJointPointButton.setOnClickListener(v -> addJointRow());

        deleteJointPointButton = view.findViewById(R.id.deleteJointPointButton);
        deleteJointPointButton.setOnClickListener(v -> deleteJointRow());

        goToJointPointButton = view.findViewById(R.id.goToJointPointButton);
        goToJointPointButton.setOnClickListener(v -> goToJointPosition());

        // Setup Shared Data and Recycler View
        // WORLD
        worldPointRecyclerView = view.findViewById(R.id.worldPointRecyclerView);
        worldDataSharedViewModel = new ViewModelProvider(requireActivity()).get(WorldDataSharedViewModel.class);
        worldDataSharedViewModel.getInputWorldData().observe(getViewLifecycleOwner(), this::setWorldRecyclerView);

        // JOINT
        jointPointRecyclerView = view.findViewById(R.id.jointPointRecyclerView);
        jointDataSharedViewModel = new ViewModelProvider(requireActivity()).get(JointDataSharedViewModel.class);
        jointDataSharedViewModel.getInputJointData().observe(getViewLifecycleOwner(), this::setJointRecyclerView);

        // Remove focus on selected Joint & World points
        dataFrameLayout = view.findViewById(R.id.dataFrameLayout);
        dataFrameLayout.setOnClickListener(v -> {
            posJointTable = "-1";
            jointDataTableRowAdapter.notifyDataSetChanged();
            posWorldTable = "-1";
            worldDataTableRowAdapter.notifyDataSetChanged();
        });

        return view;
    }

    private void setWorldRecyclerView(List<WorldDataTableRowModel> inputWorldData) {
        worldPointRecyclerView.setHasFixedSize(true);;
        worldPointRecyclerView.setLayoutManager(new LinearLayoutManager(this.requireActivity()));

        List<WorldDataTableRowModel> dataList = getWorldList(inputWorldData);
        worldDataTableRowAdapter = new WorldDataTableRowAdapter(this.requireActivity(), dataList);
        worldPointRecyclerView.setAdapter(worldDataTableRowAdapter);
        worldDataTableRowAdapter.notifyDataSetChanged();

    }

    private List<WorldDataTableRowModel> getWorldList(List<WorldDataTableRowModel> inputWorldData) {
        List<WorldDataTableRowModel> worldDataList = new ArrayList<>();

        for (int i=0; i<inputWorldData.size(); i++){
            if (inputWorldData.size() >= 0){

                String no = Integer.toString(i);
                String x = inputWorldData.get(i).getX();
                String y = inputWorldData.get(i).getY();
                String z = inputWorldData.get(i).getZ();
                String rX = inputWorldData.get(i).getRx();
                String rY = inputWorldData.get(i).getRy();
                String rZ = inputWorldData.get(i).getRz();

                // Add row Data to list of rows
                worldDataList.add(new WorldDataTableRowModel(no, x, y, z, rX, rY, rZ));
            }
        }

        // Debug
//        System.out.println("getList() result: ");
//        for (WorldDataTableRowModel item:inputWorldData){
//            System.out.println(item.getX()+","+ item.getY()+ ","+ item.getZ()+"," + item.getRx()+","+ item.getRy()+"," +item.getRz());
//        }
//        System.out.println("getList() size: " + inputWorldData.size());

        return worldDataList;
    }

    private void setJointRecyclerView(List<JointDataTableRowModel> inputPointData) {
        jointPointRecyclerView.setHasFixedSize(true);;
        jointPointRecyclerView.setLayoutManager(new LinearLayoutManager(this.requireActivity()));

        List<JointDataTableRowModel> dataList = getJointList(inputPointData);
        jointDataTableRowAdapter = new JointDataTableRowAdapter(this.requireActivity(), dataList);
        jointPointRecyclerView.setAdapter(jointDataTableRowAdapter);
        jointDataTableRowAdapter.notifyDataSetChanged();
    }

    private List<JointDataTableRowModel> getJointList(List<JointDataTableRowModel> inputJointData) {
        List<JointDataTableRowModel> jointDataList = new ArrayList<>();

        for (int i=0; i < inputJointData.size(); i++){
            if (inputJointData.size() >= 0){

                String no = Integer.toString(i);
                String j1 = inputJointData.get(i).getJ1();
                String j2 = inputJointData.get(i).getJ2();
                String j3 = inputJointData.get(i).getJ3();
                String j4 = inputJointData.get(i).getJ4();
                String j5 = inputJointData.get(i).getJ5();
                String j6 = inputJointData.get(i).getJ6();

                // Add row Data to list of rows
                jointDataList.add(new JointDataTableRowModel(no, j1, j2, j3, j4, j5, j6));
            }
        }

        return jointDataList;
    }

    // WORLD ROW PROCESSING
    private void deleteWorldRow(){
        // Retrieve current position (chosen by row get position)
        if (posWorldTable != null && !("-1").equals(posWorldTable)){
            // Delete from Shared Data (automatic update)
            worldDataSharedViewModel.deleteWorldData(Integer.parseInt(posWorldTable));
        }
        else {
            Toast.makeText(this.requireContext(), "Choose a point to delete first", Toast.LENGTH_SHORT).show();
        }
    }

    // Because of no duplicate point rule, user must edit before adding new empty row
    private void addWorldRow(){
        WorldDataTableRowModel data = new WorldDataTableRowModel("","", "", "", "", "", "");
        worldDataSharedViewModel.addWorldData(data);
    }


    public static void saveWorldRow(int currentRowPosition, int columnIndex, String value){
        if (posWorldTable != null){
            worldDataSharedViewModel.saveWorldData(currentRowPosition, columnIndex, value);
        }
    }

    public void goToWorldPosition(){
        ConcurrentHashMap<String, TCPClient> activeConnections = GlobalData.getInstance().getActiveConnections();
        TCPClient tcpClient = activeConnections.get(activeConnectionKey);

        // Take static variable coordinateState, then define Speed Level (LOW-MEDIUM-HIGH), then position)
        String[] position = {"", "", "", "", "", ""};
        if (tcpClient != null && posWorldTable != null){
            position[0] = worldDataSharedViewModel.getInputWorldData().getValue().get(Integer.parseInt(posWorldTable)).getX();
            position[1] = worldDataSharedViewModel.getInputWorldData().getValue().get(Integer.parseInt(posWorldTable)).getY();
            position[2] = worldDataSharedViewModel.getInputWorldData().getValue().get(Integer.parseInt(posWorldTable)).getZ();
            position[3] = worldDataSharedViewModel.getInputWorldData().getValue().get(Integer.parseInt(posWorldTable)).getRx();
            position[4] = worldDataSharedViewModel.getInputWorldData().getValue().get(Integer.parseInt(posWorldTable)).getRy();
            position[5] = worldDataSharedViewModel.getInputWorldData().getValue().get(Integer.parseInt(posWorldTable)).getRz();
            tcpClient.moveToPosition(2, speedLevel, position);
        }
        else{
            Toast.makeText(this.getActivity(), "Server not found", Toast.LENGTH_SHORT).show();
        }
    }

    // JOINT ROW PROCESSING
    // Because of no duplicate point rule, user must edit before adding new empty row
    private void deleteJointRow(){
        // Retrieve current position (chosen by row get position)
        if (posJointTable != null && !("-1").equals(posJointTable)){
            // Delete from Shared Data (automatic update)
            jointDataSharedViewModel.deleteJointData(Integer.parseInt(posJointTable));
        }
        else {
            Toast.makeText(this.requireContext(), "Choose a point to delete first", Toast.LENGTH_SHORT).show();
        }
    }

    private void addJointRow(){
        JointDataTableRowModel data = new JointDataTableRowModel("","", "", "", "", "", "");
        jointDataSharedViewModel.addJointData(data);
    }

    public static void saveJointRow(int currentRowPosition, int columnIndex, String value){
        if (posJointTable != null){
            jointDataSharedViewModel.saveJointData(currentRowPosition, columnIndex, value);
        }
        System.out.println("saveJointRow() method is called with value: " + value);
    }

    public void goToJointPosition(){
        ConcurrentHashMap<String, TCPClient> activeConnections = GlobalData.getInstance().getActiveConnections();
        TCPClient tcpClient = activeConnections.get(activeConnectionKey);

        // Take static variable coordinateState, then define Speed Level (LOW-MEDIUM-HIGH), then position)
        String[] position = {"", "", "", "", "", ""};
        if (tcpClient != null && posJointTable != null){
            position[0] = jointDataSharedViewModel.getInputJointData().getValue().get(Integer.parseInt(posJointTable)).getJ1();
            position[1] = jointDataSharedViewModel.getInputJointData().getValue().get(Integer.parseInt(posJointTable)).getJ2();
            position[2] = jointDataSharedViewModel.getInputJointData().getValue().get(Integer.parseInt(posJointTable)).getJ3();
            position[3] = jointDataSharedViewModel.getInputJointData().getValue().get(Integer.parseInt(posJointTable)).getJ4();
            position[4] = jointDataSharedViewModel.getInputJointData().getValue().get(Integer.parseInt(posJointTable)).getJ5();
            position[5] = jointDataSharedViewModel.getInputJointData().getValue().get(Integer.parseInt(posJointTable)).getJ6();
            tcpClient.moveToPosition(1, speedLevel, position);
        }
        else{
            Toast.makeText(this.getActivity(), "Server not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUI(String role) {
        // Get the list of allowed fields for the current role
        List<String> unAllowedFields = PrivilegeManager.getUnAllowedFields(role, "DataFragment");

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