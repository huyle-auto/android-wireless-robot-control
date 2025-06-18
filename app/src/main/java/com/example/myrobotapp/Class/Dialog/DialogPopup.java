package com.example.myrobotapp.Class.Dialog;

import static com.example.myrobotapp.Class.DataTable.ProgramFile.ProgramDataTableRowAdapter.posProgramTable;
import static com.example.myrobotapp.Class.DataTable.ProgramFile.ProgramDataTableRowAdapter.selectedProgramName;
import static com.example.myrobotapp.Class.FileManager.createTempTextFile;
import static com.example.myrobotapp.Class.FileManager.deleteFromDir;
import static com.example.myrobotapp.Class.FileManager.writeToDir;
import static com.example.myrobotapp.Fragments.Features.HistoryFragment.recordOperation;
import static com.example.myrobotapp.Fragments.Features.Settings.ConnectionFragment.activeConnectionKey;
import static com.example.myrobotapp.Fragments.Features.Settings.ConnectionFragment.globalSelectedBTDevice;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.myrobotapp.Class.Communication.PriorityCommunication;
import com.example.myrobotapp.Class.GlobalData;
import com.example.myrobotapp.Class.TCP.TCPClient;
import com.example.myrobotapp.Class.UserInput.KeyboardPopup;
import com.example.myrobotapp.Fragments.Features.Settings.ConnectionFragment;
import com.example.myrobotapp.R;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

public class DialogPopup {

    private Context dialogContext;
    public final PopupWindow popupWindow;
    public String actionType;
    TextView dialogTitle, dialogHint, dialogFirstInput, dialogSecondInput;
    Button dialogCancelButton, dialogConfirmButton;
    ImageView dialogIconImageView;

    public DialogPopup (Context context, String actionType){
        this.actionType = actionType;
        this.dialogContext = context;
        // Inflate the layout
        View popupView = LayoutInflater.from(dialogContext).inflate(R.layout.dialog_layout, null);

        // Initialize the popup window
        popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setClippingEnabled(false);

        // Dialog's fields initialization
        dialogTitle = popupView.findViewById(R.id.dialogTitle);
        dialogHint = popupView.findViewById(R.id.dialogHint);
        dialogFirstInput = popupView.findViewById(R.id.dialogFirstInput);
        dialogFirstInput.setOnClickListener(v -> {
            KeyboardPopup keyboardPopup = new KeyboardPopup(context, (EditText) dialogFirstInput, 0);
            keyboardPopup.showKeyboardPopup((Activity) context);
        });
        dialogSecondInput = popupView.findViewById(R.id.dialogSecondInput);

        dialogCancelButton = popupView.findViewById(R.id.dialogCancelButton);
        dialogCancelButton.setOnClickListener(v -> {
            onCancelClicked();
        });

        dialogConfirmButton = popupView.findViewById(R.id.dialogConfirmButton);
        dialogConfirmButton.setOnClickListener(v -> {
            onConfirmClicked();
        });

        dialogIconImageView = popupView.findViewById(R.id.dialogIconImageView);

        processAction(actionType);
    }

    private void processAction (String actionType){
        if ("NEW PROGRAM".equals(actionType)){
            makeText("Create new program", "Program Name: ", "Enter program name here...", "");
            dialogSecondInput.setVisibility(View.GONE);
        }
        if ("DELETE PROGRAM".equals(actionType)){
            makeText("Delete Program", "Permanently delete this program?", "", "");
            dialogFirstInput.setVisibility(View.GONE);
            dialogSecondInput.setVisibility(View.GONE);
        }
        if ("REBOOT".equals(actionType)){
            makeText("Controller Reboot", "Confirm reboot controller?", "", "");
            dialogFirstInput.setVisibility(View.GONE);
            dialogSecondInput.setVisibility(View.GONE);
        }
    }

    private void makeText(String dialogTitleText, String dialogHintText, String dialogFirstInputText, String dialogSecondInputText){
        dialogTitle.setText(dialogTitleText);
        dialogHint.setText(dialogHintText);
        dialogFirstInput.setHint(dialogFirstInputText);
        dialogSecondInput.setHint(dialogSecondInputText);
    }

