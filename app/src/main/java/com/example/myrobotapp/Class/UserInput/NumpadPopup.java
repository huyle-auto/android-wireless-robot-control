package com.example.myrobotapp.Class.UserInput;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.EditText;
import android.view.Gravity;
import android.widget.Toast;

import com.example.myrobotapp.R;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class NumpadPopup {

    private final PopupWindow popupWindow;
    private final EditText displayText;
    private int cursorPosition;
    private StringBuilder inputBuilder;
    EditText editText;
    private int filterType;
    private float touchX, touchY;  // Track touch positions for dragging

    // Format cells to allow max. 2 digits after decimal point "."
    DecimalFormat decimalFormatPoint = new DecimalFormat("0.00", new DecimalFormatSymbols(Locale.US));
    DecimalFormat decimalFormatParameter = new DecimalFormat();

    public NumpadPopup(Context context, EditText editText, int filterType) {
        // FILTER TYPE
        // 0. NO FILTER
        // 1. Max 2. digits after "." (for Joint/World Point)
        // 2. (Reserved)
        this.filterType = filterType;
        this.editText = editText;

        // Initialize the input builder
        inputBuilder = new StringBuilder(editText.getText());

        // Inflate the layout
        View popupView = LayoutInflater.from(context).inflate(R.layout.numpad_layout, null);

        // Initialize the popup window
        popupWindow = new PopupWindow(popupView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
        popupWindow.setClippingEnabled(false);

        // Initialize displayText
        displayText = popupView.findViewById(R.id.display_text);
        displayText.setText(editText.getText().toString());
        displayText.requestFocus();
        displayText.setShowSoftInputOnFocus(false);

        // Set up buttons
        setUpButtons(popupView);

        // Set Close button functionality
        Button btnClose = popupView.findViewById(R.id.btn_close);
        btnClose.setOnClickListener(v -> popupWindow.dismiss());

        // Enable dragging
        enableDragging(popupView);
    }

    private void setUpButtons(View view) {
        // Set up numpad buttons
        int[] buttonIds = {
                R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4,
                R.id.btn_5, R.id.btn_6, R.id.btn_7, R.id.btn_8, R.id.btn_9,
                R.id.btn_minus, R.id.btn_decimal, R.id.btn_delete
        };

        for (int id : buttonIds) {
            Button button = view.findViewById(id);
            button.setOnClickListener(this::onNumpadButtonClick);
        }

        // CLEAR
        Button btnClear = view.findViewById(R.id.btn_clear);
        Button btnOk = view.findViewById(R.id.btn_ok);

        btnClear.setOnClickListener(v -> {
            inputBuilder.setLength(0);
            displayText.setText("");
        });

        // OK
        btnOk.setOnClickListener(v -> {
            try {
                if (editText != null && inputBuilder.length() > 0) {
                    // CHECK FOR FILTER TYPE FIRST
                    // 0. No filter
                    if (filterType == 0) {
                        editText.setText(inputBuilder.toString());
                    }
                    // 1. Max 2. digits after "."
                    if (filterType == 1) {
                        double value = Double.parseDouble(inputBuilder.toString());
                        editText.setText(decimalFormatPoint.format(value));
                    }
                    // 2.
                    if (filterType == 2) {
                        float value = Float.parseFloat(displayText.getText().toString());
                        editText.setText(String.valueOf(value));
                    }

                    popupWindow.dismiss();
                } else {
                    Toast.makeText(btnOk.getContext(), "Cannot apply empty value", Toast.LENGTH_SHORT).show();
                }
            }
            catch (NumberFormatException e){
                Toast.makeText(btnOk.getContext(), "Invalid value format", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onNumpadButtonClick(View view) {
        Button button = (Button) view;
        String buttonText = button.getText().toString();

        // Get current Cursor Position. Do nothing if cursor is at start of displayText
        cursorPosition = displayText.getSelectionEnd();

        if (cursorPosition == 0 && buttonText.equals("⌫")){
            return;
        }

        if (buttonText.equals("⌫")) {
            if (inputBuilder.length() > 0) {
                System.out.println("Current inputBuilder value: " + inputBuilder + "and Cursor Position at: " + cursorPosition);
                inputBuilder.deleteCharAt(cursorPosition - 1);
                displayText.setText(inputBuilder.toString());
                cursorPosition--;
            }
            else {

            }
        } else {
            inputBuilder.insert(cursorPosition, buttonText);
            displayText.setText(inputBuilder.toString());
            cursorPosition++;
        }

        displayText.setText(inputBuilder.toString());
        displayText.setSelection(cursorPosition);
    }

    private void enableDragging(View popupView) {
        popupView.setOnTouchListener(new View.OnTouchListener() {
            private float initialX, initialY;
            private float initialTouchX, initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // Store initial touch coordinates
                        initialX = popupWindow.getContentView().getTranslationX();
                        initialY = popupWindow.getContentView().getTranslationY();
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        // Calculate the distance moved
                        float deltaX = event.getRawX() - initialTouchX;
                        float deltaY = event.getRawY() - initialTouchY;

                        // Update popup position
                        popupWindow.getContentView().setTranslationX(initialX + deltaX);
                        popupWindow.getContentView().setTranslationY(initialY + deltaY);
                        return true;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        // Optionally handle touch release and cancel events
                        return true;
                }
                return false;
            }
        });
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
        view.setOnSystemUiVisibilityChangeListener(visibility ->  {
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
}
