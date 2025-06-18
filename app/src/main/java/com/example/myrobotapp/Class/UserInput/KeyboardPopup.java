package com.example.myrobotapp.Class.UserInput;

import android.app.Activity;
import android.content.Context;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.example.myrobotapp.Class.DataTable.ProgramInput.ProgramInputCodeLineModel;
import com.example.myrobotapp.Class.DataTable.ProgramInput.ProgramInputRowAdapter;
import com.example.myrobotapp.R;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class KeyboardPopup {
    private final PopupWindow popupWindow;
    Button switchToCharacterButton, switchToSymbolButton;
    LinearLayout characterKeyboardView, symbolKeyboardView;
    private final EditText displayText;
    List<Button> keyButtons;
    private int cursorPosition;
    private StringBuilder inputBuilder;
    EditText editText;
    private int filterType;
    private float touchX, touchY;  // Track touch positions for dragging
    private boolean isShiftActive = false;

    // Format cells to allow max. 2 digits after decimal point "."
    DecimalFormat decimalFormatPoint = new DecimalFormat("#.##");
    DecimalFormat decimalFormatParameter = new DecimalFormat();

    public KeyboardPopup(Context context, EditText editText, int filterType) {
        // FILTER TYPE
        // 0. NO FILTER
        // 1. Max 2. digits after "." (for Joint/World Point)
        // 2. (Reserved)
        // 3. No filter
        // 4. Password (No filter & hide input)
        this.filterType = filterType;
        this.editText = editText;

        // Initialize the input builder
        inputBuilder = new StringBuilder(editText.getText());
        keyButtons = new ArrayList<>();

        // Inflate the layout
        View popupView = LayoutInflater.from(context).inflate(R.layout.keyboard_layout, null);

        // Initialize the popup window
        popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setClippingEnabled(false);

        // Initialize displayText
        displayText = popupView.findViewById(R.id.keyboard_display_text);
        displayText.setText(editText.getText().toString());
        displayText.requestFocus();
        displayText.setShowSoftInputOnFocus(false);

        // Switching buttons
        switchToCharacterButton = popupView.findViewById(R.id.switchToCharacterButton);
        switchToCharacterButton.setOnClickListener(v -> toggleKeyboard("CHARACTERS"));
        switchToSymbolButton = popupView.findViewById(R.id.switchToSymbolButton);
        switchToSymbolButton.setOnClickListener(v -> toggleKeyboard("SYMBOLS"));

        // Initialize whole Character/Symbol view
        characterKeyboardView = popupView.findViewById(R.id.characterKeyboardView);
        symbolKeyboardView = popupView.findViewById(R.id.symbolKeyboardView);

        // Set up key buttons
        setUpButtons(popupView);

        // Set Close button functionality
        Button btnClose = popupView.findViewById(R.id.btn_keyboard_close);
        btnClose.setOnClickListener(v -> popupWindow.dismiss());

        // Enable dragging
        enableDragging(popupView);
    }

    private void setUpButtons(View view) {
        // Set up numpad buttons
        int[] buttonIds = {
                // *SWITCH AND CLOSE button are assigned earlier

                // *Preview text is assigned earlier
                R.id.btn_keyboard_delete,

                // Keyboard input
                R.id.btn_keyboard_0, R.id.btn_keyboard_1, R.id.btn_keyboard_2, R.id.btn_keyboard_3, R.id.btn_keyboard_4,
                R.id.btn_keyboard_5, R.id.btn_keyboard_6, R.id.btn_keyboard_7, R.id.btn_keyboard_8, R.id.btn_keyboard_9,

                R.id.btn_keyboard_q, R.id.btn_keyboard_w, R.id.btn_keyboard_e, R.id.btn_keyboard_r, R.id.btn_keyboard_t,
                R.id.btn_keyboard_y, R.id.btn_keyboard_u, R.id.btn_keyboard_i, R.id.btn_keyboard_o, R.id.btn_keyboard_p,

                R.id.btn_keyboard_a, R.id.btn_keyboard_s, R.id.btn_keyboard_d, R.id.btn_keyboard_f, R.id.btn_keyboard_g,
                R.id.btn_keyboard_h, R.id.btn_keyboard_j, R.id.btn_keyboard_k, R.id.btn_keyboard_l,

                R.id.btn_keyboard_shift, R.id.btn_keyboard_z, R.id.btn_keyboard_x, R.id.btn_keyboard_c, R.id.btn_keyboard_v,
                R.id.btn_keyboard_b, R.id.btn_keyboard_n, R.id.btn_keyboard_m,

                // Symbol input
                R.id.btn_keyboard_tilde, R.id.btn_keyboard_exclamation, R.id.btn_keyboard_at, R.id.btn_keyboard_hash, R.id.btn_keyboard_dollar,
                R.id.btn_keyboard_percent, R.id.btn_keyboard_caret, R.id.btn_keyboard_asterisk, R.id.btn_keyboard_leftParenthesis, R.id.btn_keyboard_rightParenthesis,

                R.id.btn_keyboard_ampersand, R.id.btn_keyboard_apostrophe, R.id.btn_keyboard_quotation, R.id.btn_keyboard_verticalSlash, R.id.btn_keyboard_plus,
                R.id.btn_keyboard_minus, R.id.btn_keyboard_equal, R.id.btn_keyboard_colon, R.id.btn_keyboard_semicolon, R.id.btn_keyboard_lessThan, R.id.btn_keyboard_greaterThan,

                R.id.btn_keyboard_questionMark, R.id.btn_keyboard_slash, R.id.btn_keyboard_backLash, R.id.btn_keyboard_underscore, R.id.btn_keyboard_leftSquareBracket,
                R.id.btn_keyboard_rightSquareBracket, R.id.btn_keyboard_leftCurlyBracket, R.id.btn_keyboard_rightCurlyBracket,

                // Space bar
                R.id.btn_keyboard_space, R.id.btn_keyboard_dot, R.id.btn_keyboard_comma

                // *CLEAR AND OK buttons will be assigned after this
        };

        for (int id : buttonIds) {
            Button button = view.findViewById(id);
            // Add to List to capitalize if SHIFT is pressed
            keyButtons.add(button);

            if (button != null){
                button.setOnClickListener(this::onKeyboardButtonClick);
            }
            else {
                Toast.makeText(displayText.getContext(), "Null button assignment", Toast.LENGTH_SHORT).show();
            }
        }

        // CLEAR
        Button btnClear = view.findViewById(R.id.btn_keyboard_clear);
        Button btnOk = view.findViewById(R.id.btn_keyboard_ok);

        btnClear.setOnClickListener(v -> {
            inputBuilder.setLength(0);
            displayText.setText("");
        });

        // OK
        btnOk.setOnClickListener(v -> {
            try {
                if (editText != null && inputBuilder.length() >= 0) {
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
                    // 2. (Reserved)
                    if (filterType == 2) {
                        float value = Float.parseFloat(displayText.getText().toString());
                        editText.setText(String.valueOf((int) value));
                    }
                    // 3. No filter for KEYBOARD INPUT
                    if (filterType == 3) {
                        editText.setText(displayText.getText().toString());
                    }

                    // 4. No filter & Hide INPUT
                    if (filterType == 4) {
                        editText.setText(displayText.getText().toString());
                    }

                    popupWindow.dismiss();
                } else {
                    Toast.makeText(btnOk.getContext(), "Cannot apply empty value", Toast.LENGTH_SHORT).show();
                }
            }
            catch (NumberFormatException e){
                Toast.makeText(btnOk.getContext(), "Invalid input format", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onKeyboardButtonClick(View view) {
        Button button = (Button) view;
        String buttonText = button.getText().toString();

        // Get current Cursor Position. Do nothing if cursor is at start of displayText
        cursorPosition = displayText.getSelectionEnd();
        System.out.println("Current keyboard preview cursor position is: " + cursorPosition);

        if (cursorPosition == 0 && buttonText.equals("BACKSPACE")){
            return;
        }

        // DELETE (BACKSPACE) + SHIFT + SPACE
        switch (buttonText){
            case "BACKSPACE":
                if (inputBuilder.length() > 0) {
                    System.out.println("Current inputBuilder value: " + inputBuilder + "and Cursor Position at: " + cursorPosition);
                    inputBuilder.deleteCharAt(cursorPosition - 1);
                    displayText.setText(inputBuilder.toString());
                    cursorPosition--;
                } else {
                    Toast.makeText(displayText.getContext(), "Error in backspace button", Toast.LENGTH_SHORT).show();
                }
                // Append changes
                displayText.setText(inputBuilder.toString());
                displayText.setSelection(cursorPosition);
                break;

            case "SHIFT":
                isShiftActive = !isShiftActive; // Toggle SHIFT state

                // Iterate through all character buttons in the list and update their text
                for (Button keyButton : keyButtons) {
                    String currentText = keyButton.getText().toString();
                    if (isShiftActive) {
                        // Convert to uppercase if SHIFT is active
                        keyButton.setText(currentText.toUpperCase());
                    } else {
                        // Convert to lowercase if SHIFT is not active
                        if (!keyButton.getText().equals("SHIFT") && !keyButton.getText().equals("SPACE") && !keyButton.getText().equals("BACKSPACE")){
                            keyButton.setText(currentText.toLowerCase());
                        }
                    }
                }
                break;

            case "SPACE":
                inputBuilder.insert(cursorPosition, " ");
                displayText.setText(inputBuilder.toString());
                cursorPosition++;

                // Append changes
                displayText.setText(inputBuilder.toString());
                displayText.setSelection(cursorPosition);
                break;

            default:
                inputBuilder.insert(cursorPosition, buttonText);
                displayText.setText(inputBuilder.toString());
                cursorPosition++;

                // Append changes
                displayText.setText(inputBuilder.toString());
                displayText.setSelection(cursorPosition);
                break;
        }

    }

    private void toggleKeyboard(String buttonText){
        if (buttonText.equals("CHARACTERS")) {
            symbolKeyboardView.setVisibility(View.GONE);
            characterKeyboardView.setVisibility(View.VISIBLE);
        }
        else {
            symbolKeyboardView.setVisibility(View.VISIBLE);
            characterKeyboardView.setVisibility(View.GONE);
        }
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

    // Call this method to display KEYBOARD INPUT as popup window
    public void showKeyboardPopup(Activity activity) {
        // Disable system keyboard for the EditText
        editText.setShowSoftInputOnFocus(false); // For API 21+
        disableSystemKeyboard(activity, editText);

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

    public void hideInput() {
        displayText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
    }

    private void disableSystemKeyboard(Activity activity, EditText editText) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            editText.setShowSoftInputOnFocus(false);
        } else {
            editText.setInputType(InputType.TYPE_NULL);
            editText.setFocusable(false);
            editText.setFocusableInTouchMode(true);

            editText.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.performClick();  // Ensures proper click behavior
                }
                return true; // Consume touch event to prevent keyboard popup
            });

            editText.setOnClickListener(v -> {
                // Optional: Add custom logic if needed (e.g., opening your keyboard manually)
            });
        }
    }
}
