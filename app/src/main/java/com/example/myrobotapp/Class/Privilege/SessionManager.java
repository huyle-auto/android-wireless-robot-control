package com.example.myrobotapp.Class.Privilege;

import java.util.ArrayList;
import java.util.List;

public class SessionManager {
    public static String currentUserRole = "Operator";
    private static List<RoleChangeListener> listeners = new ArrayList<>();

    public static String getUserRole() {
        return currentUserRole;
    }

    public static void setUserRole(String role) {
        currentUserRole = role;
        notifyListeners();
    }

    public static void registerListener(RoleChangeListener listener) {
        listeners.add(listener);
    }

    public static void unregisterListener(RoleChangeListener listener) {
        listeners.remove(listener);
    }

    private static void notifyListeners() {
        for (RoleChangeListener listener : listeners) {
            listener.onRoleChanged(currentUserRole);
        }
    }

    public interface RoleChangeListener {
        void onRoleChanged(String newRole);
    }
}

