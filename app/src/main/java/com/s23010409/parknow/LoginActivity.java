package com.s23010409.parknow;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log; // Import Log for debugging
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity"; // Tag for Logcat messages
    private EditText emailField, passwordField;
    private Button buttonLogin;
    private TextView textSignup;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Log.d(TAG, "onCreate: LoginActivity started.");

        db = new DatabaseHelper(this);

        emailField = findViewById(R.id.editTextEmail);
        passwordField = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textSignup = findViewById(R.id.textSignup);

        // Setup toolbar (optional for login, but good for consistency)
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Login to ParkNow");
            // No back button on login screen, so no navigation icon needed
        }

        // Login Button Logic
        buttonLogin.setOnClickListener(v -> loginUser());

        // Navigate to SignUp
        textSignup.setOnClickListener(v -> {
            Log.d(TAG, "Navigating to SignupActivity.");
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
            // We don't finish LoginActivity here, so user can press back from Signup to Login
        });
    }

    private void loginUser() {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Login attempt: Empty email or password.");
            return;
        }

        boolean isValid = db.checkUser(email, password);
        Log.d(TAG, "Login attempt for email: " + email + ", isValid: " + isValid);

        if (isValid) {
            Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "Login Successful. Preparing to navigate to HomeActivity...");

            // Introduce a small delay to ensure the Toast is visible.
            // In a production app, this delay might be removed or very minimal.
            new Handler().postDelayed(() -> {
                try {
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    intent.putExtra("USER_EMAIL", email); // Pass the logged-in user's email
                    // Clear the back stack so user cannot go back to LoginActivity from HomeActivity
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent); // Start HomeActivity
                    finish(); // Finish LoginActivity
                    Log.d(TAG, "Successfully started HomeActivity and finished LoginActivity.");
                } catch (Exception e) {
                    Log.e(TAG, "Error starting HomeActivity: " + e.getMessage(), e);
                    Toast.makeText(LoginActivity.this, "Error starting Home screen. Please try again.", Toast.LENGTH_LONG).show();
                }
            }, 500); // 500ms delay

        } else {
            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "Login Failed: Invalid credentials for email: " + email);
        }
    }
}
