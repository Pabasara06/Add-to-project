package com.s23010409.parknow;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import java.util.Locale;

public class AccelerometerTestActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private TextView xValue, yValue, zValue;
    private TextView textAccelerometerStatus; // TextView to show status
    private View gravityIndicator; // The circular view to move

    private String currentUserEmail;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MaterialToolbar topAppBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accelerometer_test);

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

        // Set up the top app bar as the ActionBar
        setSupportActionBar(topAppBar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Accelerometer Test"); // Set the title
            // Note: DisplayHomeAsUpEnabled and setNavigationOnClickListener are implicitly handled by ActionBarDrawerToggle
        }

        // Setup Navigation Drawer Toggle
        // ActionBarDrawerToggle links the hamburger icon with the drawer's open/close state
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, topAppBar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState(); // Synchronizes the indicator with the drawer state

        // Set up Navigation Drawer item click listener
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                Intent intent = null;

                if (id == R.id.nav_accelerometer) {
                    // Already on Accelerometer Test, can stay or refresh
                    drawerLayout.closeDrawers();
                    return true;
                } else if (id == R.id.nav_about) {
                    intent = new Intent(AccelerometerTestActivity.this, AboutUsActivity.class);
                } else if (id == R.id.nav_settings) {
                    intent = new Intent(AccelerometerTestActivity.this, SettingsActivity.class);
                }

                if (intent != null) {
                    intent.putExtra("USER_EMAIL", currentUserEmail);
                    startActivity(intent);
                    overridePendingTransition(0, 0); // Disable activity transition animation for smoother drawer exit
                    finish(); // Finish current activity to prevent deep back stack when navigating
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

        // Initialize TextViews and indicator for sensor values and status
        xValue = findViewById(R.id.textAccelerometerX);
        yValue = findViewById(R.id.textAccelerometerY);
        zValue = findViewById(R.id.textAccelerometerZ);
        textAccelerometerStatus = findViewById(R.id.textAccelerometerStatus); // Initialize status TextView
        gravityIndicator = findViewById(R.id.gravityIndicator); // Initialize the moving indicator

        // Get SensorManager instance
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // Check if accelerometer exists on the device
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (accelerometer == null) {
                // Device does not have an accelerometer
                textAccelerometerStatus.setText("Accelerometer Status: Not available");
                Toast.makeText(this, "Accelerometer not available on this device.", Toast.LENGTH_LONG).show();
                xValue.setText("X: N/A");
                yValue.setText("Y: N/A");
                zValue.setText("Z: N/A");
            } else {
                textAccelerometerStatus.setText("Accelerometer Status: Ready");
            }
        } else {
            textAccelerometerStatus.setText("Accelerometer Status: Service not available");
            Toast.makeText(this, "Sensor service not available.", Toast.LENGTH_LONG).show();
            xValue.setText("X: N/A");
            yValue.setText("Y: N/A");
            zValue.setText("Z: N/A");
        }

        setupBottomNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register the sensor listener when the activity is active
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the sensor listener to save battery when the activity is paused
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Check if the event is from the accelerometer
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0]; // Acceleration force along the x-axis
            float y = event.values[1]; // Acceleration force along the y-axis
            float z = event.values[2]; // Acceleration force along the z-axis

            // Update TextViews with current sensor values
            xValue.setText(String.format(Locale.getDefault(), "X: %.2f m/s²", x));
            yValue.setText(String.format(Locale.getDefault(), "Y: %.2f m/s²", y));
            zValue.setText(String.format(Locale.getDefault(), "Z: %.2f m/s²", z));

            // Move the gravity indicator based on X and Y values
            // Normalize values by SensorManager.GRAVITY_EARTH (approx. 9.81 m/s²)
            // Scale movement to a visible range on the screen
            float maxMovementPx = 100 * getResources().getDisplayMetrics().density; // 100dp converted to pixels
            float normalizedX = x / SensorManager.GRAVITY_EARTH;
            float normalizedY = y / SensorManager.GRAVITY_EARTH;

            // Apply translation, inverting Y to match typical screen coordinates (up is negative Y)
            gravityIndicator.setTranslationX(normalizedX * maxMovementPx);
            gravityIndicator.setTranslationY(-normalizedY * maxMovementPx);

            // Update status based on movement (simple example)
            double magnitude = Math.sqrt(x * x + y * y + z * z);
            // Thresholds can be adjusted based on desired sensitivity
            if (magnitude > (SensorManager.GRAVITY_EARTH + 3)) { // If acceleration is significantly above gravity (e.g., shaken)
                textAccelerometerStatus.setText("Device is shaking!");
            } else if (magnitude < (SensorManager.GRAVITY_EARTH - 3)) { // If acceleration is significantly below gravity (e.g., falling)
                textAccelerometerStatus.setText("Device is falling!");
            } else if (Math.abs(x) < 0.5 && Math.abs(y) < 0.5 && Math.abs(z - SensorManager.GRAVITY_EARTH) < 0.5) {
                // Check if device is relatively stable
                textAccelerometerStatus.setText("Device is stable.");
            } else {
                textAccelerometerStatus.setText("Device is moving.");
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not typically used for basic accelerometer readings
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        if (bottomNav == null) return;

        // Accelerometer is in the drawer, so Home is the default selected item for bottom nav
        bottomNav.setSelectedItemId(R.id.action_home);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Intent intent = null;

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
                intent.putExtra("USER_EMAIL", currentUserEmail);
                startActivity(intent);
                overridePendingTransition(0, 0); // Disable activity transition animation
                finish(); // Finish current activity to prevent deep back stack
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
