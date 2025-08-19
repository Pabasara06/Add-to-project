package com.s23010409.parknow;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem; // Import MenuItem for drawer
import android.view.View; // Import View for nav header
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView; // Import TextView for nav header
import android.widget.Toast;

import androidx.annotation.NonNull; // Import for @NonNull
import androidx.appcompat.app.ActionBarDrawerToggle; // Import for drawer toggle
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout; // Import for DrawerLayout

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView; // Import for NavigationView

public class FeedbackActivity extends AppCompatActivity {

    private EditText editSubject, editMessage;
    private RatingBar ratingBar;
    private Button submitButton;
    private DatabaseHelper db;
    private String currentUserEmail;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MaterialToolbar topAppBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback); // Ensure this layout uses DrawerLayout

        db = new DatabaseHelper(this);

        // Get current user email from intent
        currentUserEmail = getIntent().getStringExtra("USER_EMAIL");
        if (currentUserEmail == null || currentUserEmail.isEmpty()) {
            Toast.makeText(this, "User email not found. Please login again.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Initialize UI elements for drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        topAppBar = findViewById(R.id.topAppBar);

        // Setup toolbar
        setSupportActionBar(topAppBar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Submit Feedback");
        }
        // Set navigation icon to open drawer instead of back button
        // topAppBar.setNavigationOnClickListener(v -> onBackPressed()); // Removed, now handled by toggle

        // Setup Navigation Drawer Toggle
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, topAppBar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Set up Navigation Drawer item click listener
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                Intent intent = null;

                if (id == R.id.nav_accelerometer) {
                    intent = new Intent(FeedbackActivity.this, AccelerometerTestActivity.class);
                } else if (id == R.id.nav_about) {
                    intent = new Intent(FeedbackActivity.this, AboutUsActivity.class);
                } else if (id == R.id.nav_settings) {
                    intent = new Intent(FeedbackActivity.this, SettingsActivity.class);
                }

                if (intent != null) {
                    intent.putExtra("USER_EMAIL", currentUserEmail);
                    startActivity(intent);
                    drawerLayout.closeDrawers();
                    return true;
                }
                return false;
            }
        });

        // Update Nav Header with user email
        View headerView = navigationView.getHeaderView(0);
        TextView navHeaderEmail = headerView.findViewById(R.id.textViewUserEmail);
        if (navHeaderEmail != null) {
            navHeaderEmail.setText(currentUserEmail);
        }

        // Initialize UI elements for activity content
        editSubject = findViewById(R.id.editSubject);
        editMessage = findViewById(R.id.editMessage);
        ratingBar = findViewById(R.id.ratingBarFeedback); // Corrected ID here
        submitButton = findViewById(R.id.buttonSubmitFeedback);

        // Set up submit button click listener
        submitButton.setOnClickListener(v -> submitFeedback());

        setupBottomNavigation();
    }

    private void submitFeedback() {
        String subject = editSubject.getText().toString().trim();
        String message = editMessage.getText().toString().trim();
        float rating = ratingBar.getRating();

        if (TextUtils.isEmpty(subject)) {
            editSubject.setError("Subject cannot be empty");
            return;
        }
        if (TextUtils.isEmpty(message)) {
            editMessage.setError("Message cannot be empty");
            return;
        }
        if (rating == 0) {
            Toast.makeText(this, "Please provide a rating.", Toast.LENGTH_SHORT).show();
            return;
        }

        int userId = db.getUserIdByEmail(currentUserEmail);
        if (userId == -1) {
            Toast.makeText(this, "User not found. Cannot submit feedback.", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isInserted = db.insertFeedback(userId, subject, message, rating);

        if (isInserted) {
            Toast.makeText(this, "Feedback submitted successfully!", Toast.LENGTH_SHORT).show();
            // Clear fields after successful submission
            editSubject.setText("");
            editMessage.setText("");
            ratingBar.setRating(0);
            // Optionally navigate back or to a confirmation page
            // finish();
        } else {
            Toast.makeText(this, "Failed to submit feedback. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        if (bottomNav == null) return;

        bottomNav.setSelectedItemId(R.id.action_feedback); // Highlight Feedback

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Intent intent = null;

            if (id == R.id.action_home) {
                intent = new Intent(this, HomeActivity.class);
            } else if (id == R.id.action_profile) {
                intent = new Intent(this, UserProfileActivity.class);
            } else if (id == R.id.action_reservations) {
                // Correctly navigate to MyReservationsActivity
                intent = new Intent(this, MyReservationsActivity.class);
            } else if (id == R.id.action_favorites) {
                intent = new Intent(this, FavoriteSpotsActivity.class);
            } else if (id == R.id.action_feedback) {
                // Already on Feedback, can stay or refresh
                return true;
            }
            // Removed other commented-out items from the navigation logic as they are not in bottom_navigation_menu
            // and are now handled by the drawer.

            if (intent != null) {
                intent.putExtra("USER_EMAIL", currentUserEmail);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(navigationView)) {
            drawerLayout.closeDrawers();
        } else {
            super.onBackPressed();
        }
    }
}
