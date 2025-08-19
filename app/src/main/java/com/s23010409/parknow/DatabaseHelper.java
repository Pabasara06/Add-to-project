package com.s23010409.parknow;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log; // Added for logging

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper"; // Tag for Logcat
    private static final String DATABASE_NAME = "parkNow.db";
    // Increment database version for schema changes
    // Current version 4 includes Users (with ProfileImage), Reservations, Favorites, Feedback tables
    private static final int DATABASE_VERSION = 4; // Incremented database version

    // Table names
    public static final String TABLE_USERS = "Users";
    public static final String TABLE_RESERVATIONS = "Reservations";
    public static final String TABLE_FAVORITES = "Favorites";
    public static final String TABLE_FEEDBACK = "Feedback";

    // User table columns
    public static final String COL_USER_ID = "UserID";
    public static final String COL_NAME = "Name";
    public static final String COL_EMAIL = "Email";
    public static final String COL_PASSWORD = "Password";
    public static final String COL_PROFILE_IMAGE = "ProfileImage"; // New column for profile image URI

    // Reservation table columns
    public static final String COL_RES_ID = "ReservationID";
    public static final String COL_SPOT_ID = "SpotID"; // Reused for Favorites table as well
    public static final String COL_TIMESTAMP = "TimeStamp"; // Reused for Feedback table as well

    // Favorite table columns (reusing COL_USER_ID and COL_SPOT_ID)
    public static final String COL_FAV_ID = "FavoriteID";

    // Feedback table columns (reusing COL_USER_ID and COL_TIMESTAMP for rating)
    public static final String COL_FEEDBACK_ID = "FeedbackID";
    public static final String COL_SUBJECT = "Subject";
    public static final String COL_MESSAGE = "Message";
    public static final String COL_RATING = "Rating"; // Changed from COL_FEEDBACK_TIMESTAMP for clarity

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // Enable foreign key constraints for referential integrity
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate: Creating database tables.");

        // Create Users table
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_NAME + " TEXT,"
                + COL_EMAIL + " TEXT UNIQUE," // Email must be unique
                + COL_PASSWORD + " TEXT,"
                + COL_PROFILE_IMAGE + " TEXT" + ")"; // Added new column
        db.execSQL(CREATE_USERS_TABLE);
        Log.d(TAG, "Table " + TABLE_USERS + " created.");

        // Create Reservations table
        String CREATE_RESERVATIONS_TABLE = "CREATE TABLE " + TABLE_RESERVATIONS + "("
                + COL_RES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_USER_ID + " INTEGER,"
                + COL_SPOT_ID + " TEXT,"
                + COL_TIMESTAMP + " TEXT,"
                + "FOREIGN KEY(" + COL_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + ") ON DELETE CASCADE" + ")";
        db.execSQL(CREATE_RESERVATIONS_TABLE);
        Log.d(TAG, "Table " + TABLE_RESERVATIONS + " created.");


        // Create Favorites table
        String CREATE_FAVORITES_TABLE = "CREATE TABLE " + TABLE_FAVORITES + "("
                + COL_FAV_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_USER_ID + " INTEGER,"
                + COL_SPOT_ID + " TEXT," // SpotID is the name/identifier of the parking spot
                + "UNIQUE(" + COL_USER_ID + ", " + COL_SPOT_ID + ")," // Prevent duplicate favorite entries for same user/spot
                + "FOREIGN KEY(" + COL_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + ") ON DELETE CASCADE" + ")";
        db.execSQL(CREATE_FAVORITES_TABLE);
        Log.d(TAG, "Table " + TABLE_FAVORITES + " created.");

        // Create Feedback table
        String CREATE_FEEDBACK_TABLE = "CREATE TABLE " + TABLE_FEEDBACK + "("
                + COL_FEEDBACK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COL_USER_ID + " INTEGER,"
                + COL_SUBJECT + " TEXT,"
                + COL_MESSAGE + " TEXT,"
                + COL_RATING + " REAL," // Store rating as REAL (float/double)
                + COL_TIMESTAMP + " TEXT," // Timestamp for when feedback was given
                + "FOREIGN KEY(" + COL_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + ") ON DELETE CASCADE" + ")";
        db.execSQL(CREATE_FEEDBACK_TABLE);
        Log.d(TAG, "Table " + TABLE_FEEDBACK + " created.");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade: Upgrading database from version " + oldVersion + " to " + newVersion);

        // Handle database schema upgrades
        if (oldVersion < 2) {
            // No changes for version 1 to 2 needed based on previous history (was likely just fixing existing schema)
            // If you had changes between 1 and 2, they'd go here.
        }
        if (oldVersion < 3) {
            // Add Favorites and Feedback tables if they don't exist
            String CREATE_FAVORITES_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_FAVORITES + "("
                    + COL_FAV_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COL_USER_ID + " INTEGER,"
                    + COL_SPOT_ID + " TEXT,"
                    + "UNIQUE(" + COL_USER_ID + ", " + COL_SPOT_ID + "),"
                    + "FOREIGN KEY(" + COL_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + ") ON DELETE CASCADE" + ")";
            db.execSQL(CREATE_FAVORITES_TABLE);
            Log.d(TAG, "Table " + TABLE_FAVORITES + " created during upgrade to V3.");

            String CREATE_FEEDBACK_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_FEEDBACK + "("
                    + COL_FEEDBACK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COL_USER_ID + " INTEGER,"
                    + COL_SUBJECT + " TEXT,"
                    + COL_MESSAGE + " TEXT,"
                    + COL_RATING + " REAL,"
                    + COL_TIMESTAMP + " TEXT,"
                    + "FOREIGN KEY(" + COL_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COL_USER_ID + ") ON DELETE CASCADE" + ")";
            db.execSQL(CREATE_FEEDBACK_TABLE);
            Log.d(TAG, "Table " + TABLE_FEEDBACK + " created during upgrade to V3.");
        }
        if (oldVersion < 4) {
            // Add COL_PROFILE_IMAGE to Users table
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COL_PROFILE_IMAGE + " TEXT");
            Log.d(TAG, "Column " + COL_PROFILE_IMAGE + " added to " + TABLE_USERS + " during upgrade to V4.");
        }
        // Add more 'if (oldVersion < X)' blocks for future migrations
    }

    /**
     * Inserts a new user into the Users table.
     *
     * @param name     User's name.
     * @param email    User's email (must be unique).
     * @param password User's password.
     * @return true if insertion is successful, false otherwise.
     */
    public boolean insertUser(String name, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_EMAIL, email);
        values.put(COL_PASSWORD, password);
        values.put(COL_PROFILE_IMAGE, (String) null); // Initialize profile image as null
        long result = db.insert(TABLE_USERS, null, values);
        if (result == -1) {
            Log.e(TAG, "Failed to insert user: " + email);
        } else {
            Log.d(TAG, "User inserted: " + email + ", Row ID: " + result);
        }
        return result != -1;
    }

    /**
     * Checks if a user exists with the given email and password.
     *
     * @param email    User's email.
     * @param password User's password.
     * @return true if a matching user is found, false otherwise.
     */
    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COL_USER_ID};
        String selection = COL_EMAIL + " = ?" + " AND " + COL_PASSWORD + " = ?";
        String[] selectionArgs = {email, password};
        Cursor cursor = null;
        boolean userExists = false;
        try {
            cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
            userExists = cursor != null && cursor.getCount() > 0;
            Log.d(TAG, "Checking user: " + email + ", Exists: " + userExists);
        } catch (Exception e) {
            Log.e(TAG, "Error checking user: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return userExists;
    }

    /**
     * Retrieves user data by email.
     *
     * @param email User's email.
     * @return A Cursor containing user data, or null if no user found or an error occurs.
     */
    public Cursor getUserByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(
                    "SELECT * FROM " + TABLE_USERS + " WHERE " + COL_EMAIL + " = ?",
                    new String[]{email});
            if (cursor != null && cursor.getCount() > 0) {
                Log.d(TAG, "User data retrieved for: " + email);
            } else {
                Log.d(TAG, "No user data found for: " + email);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting user by email: " + e.getMessage());
        }
        return cursor; // Caller is responsible for closing the cursor
    }

    /**
     * Retrieves the UserID for a given email.
     *
     * @param email User's email.
     * @return The UserID if found, -1 otherwise.
     */
    public int getUserIdByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        int userId = -1;
        try {
            cursor = db.rawQuery("SELECT " + COL_USER_ID + " FROM " + TABLE_USERS + " WHERE " + COL_EMAIL + " = ?", new String[]{email});
            if (cursor != null && cursor.moveToFirst()) {
                userId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_ID));
                Log.d(TAG, "Retrieved UserID " + userId + " for email: " + email);
            } else {
                Log.d(TAG, "UserID not found for email: " + email);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting user ID by email: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return userId;
    }

    /**
     * Updates the name of an existing user.
     *
     * @param email   User's email to identify the record.
     * @param newName The new name for the user.
     * @return true if update is successful, false otherwise.
     */
    public boolean updateUser(String email, String newName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAME, newName);
        int rowsAffected = db.update(TABLE_USERS, values, COL_EMAIL + " = ?", new String[]{email});
        if (rowsAffected > 0) {
            Log.d(TAG, "User " + email + " name updated to: " + newName);
        } else {
            Log.e(TAG, "Failed to update user " + email + " name.");
        }
        return rowsAffected > 0;
    }

    /**
     * Updates the profile image URI for a user.
     *
     * @param userId         The ID of the user.
     * @param imageUriString The URI string of the new profile image.
     * @return true if update is successful, false otherwise.
     */
    public boolean updateProfileImage(int userId, String imageUriString) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PROFILE_IMAGE, imageUriString);
        int rowsAffected = db.update(TABLE_USERS, values, COL_USER_ID + " = ?", new String[]{String.valueOf(userId)});
        if (rowsAffected > 0) {
            Log.d(TAG, "User " + userId + " profile image updated to: " + imageUriString);
        } else {
            Log.e(TAG, "Failed to update user " + userId + " profile image.");
        }
        return rowsAffected > 0;
    }


    /**
     * Inserts a new parking reservation into the Reservations table.
     *
     * @param userId    The ID of the user making the reservation.
     * @param spotId    The ID/name of the parking spot.
     * @param timestamp The timestamp of the reservation.
     * @return true if insertion is successful, false otherwise.
     */
    public boolean insertReservation(int userId, String spotId, String timestamp) {
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COL_USER_ID, userId);
            values.put(COL_SPOT_ID, spotId);
            values.put(COL_TIMESTAMP, timestamp);

            long result = db.insert(TABLE_RESERVATIONS, null, values);
            if (result == -1) {
                Log.e(TAG, "Failed to insert reservation for user " + userId + " spot " + spotId);
            } else {
                Log.d(TAG, "Reservation inserted: UserID=" + userId + ", SpotID=" + spotId + ", Timestamp=" + timestamp);
            }
            return result != -1;
        } catch (Exception e) {
            Log.e(TAG, "Error inserting reservation: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves all reservations from the Reservations table.
     *
     * @return A Cursor containing all reservation data.
     */
    public Cursor getAllReservations() {
        Log.d(TAG, "Retrieving all reservations.");
        return getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_RESERVATIONS, null);
    }

    /**
     * Retrieves all reservations made by a specific user.
     *
     * @param userId The ID of the user.
     * @return A Cursor containing reservations for the specified user, ordered by timestamp descending.
     */
    public Cursor getReservationsByUser(int userId) {
        Log.d(TAG, "Retrieving reservations for UserID: " + userId);
        return getReadableDatabase().rawQuery(
                "SELECT * FROM " + TABLE_RESERVATIONS + " WHERE " + COL_USER_ID + "=? ORDER BY " + COL_TIMESTAMP + " DESC",
                new String[]{String.valueOf(userId)});
    }

    /**
     * Inserts a parking spot into the Favorites table for a specific user.
     *
     * @param userId The ID of the user.
     * @param spotId The ID/name of the parking spot to favorite.
     * @return true if insertion is successful, false otherwise.
     */
    public boolean insertFavoriteSpot(int userId, String spotId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USER_ID, userId);
        values.put(COL_SPOT_ID, spotId);
        long result = db.insert(TABLE_FAVORITES, null, values);
        if (result == -1) {
            Log.e(TAG, "Failed to insert favorite spot: UserID=" + userId + ", SpotID=" + spotId);
        } else {
            Log.d(TAG, "Favorite spot inserted: UserID=" + userId + ", SpotID=" + spotId);
        }
        return result != -1;
    }

    /**
     * Deletes a parking spot from the Favorites table for a specific user.
     *
     * @param userId The ID of the user.
     * @param spotId The ID/name of the parking spot to unfavorite.
     * @return true if deletion is successful, false otherwise.
     */
    public boolean deleteFavoriteSpot(int userId, String spotId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_FAVORITES, COL_USER_ID + " = ? AND " + COL_SPOT_ID + " = ?",
                new String[]{String.valueOf(userId), spotId});
        if (rowsAffected > 0) {
            Log.d(TAG, "Favorite spot deleted: UserID=" + userId + ", SpotID=" + spotId);
        } else {
            Log.e(TAG, "Failed to delete favorite spot: UserID=" + userId + ", SpotID=" + spotId);
        }
        return rowsAffected > 0;
    }

    /**
     * Retrieves all favorite spots for a specific user.
     *
     * @param userId The ID of the user.
     * @return A Cursor containing favorite spots for the specified user.
     */
    public Cursor getFavoriteSpotsByUser(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Log.d(TAG, "Retrieving favorite spots for UserID: " + userId);
        return db.rawQuery(
                "SELECT * FROM " + TABLE_FAVORITES + " WHERE " + COL_USER_ID + "=?",
                new String[]{String.valueOf(userId)});
    }

    /**
     * Checks if a specific parking spot is favorited by a user.
     *
     * @param userId The ID of the user.
     * @param spotId The ID/name of the parking spot.
     * @return true if the spot is favorited by the user, false otherwise.
     */
    public boolean isSpotFavorite(int userId, String spotId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        boolean isFavorite = false;
        try {
            String selection = COL_USER_ID + " = ? AND " + COL_SPOT_ID + " = ?";
            String[] selectionArgs = {String.valueOf(userId), spotId};
            cursor = db.query(TABLE_FAVORITES, null, selection, selectionArgs, null, null, null);
            isFavorite = cursor != null && cursor.getCount() > 0;
            Log.d(TAG, "Is spot " + spotId + " favorite for user " + userId + "? " + isFavorite);
        } catch (Exception e) {
            Log.e(TAG, "Error checking if spot is favorite: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return isFavorite;
    }

    /**
     * Inserts new feedback into the Feedback table.
     *
     * @param userId  The ID of the user submitting feedback.
     * @param subject The subject of the feedback.
     * @param message The detailed feedback message.
     * @param rating  The rating given by the user (e.g., 1.0 to 5.0).
     * @return true if insertion is successful, false otherwise.
     */
    public boolean insertFeedback(int userId, String subject, String message, float rating) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USER_ID, userId);
        values.put(COL_SUBJECT, subject);
        values.put(COL_MESSAGE, message);
        values.put(COL_RATING, rating); // Store rating as REAL
        values.put(COL_TIMESTAMP, String.valueOf(System.currentTimeMillis())); // Store current timestamp
        long result = db.insert(TABLE_FEEDBACK, null, values);
        if (result == -1) {
            Log.e(TAG, "Failed to insert feedback for user " + userId);
        } else {
            Log.d(TAG, "Feedback inserted: UserID=" + userId + ", Subject=" + subject + ", Rating=" + rating);
        }
        return result != -1;
    }

    /**
     * Retrieves all feedback entries from the Feedback table.
     *
     * @return A Cursor containing all feedback data.
     */
    public Cursor getAllFeedback() {
        Log.d(TAG, "Retrieving all feedback.");
        return getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_FEEDBACK, null);
    }
}

