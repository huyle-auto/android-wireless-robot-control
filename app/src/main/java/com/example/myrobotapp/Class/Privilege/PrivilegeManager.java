package com.example.myrobotapp.Class.Privilege;

import static com.example.myrobotapp.Class.FileManager.loadJsonFromAsset;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PrivilegeManager {
    private static Map<String, Map<String, List<String>>> privilegeMap;

    /**
     * Loads privileges from a JSON file in the assets folder.
     *
     * @param context the application context to access assets
     */
    public static void loadPrivileges(Context context) {
        if (privilegeMap != null) {
            Log.w("PrivilegeManager", "Privileges are already loaded.");
            return;
        }

        String json = loadJsonFromAsset(context, "RBAC/user_privileges.json");
        if (json != null) {
            try {
                Gson gson = new Gson();
                Type type = new TypeToken<Map<String, Map<String, List<String>>>>() {}.getType();
                privilegeMap = Collections.unmodifiableMap(gson.fromJson(json, type));
                Log.d("PrivilegeManager", "Privileges loaded successfully.");
            } catch (Exception e) {
                Log.e("PrivilegeManager", "Failed to parse privileges JSON.", e);
            }
        } else {
            Log.e("PrivilegeManager", "Privileges JSON file not found or empty.");
        }
    }

    /**
     * Returns the list of un-allowed fields for a given role and fragment.
     *
     * @param role     the user role
     * @param fragment the fragment name
     * @return a list of un-allowed field names
     */
    public static List<String> getUnAllowedFields(String role, String fragment) {
        if (privilegeMap == null) {
            return Collections.emptyList();
        }

        Map<String, List<String>> rolePrivileges = privilegeMap.get(role);
        if (rolePrivileges == null) {
            return Collections.emptyList();
        }

        return rolePrivileges.getOrDefault(fragment, Collections.emptyList());
    }
}
