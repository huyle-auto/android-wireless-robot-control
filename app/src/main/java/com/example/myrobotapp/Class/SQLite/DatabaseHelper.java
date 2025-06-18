package com.example.myrobotapp.Class.SQLite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.mindrot.jbcrypt.BCrypt;

public class DatabaseHelper extends SQLiteOpenHelper {

    private Context context;
    public static final String DATABASE_NAME = "RobotAppDatabase.db";
    public static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME = "Users";
    private static final String COLUMN_ID = "Id";
    private static final String COLUMN_USERNAME = "Username";
    private static final String COLUMN_PASSWORD = "Password";
    private static final String COLUMN_ROLE = "Role";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String query =
                        "CREATE TABLE " + TABLE_NAME + "("
                                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                                + COLUMN_USERNAME + " TEXT, "
                                + COLUMN_PASSWORD + " TEXT, "
                                + COLUMN_ROLE + " TEXT"
                                + ")";
        // Create table
        sqLiteDatabase.execSQL(query);

        // Initiate default users
        String[][] initialUsers = {
                {"HuyOperator", "HuyOperator123", "Operator"},
                {"HuyEngineer", "HuyEngineer123", "Engineer"},
                {"HuyAdmin", "HuyAdmin123", "Admin"}
        };

        // Use a transaction for batch insert
        sqLiteDatabase.beginTransaction();
        try {
            for (String[] user : initialUsers) {
                String username = user[0];
                String rawPassword = user[1];
                String role = user[2];

                // Hash the password
                String hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt());

                // Insert data
                ContentValues values = new ContentValues();
                values.put(COLUMN_USERNAME, username);
                values.put(COLUMN_PASSWORD, hashedPassword);
                values.put(COLUMN_ROLE, role);

                sqLiteDatabase.insert(TABLE_NAME, null, values);
            }

            // Mark the transaction as successful
            sqLiteDatabase.setTransactionSuccessful();
        } finally {
            sqLiteDatabase.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public void addUser(String username, String password, String role) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_USERNAME, username);
        cv.put(COLUMN_PASSWORD, password);
        cv.put(COLUMN_ROLE, role);

        long result = db.insert(TABLE_NAME, null, cv);

        if (result == -1) {
            Toast.makeText(context, "Failed to add data", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Succeeded to add data", Toast.LENGTH_SHORT).show();
        }

        db.close();
    }

    public boolean isLoginValid(String usernameInput, String passwordInput) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Query the database for the row with the matching username
        String[] columns = {"Password"}; // We only need the Password column
        String selection = "Username = ?";
        String[] selectionArgs = {usernameInput};

        Cursor cursor = db.query("Users", columns, selection, selectionArgs, null, null, null);

        if (cursor.moveToFirst()) {
            // Username exists; get the hashed password from the database
            String storedHashedPassword = cursor.getString(cursor.getColumnIndexOrThrow("Password"));

            // Verify the user-provided password against the hashed password
            if (BCrypt.checkpw(passwordInput, storedHashedPassword)) {
                cursor.close();
                db.close();
                return true; // Login valid
            }
        }

        // If no match for username or password, return false
        cursor.close();
        db.close();
        return false;
    }

    public String getUserRole(String usernameInput) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {"Role"}; // Only fetch the Role column
        String selection = "Username = ?";
        String[] selectionArgs = {usernameInput};

        Cursor cursor = db.query("Users", columns, selection, selectionArgs, null, null, null);

        String userRole = null; // Default if no role is found
        if (cursor.moveToFirst()) {
            // Fetch the role
            userRole = cursor.getString(cursor.getColumnIndexOrThrow("Role"));
        }

        cursor.close();
        db.close();
        return userRole; // Will return null if the username does not exist
    }
}
