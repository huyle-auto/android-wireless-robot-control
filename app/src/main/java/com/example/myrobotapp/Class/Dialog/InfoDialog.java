package com.example.myrobotapp.Class.Dialog;

import android.content.Context;
import android.opengl.Visibility;
import android.view.View;

import com.example.myrobotapp.R;

public class InfoDialog extends DialogPopup{
    public InfoDialog(Context context, String actionType) {
        super(context, actionType);
        setVisibility(dialogIconImageView, View.VISIBLE);
        setImage(R.drawable.info_dialog_icon);
        setVisibility(dialogFirstInput, View.GONE);
        setVisibility(dialogSecondInput, View.GONE);
        setVisibility(dialogCancelButton, View.GONE);

        setTitle("Information");
    }
}
