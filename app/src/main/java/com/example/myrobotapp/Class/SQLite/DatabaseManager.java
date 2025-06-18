package com.example.myrobotapp.Class.SQLite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class DatabaseManager {
    private static DatabaseManager instance;
    private final DatabaseHelper dbHelper;

    private DatabaseManager(Context context) {
        // Use the application context to avoid memory leaks
        dbHelper = new DatabaseHelper(context.getApplicationContext());
        initializeDatabase();
    }

    // Singleton instance getter
    public static synchronized DatabaseManager getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseManager(context);
        }
        return instance;
    }

    // Access the DatabaseHelper instance
    public DatabaseHelper getHelper() {
        return dbHelper;
    }

    private void initializeDatabase() {
        // Trigger database creation and `onCreate()` if needed
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.close(); // Close immediately if no further operations are needed
    }
}
