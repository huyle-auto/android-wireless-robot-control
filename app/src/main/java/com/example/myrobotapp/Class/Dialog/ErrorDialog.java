package com.example.myrobotapp.Class.Dialog;

import android.content.Context;
import android.view.View;

import com.example.myrobotapp.R;

public class ErrorDialog extends DialogPopup{

    public ErrorDialog(Context context, String actionType) {
        super(context, actionType);
        setVisibility(dialogIconImageView, View.VISIBLE);
        setImage(R.drawable.error_dialog_icon);
        setVisibility(dialogFirstInput, View.GONE);
        setVisibility(dialogSecondInput, View.GONE);
        setVisibility(dialogCancelButton, View.GONE);

        setTitle("Error");
    }

    private void acknowledgeError() {

    }
}
