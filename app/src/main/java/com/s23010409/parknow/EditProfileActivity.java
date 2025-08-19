package com.s23010409.parknow;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

public class EditProfileActivity extends AppCompatActivity {

    private EditText editName;
    private Button buttonSave;
    private DatabaseHelper db;

    private String currentUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        db = new DatabaseHelper(this);

        // Setup toolbar with back button and title
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Edit Profile");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed()); // Handle back button click

        // Initialize views
        editName = findViewById(R.id.editName);
        buttonSave = findViewById(R.id.buttonSave);

        // Get user email from intent extras
        currentUserEmail = getIntent().getStringExtra("USER_EMAIL");
        if (TextUtils.isEmpty(currentUserEmail)) {
            Toast.makeText(this, "User email not provided.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadUserData(currentUserEmail);

        buttonSave.setOnClickListener(v -> {
            String newName = editName.getText().toString().trim();
            if (TextUtils.isEmpty(newName)) {
                editName.setError("Name cannot be empty");
                return;
            }

            boolean success = db.updateUser(currentUserEmail, newName);
            if (success) {
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();

                // Return updated name to caller
                Intent resultIntent = new Intent();
                resultIntent.putExtra("UPDATED_NAME", newName);
                setResult(RESULT_OK, resultIntent);

                finish();
            } else {
                Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserData(String email) {
        Cursor cursor = null;
        try {
            cursor = db.getUserByEmail(email);
            if (cursor != null && cursor.moveToFirst()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NAME));
                editName.setText(name);
            } else {
                Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error loading user data", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    // No need to override onSupportNavigateUp if using toolbar.setNavigationOnClickListener
    // @Override
    // public boolean onSupportNavigateUp() {
    //     finish();
    //     return true;
    // }
}
