package com.example.myrobotapp;

import static com.example.myrobotapp.Class.FileManager.copyFileFromAssets;
import static com.example.myrobotapp.Class.FileManager.copyFolderFromAssets;
import static com.example.myrobotapp.Class.Privilege.PrivilegeManager.loadPrivileges;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myrobotapp.Class.EchoServer;
import com.example.myrobotapp.Class.FirstLaunchChecker;
import com.example.myrobotapp.Class.Privilege.PrivilegeManager;
import com.example.myrobotapp.Class.SQLite.DatabaseManager;

public class MainActivity extends AppCompatActivity {

    EchoServer echoMultiServer1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        // Force early landscape orientation
//        int currentOrientation = getResources().getConfiguration().orientation;
//        if (currentOrientation != Configuration.ORIENTATION_LANDSCAPE) {
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//            return;
//        }

        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        configureNextButton();
        enableImmersiveMode();
        userFileInit();
        rbacInit();
    }

    private void enableImmersiveMode() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    private void configureNextButton(){
        // Initiate a Button by assigning a Button in UI to it
        Button nextButton = findViewById(R.id.nextButton);

        // Configure Listener for the Button to be triggered on a click
        nextButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, HomeScreenActivity.class);
            startActivity(intent);
        });
    }

    private void userFileInit(){
        FirstLaunchChecker checker = new FirstLaunchChecker();
        try {
            if (checker.isFirstLaunch(this)) {
                copyFileFromAssets(this, "ControllerConfig.txt", "ControllerSettings/default","ControllerConfig.txt");
                copyFileFromAssets(this, "ControllerConfig.txt", "ControllerSettings/custom","ControllerConfig.txt");
                Toast.makeText(this, "Loaded Robot configuration", Toast.LENGTH_SHORT).show();

                copyFolderFromAssets(this, "TemplateProgram", "Program");
                copyFolderFromAssets(this, "DeviceSettings", "DeviceSettings");
            }
        } catch (Exception e){
            System.out.println("Initiating files error: " + e.getMessage());
        }

    }

    private void rbacInit() {
        // Create database (if not exist) and add default users
        DatabaseManager.getInstance(getApplicationContext());
        PrivilegeManager.loadPrivileges(this);
    }

    private void showToast(String message, boolean longToast) {

        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(this, message, longToast ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show()
        );
    }
}