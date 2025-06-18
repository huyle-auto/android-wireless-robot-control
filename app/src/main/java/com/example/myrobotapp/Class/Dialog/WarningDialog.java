package com.example.myrobotapp.Class.Dialog;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import com.example.myrobotapp.Class.Privilege.SessionManager;
import com.example.myrobotapp.Class.SQLite.DatabaseHelper;
import com.example.myrobotapp.Class.SQLite.DatabaseManager;
import com.example.myrobotapp.R;

public class WarningDialog extends DialogPopup {
    private DialogActionListener listener;

    public WarningDialog(Context context, String actionType, DialogActionListener listener) {
        super(context, actionType);
        this.listener = listener;

        setVisibility(dialogIconImageView, View.VISIBLE);
        setImage(R.drawable.warning_dialog_icon);
        setVisibility(dialogFirstInput, View.GONE);
        setVisibility(dialogSecondInput, View.GONE);
        setVisibility(dialogCancelButton, View.GONE);

        setTitle("Warning");

    }

    @Override
    public void onConfirmClicked() {
        if (listener != null) {
            listener.onDialogConfirmed(actionType);
        }
        popupWindow.dismiss();
    }
}
