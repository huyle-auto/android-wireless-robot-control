package com.example.myrobotapp.Fragments.Features.Settings;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.EditText;

import android.widget.Toast;

import com.example.myrobotapp.Class.UserInput.NumpadPopup;
import com.example.myrobotapp.R;
import com.google.android.material.slider.Slider;

public class SpeedFragment extends Fragment {

    private Slider[] sliders;
    private EditText[] editTexts;
    public static int[] orgValue = {30, 50, 70, 100};
    private EditText activeEditText;
    private int activeIndex = -1; // Track the index of the active EditText

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_speed, container, false);

        // Initialize Sliders and EditTexts arrays
        sliders = new Slider[]{
                view.findViewById(R.id.lowSpeedValueSlider),
                view.findViewById(R.id.mediumSpeedValueSlider),
                view.findViewById(R.id.highSpeedValueSlider),
                view.findViewById(R.id.topSpeedValueSlider)
        };

        editTexts = new EditText[]{
                view.findViewById(R.id.lowSpeedValueEditText),
                view.findViewById(R.id.mediumSpeedValueEditText),
                view.findViewById(R.id.highSpeedValueEditText),
                view.findViewById(R.id.topSpeedValueEditText)
        };

        // Initialize each Slider and setup CalcDialog trigger for each EditText
        for (int i = 0; i < sliders.length; i++) {
            final int index = i;
            sliders[i].setValue(orgValue[i]);
            editTexts[i].setText(String.valueOf(orgValue[i]));
            setupSlider(sliders[i], editTexts[i], index);
        }

        editTexts[0].setOnClickListener(v -> {
            NumpadPopup numpadPopup = new NumpadPopup(this.requireActivity(), editTexts[0], 2);
            numpadPopup.showNumpadPopup(this.requireActivity());
            editTexts[0].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    sliders[0].setValue(Float.parseFloat(editTexts[0].getText().toString()));
                }
            });
        });


        editTexts[1].setOnClickListener(v -> {
            NumpadPopup numpadPopup = new NumpadPopup(this.requireActivity(), editTexts[1], 2);
            numpadPopup.showNumpadPopup(this.requireActivity());
            editTexts[1].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    sliders[1].setValue(Float.parseFloat(editTexts[1].getText().toString()));
                }
            });
        });

        editTexts[2].setOnClickListener(v -> {
            NumpadPopup numpadPopup = new NumpadPopup(this.requireActivity(), editTexts[2], 2);
            numpadPopup.showNumpadPopup(this.requireActivity());
            editTexts[2].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    sliders[2].setValue(Float.parseFloat(editTexts[2].getText().toString()));
                }
            });
        });


        editTexts[3].setOnClickListener(v -> {
            NumpadPopup numpadPopup = new NumpadPopup(this.requireActivity(), editTexts[3], 2);
            numpadPopup.showNumpadPopup(this.requireActivity());
            editTexts[3].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    sliders[3].setValue(Float.parseFloat(editTexts[3].getText().toString()));
                }
            });
        });


        return view;
    }

    private void setupSlider(Slider slider, EditText editText, int index) {
        slider.addOnChangeListener((s, value, fromUser) -> {
            int newValue = (int) value;
            if (isValidSpeed(newValue, index)) {
                editText.setText(String.valueOf(newValue));
                orgValue[index] = newValue;
            } else {
                slider.setValue(orgValue[index]);
                Toast.makeText(getActivity(), "Values must satisfy: LOW < MEDIUM < HIGH < TOP", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isValidSpeed(int value, int index) {
        return (index == 0 || value > sliders[index - 1].getValue()) &&
                (index == sliders.length - 1 || value < sliders[index + 1].getValue());
    }

}


