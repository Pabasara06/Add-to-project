package com.s23010409.parknow;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler; // Import Handler for delayed actions

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    // Duration of the splash screen in milliseconds
    private static final long SPLASH_DISPLAY_LENGTH = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Set the layout for the splash screen

        // Using a Handler to delay the start of the LoginActivity
        new Handler().postDelayed(() -> {
            // Create an Intent to start the LoginActivity
            Intent mainIntent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(mainIntent); // Start the LoginActivity

            // Finish the MainActivity so the user cannot go back to it using the back button
            finish();
        }, SPLASH_DISPLAY_LENGTH);
    }
}
