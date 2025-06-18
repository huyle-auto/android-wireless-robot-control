package com.example.myrobotapp.Fragments.Features.Settings;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myrobotapp.Class.Dialog.WarningDialog;
import com.example.myrobotapp.Class.Privilege.SessionManager;
import com.example.myrobotapp.Class.SQLite.DatabaseHelper;
import com.example.myrobotapp.Class.SQLite.DatabaseManager;
import com.example.myrobotapp.Class.UserInput.KeyboardPopup;
import com.example.myrobotapp.R;


import org.mindrot.jbcrypt.BCrypt;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UserFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public UserFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UserFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UserFragment newInstance(String param1, String param2) {
        UserFragment fragment = new UserFragment();
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

    TextView currentUsernameTextView, currentRoleTextView;
    EditText usernameEditText, passwordEditText;
    Button loginButton, logoutButton;
    CheckBox showPasswordCheckBox;

    Button addUserButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user, container, false);

        currentUsernameTextView = view.findViewById(R.id.currentUsernameTextView);
        currentRoleTextView = view.findViewById(R.id.currentRoleTextView);
        usernameEditText = view.findViewById(R.id.usernameEditText);
        usernameEditText.setShowSoftInputOnFocus(false);
        passwordEditText = view.findViewById(R.id.passwordEditText);
        passwordEditText.setShowSoftInputOnFocus(false);
        loginButton = view.findViewById(R.id.loginButton);
        logoutButton = view.findViewById(R.id.logoutButton);
        showPasswordCheckBox = view.findViewById(R.id.showPasswordCheckBox);

        addUserButton = view.findViewById(R.id.addUserButton);

        init();

        return view;
    }

    public void init() {
        // Show keyboard
        usernameEditText.setOnClickListener(v -> {
            KeyboardPopup keyboardPopup = new KeyboardPopup(this.requireContext(), usernameEditText, 3);
            keyboardPopup.showKeyboardPopup(this.requireActivity());
        });

        passwordEditText.setOnClickListener(v -> {
            KeyboardPopup keyboardPopup = new KeyboardPopup(this.requireContext(), passwordEditText, 3);
            keyboardPopup.hideInput();
            keyboardPopup.showKeyboardPopup(this.requireActivity());
        });

        // Show/hide password
        showPasswordCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            } else {
                // Hide password
                passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
            // Move the cursor to the end
            passwordEditText.setSelection(passwordEditText.getText().length());
        });

        // Login confirm
        loginButton.setOnClickListener(v -> {
            login();
        });

        // Logout confirm
        logoutButton.setOnClickListener(v -> {
            WarningDialog warningDialog = new WarningDialog(
                    requireContext(),
                    "Logout",
                    actionType -> {
                        if ("Logout".equals(actionType)) {
                            logout();
                        }
                    }
            );
            warningDialog.setInfoHint("Logging out from this account takes you back to Operation Level");
            warningDialog.showNumpadPopup(requireActivity());
        });


        // Add User confirm
        addUserButton.setOnClickListener(v -> {
            addUser();
        });
    }

    private void login() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this.requireContext(), "Empty username or password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate password
        DatabaseHelper dbHelper = DatabaseManager.getInstance(this.requireContext()).getHelper();
        if (dbHelper.isLoginValid(username, password)) {
            Toast.makeText(this.requireContext(), "Login successfully", Toast.LENGTH_SHORT).show();
            // Get user role if login succeed
            String userRole = dbHelper.getUserRole(username);
            SessionManager.setUserRole(userRole);

            // Update current username and role
            currentUsernameTextView.setText(username);
            currentRoleTextView.setText(userRole);

            // Erase old username & password
            usernameEditText.setText("");
            passwordEditText.setText("");
        }
        else {
            Toast.makeText(this.requireContext(), "Wrong username or password", Toast.LENGTH_SHORT).show();
        }
    }

    private void logout() {
        Toast.makeText(requireContext(), "Logged out", Toast.LENGTH_SHORT).show();
        SessionManager.setUserRole("Operator");
        currentUsernameTextView.setText("");
        currentRoleTextView.setText("Operator");
    }

    private void addUser() {
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this.requireContext(), "Empty username or password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Hash new user's password
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        // Add user to database
        DatabaseHelper dbHelper = DatabaseManager.getInstance(this.requireContext()).getHelper();
        dbHelper.addUser(username, hashedPassword, "Engineer");
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(this.requireContext());

        // Default user role is OPERATOR
        SessionManager.setUserRole("Operator");
    }
}