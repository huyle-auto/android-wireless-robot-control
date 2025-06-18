package com.example.myrobotapp.Class;

import android.content.Context;
import android.content.SharedPreferences;

public class FirstLaunchChecker {

    public boolean isFirstLaunch(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        boolean isFirstLaunch = prefs.getBoolean("isFirstLaunch", true);

        if (isFirstLaunch) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("isFirstLaunch", false);
            editor.apply();
        }

        return isFirstLaunch;
    }
}
