package com.s23010409.parknow;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReserveSpotActivity extends AppCompatActivity {

    private Button confirmButton;
    private DatabaseHelper db;
    private int userId = -1;
    private String spotId;
    private String timestamp;
    private String userEmail; // This is the correct variable name
    private double pricePerHour;

    private EditText editDuration;
    private TextView textPricePerHour, textTotalCost;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MaterialToolbar topAppBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reserve_spot);

        db = new DatabaseHelper(this);
        confirmButton = findViewById(R.id.buttonConfirm);
        editDuration = findViewById(R.id.editDuration);
        textPricePerHour = findViewById(R.id.textPricePerHour);
        textTotalCost = findViewById(R.id.textTotalCost);

        // Get data from intent
        userEmail = getIntent().getStringExtra("USER_EMAIL"); // Correctly initialized here
        spotId = getIntent().getStringExtra("spot_name");
        timestamp = getIntent().getStringExtra("TIMESTAMP");
        pricePerHour = getIntent().getDoubleExtra("price_per_hour", 0.0);

        if (userEmail == null || userEmail.isEmpty() || spotId == null || timestamp == null) {
            Toast.makeText(this, "Reservation data incomplete", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Get userId from email
        userId = db.getUserIdByEmail(userEmail);
        if (userId == -1) {
            Toast.makeText(this, "Error: User ID not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Display parking spot ID and reservation timestamp
        TextView textParkingSpotId = findViewById(R.id.textParkingSpotId);
        TextView textReservationTimestamp = findViewById(R.id.textReservationTimestamp);
        textParkingSpotId.setText("Spot: " + spotId);
        textReservationTimestamp.setText("Time: " + timestamp);


        // Display price per hour
        textPricePerHour.setText(String.format(Locale.getDefault(), "Price Per Hour: LKR %.2f", pricePerHour));

        // Calculate and display total cost based on duration input
        editDuration.setText("1"); // Default to 1 hour
        calculateAndDisplayTotalCost(); // Calculate initial cost

        // Add TextWatcher to dynamically update total cost when duration changes
        editDuration.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calculateAndDisplayTotalCost(); // Recalculate and display on text change
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Setup TopAppBar Toolbar for ReserveSpotActivity
        topAppBar = findViewById(R.id.topAppBar);
        setSupportActionBar(topAppBar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Enable back button
            getSupportActionBar().setTitle("Confirm Reservation");
        }
        topAppBar.setNavigationOnClickListener(v -> onBackPressed()); // Handle back button click

        // Initialize UI elements for drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

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

                // Handle navigation based on drawer item ID
                if (id == R.id.nav_accelerometer) {
                    intent = new Intent(ReserveSpotActivity.this, AccelerometerTestActivity.class);
                } else if (id == R.id.nav_about) {
                    intent = new Intent(ReserveSpotActivity.this, AboutUsActivity.class);
                } else if (id == R.id.nav_settings) {
                    intent = new Intent(ReserveSpotActivity.this, SettingsActivity.class);
                }

                if (intent != null) {
                    intent.putExtra("USER_EMAIL", userEmail); // Pass user email to next activity
                    startActivity(intent);
                    drawerLayout.closeDrawers(); // Close the drawer after selection
                    return true;
                }
                return false;
            }
        });

        // Update Nav Header with user email
        View headerView = navigationView.getHeaderView(0); // Get the header view of the drawer
        TextView navHeaderEmail = headerView.findViewById(R.id.textViewUserEmail); // Find TextView for email
        if (navHeaderEmail != null) {
            navHeaderEmail.setText(userEmail); // Set the user's email in the header
        }

        // Confirm Button click listener: proceed to payment
        confirmButton.setOnClickListener(v -> {
            int durationHours;
            try {
                durationHours = Integer.parseInt(editDuration.getText().toString());
                if (durationHours <= 0) {
                    Toast.makeText(this, "Duration must be at least 1 hour", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter a valid number of hours", Toast.LENGTH_SHORT).show();
                return;
            }

            // Before making reservation, navigate to payment activity
            Intent paymentIntent = new Intent(ReserveSpotActivity.this, PaymentActivity.class);
            paymentIntent.putExtra("USER_EMAIL", userEmail); // Pass user email
            paymentIntent.putExtra("spot_name", spotId);
            paymentIntent.putExtra("TIMESTAMP", timestamp);
            paymentIntent.putExtra("DURATION_HOURS", durationHours); // Pass duration
            paymentIntent.putExtra("TOTAL_COST", pricePerHour * durationHours); // Pass calculated total cost
            startActivity(paymentIntent);

            // No need to finish() here, as PaymentActivity might return a result and you might want to come back
        });

        // Setup Bottom Navigation
        setupBottomNavigation();
    }

    // Helper method to calculate and display the total cost
    private void calculateAndDisplayTotalCost() {
        String durationStr = editDuration.getText().toString();
        double duration = 0.0;
        try {
            if (!durationStr.isEmpty()) {
                duration = Double.parseDouble(durationStr);
            }
        } catch (NumberFormatException e) {
            // If input is invalid during typing, default duration to 0 for calculation
            duration = 0.0;
        }

        double totalCost = pricePerHour * duration;
        textTotalCost.setText(String.format(Locale.getDefault(), "Total Cost: LKR %.2f", totalCost));
    }


    // Sets up the bottom navigation bar and its item click listener
    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        if (bottomNav == null) return; // Exit if bottom navigation is not in layout

        // ReserveSpotActivity is not a primary bottom navigation screen, so set a different default
        bottomNav.setSelectedItemId(R.id.action_home); // Default to Home for visual consistency

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Intent intent = null;

            // Handle navigation for bottom navigation items
            if (id == R.id.action_home) {
                intent = new Intent(this, HomeActivity.class);
            } else if (id == R.id.action_profile) {
                intent = new Intent(this, UserProfileActivity.class);
            } else if (id == R.id.action_reservations) {
                intent = new Intent(this, MyReservationsActivity.class);
            } else if (id == R.id.action_favorites) {
                intent = new Intent(this, FavoriteSpotsActivity.class);
            } else if (id == R.id.action_feedback) {
                intent = new Intent(this, FeedbackActivity.class);
            }

            if (intent != null) {
                intent.putExtra("USER_EMAIL", userEmail); // Pass user email for continuity
                startActivity(intent);
                overridePendingTransition(0, 0); // Disable activity transition animation
                finish(); // Finish current activity to prevent deep back stack
                return true;
            }
            return false; // No navigation handled for the selected item
        });
    }

    // Handle options menu item clicks (e.g., back arrow in toolbar)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle the Up button (back arrow)
        if (item.getItemId() == android.R.id.home) {
            onBackPressed(); // Call onBackPressed when Up button is pressed
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Override onBackPressed to close the drawer first if it's open, otherwise perform default back action
    @Override
    public void onBackPressed() {
        if (drawerLayout != null && navigationView != null && drawerLayout.isDrawerOpen(navigationView)) {
            drawerLayout.closeDrawers(); // Close the navigation drawer
        } else {
            super.onBackPressed(); // Perform default back button action (finish activity)
        }
    }
}