    public void onConfirmClicked(){
        if ("NEW PROGRAM".equals(actionType)){
            // Check empty or invalid Program Name. Check if the same name already in Program List
            if (dialogFirstInput.getText().toString().isEmpty() || !isValidProjectName(dialogFirstInput.getText().toString())){
                Toast.makeText(dialogContext, "Invalid Program Name", Toast.LENGTH_SHORT).show();
                return;
            }

            // (NOT FINISHED): Check if same program with that name already existed

            File tempTextFile = createTempTextFile(dialogContext, "", "Anyname");
            writeToDir(dialogContext, "Program/" + dialogFirstInput.getText().toString(), dialogFirstInput.getText().toString() + ".txt", tempTextFile);
        }
        if ("DELETE PROGRAM".equals(actionType)){
            if ("-1".equals(posProgramTable)){
                Toast.makeText(dialogContext, "Choose a program first", Toast.LENGTH_SHORT).show();
                return;
            }
            deleteFromDir(dialogContext, "Program/" + selectedProgramName, null);
        }

        popupWindow.dismiss();

        if ("REBOOT".equals(actionType)){
            ConcurrentHashMap<String, TCPClient> activeConnections = GlobalData.getInstance().getActiveConnections();
            TCPClient tcpClient = activeConnections.get(activeConnectionKey);

            if (tcpClient == null && globalSelectedBTDevice == null) {
                Toast.makeText(dialogContext, "Server not found", Toast.LENGTH_SHORT).show();
                return;
            }

            // TCP (Prioritized)
            PriorityCommunication.ChannelType channelType = PriorityCommunication.checkAvailableChannel();
            if (channelType == PriorityCommunication.ChannelType.TCP) {
                // Run program
                tcpClient.rebootSecond('Y');
                Toast.makeText(dialogContext, "Requested controller reboot", Toast.LENGTH_SHORT).show();
                return;
            }

            // Bluetooth
            if (channelType == PriorityCommunication.ChannelType.Bluetooth) {
                ConnectionFragment.bluetoothHelper.rebootSecond(globalSelectedBTDevice, 'Y');
                Toast.makeText(dialogContext, "Requested controller reboot", Toast.LENGTH_SHORT).show();
                return;
            }

            showToast("No server available", false);
        }

        if ("OPEN_APP_INFO".equals(actionType)) {
            popupWindow.dismiss();  // Dismiss first before going to App Info page

            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", dialogContext.getPackageName(), null);
            intent.setData(uri);
            if (!(dialogContext instanceof Activity)) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            dialogContext.startActivity(intent);
        }

        popupWindow.dismiss();
    }

    public void onCancelClicked() {
        // Must send CANCEL signal if do not proceed reboot command
        if ("REBOOT".equals(actionType)) {
            ConcurrentHashMap<String, TCPClient> activeConnections = GlobalData.getInstance().getActiveConnections();
            TCPClient tcpClient = activeConnections.get(activeConnectionKey);

            if (tcpClient == null && globalSelectedBTDevice == null) {
                Toast.makeText(dialogContext, "Server not found", Toast.LENGTH_SHORT).show();
                return;
            }

            // TCP (Prioritized)
            PriorityCommunication.ChannelType channelType = PriorityCommunication.checkAvailableChannel();
            if (channelType == PriorityCommunication.ChannelType.TCP) {
                // Run program
                tcpClient.rebootSecond('N');
                Toast.makeText(dialogContext, "Reboot Cancelled", Toast.LENGTH_SHORT).show();
                popupWindow.dismiss();
                return;
            }

            // Bluetooth
            if (channelType == PriorityCommunication.ChannelType.Bluetooth) {
                ConnectionFragment.bluetoothHelper.rebootSecond(globalSelectedBTDevice, 'N');
                Toast.makeText(dialogContext, "Reboot Cancelled", Toast.LENGTH_SHORT).show();
                popupWindow.dismiss();
                return;
            }

            showToast("No server available", false);

            return;
        }
        popupWindow.dismiss();
    }

        public static boolean isValidProjectName(String projectName) {
            return projectName != null && projectName.matches("^[a-zA-Z0-9!&()'\\-_]{1,32}$");
        }

    public void showNumpadPopup(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(activity.findViewById(android.R.id.content).getWindowToken(), 0);
        }

        //Enable Immersive mode first (to avoid flashing navigation bar)
        enableImmersiveMode(popupWindow.getContentView());
        // Show popup at the center of the activity
        popupWindow.showAtLocation(activity.findViewById(android.R.id.content), Gravity.CENTER, 0, 0);
    }

    private void enableImmersiveMode(View view) {
        view.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );

        // Optional: Add a listener to re-apply immersive mode when it changes
        view.setOnSystemUiVisibilityChangeListener(visibility -> {
            if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                popupWindow.getContentView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                );
            }
        });

        // Keep immersive mode when EditText is focused
        view.getViewTreeObserver().addOnGlobalFocusChangeListener((oldFocus, newFocus) -> {
            if (newFocus instanceof EditText) {
                view.setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                );
            }
        });
    }

    // Adjusting fields' visibility
    public void setVisibility(View view, int visibility) {
        view.setVisibility(visibility);
    }

    public void setTitle(String text) {
        dialogTitle.setText(text);
    }

    public void setInfoHint(String text) {
        dialogHint.setText(text);
    }

    public void setImage(int drawableResId) {
        // Clear any previous image to avoid overlap or flicker
        Glide.with(dialogIconImageView.getContext()).clear(dialogIconImageView);
        dialogIconImageView.setImageDrawable(null);

        // Load new image
        Glide.with(dialogIconImageView.getContext())
                .load(drawableResId)
                .override(dialogIconImageView.getWidth(), dialogIconImageView.getHeight()) // Resize the image to fit your needs
                .into(dialogIconImageView);
    }

    private void showToast(String message, boolean longToast) {
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(dialogContext, message, longToast ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show()
        );
    }
}
