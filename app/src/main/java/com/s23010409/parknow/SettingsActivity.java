package com.s23010409.parknow;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch; // Import Switch
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate; // Import AppCompatDelegate
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";
    private static final String PREFS_NAME = "ParkNowPrefs";
    private static final String PREF_DARK_MODE = "darkModeEnabled";

    private String currentUserEmail;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MaterialToolbar topAppBar;
    private Switch switchDarkMode; // Declare the Switch

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: SettingsActivity started.");

        try {
            setContentView(R.layout.activity_settings);
            Log.d(TAG, "onCreate: Layout set successfully.");

            currentUserEmail = getIntent().getStringExtra("USER_EMAIL");
            if (currentUserEmail == null || currentUserEmail.isEmpty()) {
                Toast.makeText(this, "User email not found. Please login again.", Toast.LENGTH_LONG).show();
                Log.e(TAG, "onCreate: User email not found in intent.");
                finish();
                return;
            } else {
                Log.d(TAG, "onCreate: Current user email: " + currentUserEmail);
            }

            // Initialize UI elements for drawer
            drawerLayout = findViewById(R.id.drawer_layout);
            navigationView = findViewById(R.id.nav_view);
            topAppBar = findViewById(R.id.topAppBar);
            switchDarkMode = findViewById(R.id.switchDarkMode); // Initialize the Switch

            if (drawerLayout == null) Log.e(TAG, "findViewById failed for drawer_layout");
            if (navigationView == null) Log.e(TAG, "findViewById failed for nav_view");
            if (topAppBar == null) Log.e(TAG, "findViewById failed for topAppBar");
            if (switchDarkMode == null) Log.e(TAG, "findViewById failed for switchDarkMode");

            setSupportActionBar(topAppBar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Settings");
                Log.d(TAG, "onCreate: Toolbar title set to Settings.");
            }

            // Setup Navigation Drawer Toggle
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawerLayout, topAppBar,
                    R.string.navigation_drawer_open,
                    R.string.navigation_drawer_close
            );
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();
            Log.d(TAG, "onCreate: Navigation Drawer Toggle synced.");

            // Set up Navigation Drawer item click listener
            navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int id = item.getItemId();
                    Intent intent = null;
                    Log.d(TAG, "onNavigationItemSelected: Item selected with ID: " + getResources().getResourceEntryName(id));

                    if (id == R.id.nav_accelerometer) {
                        intent = new Intent(SettingsActivity.this, AccelerometerTestActivity.class);
                    } else if (id == R.id.nav_about) {
                        intent = new Intent(SettingsActivity.this, AboutUsActivity.class);
                    } else if (id == R.id.nav_settings) {
                        drawerLayout.closeDrawers();
                        Log.d(TAG, "onNavigationItemSelected: Already on Settings. Closing drawer.");
                        return true;
                    }

                    if (intent != null) {
                        intent.putExtra("USER_EMAIL", currentUserEmail);
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                        finish();
                        Log.d(TAG, "onNavigationItemSelected: Navigated to " + intent.getComponent().getClassName());
                        return true;
                    }
                    Log.w(TAG, "onNavigationItemSelected: No intent created for selected item.");
                    return false;
                }
            });
            Log.d(TAG, "onCreate: Navigation Drawer listener set.");

            // Update Nav Header with user email
            View headerView = navigationView.getHeaderView(0);
            TextView navHeaderEmail = headerView.findViewById(R.id.textViewUserEmail);
            if (navHeaderEmail != null) {
                navHeaderEmail.setText(currentUserEmail);
                Log.d(TAG, "onCreate: Nav header email set.");
            } else {
                Log.e(TAG, "onCreate: textViewUserEmail not found in nav_header_main.xml");
            }

            // Load saved dark mode preference and set switch state
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            boolean darkModeEnabled = prefs.getBoolean(PREF_DARK_MODE, false); // Default to false (light mode)
            switchDarkMode.setChecked(darkModeEnabled);
            Log.d(TAG, "onCreate: Initial dark mode state loaded: " + darkModeEnabled);

            // Set listener for dark mode switch
            switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(PREF_DARK_MODE, isChecked);
                editor.apply();
                Log.d(TAG, "onCheckedChanged: Dark mode preference saved: " + isChecked);

                if (isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    Log.d(TAG, "onCheckedChanged: Setting theme to MODE_NIGHT_YES");
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    Log.d(TAG, "onCheckedChanged: Setting theme to MODE_NIGHT_NO");
                }
                // Recreate activity to apply theme change immediately
                recreate();
            });
            Log.d(TAG, "onCreate: Dark mode switch listener set.");


            setupBottomNavigation();
            Log.d(TAG, "onCreate: setupBottomNavigation completed.");

        } catch (Exception e) {
            Log.e(TAG, "onCreate: Error during activity creation: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading Settings page: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        if (bottomNav == null) {
            Log.e(TAG, "setupBottomNavigation: bottom_navigation view not found!");
            return;
        }
        Log.d(TAG, "setupBottomNavigation: Bottom navigation view found.");

        bottomNav.setSelectedItemId(R.id.action_home);
        Log.d(TAG, "setupBottomNavigation: Default selected item set to Home.");


        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Intent intent = null;
            Log.d(TAG, "onBottomNavigationItemSelected: Item selected with ID: " + getResources().getResourceEntryName(id));


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
                overridePendingTransition(0, 0);
                finish();
                Log.d(TAG, "onBottomNavigationItemSelected: Navigated to " + intent.getComponent().getClassName());
                return true;
            }
            Log.w(TAG, "onBottomNavigationItemSelected: No intent created for selected item.");
            return false;
        });
        Log.d(TAG, "setupBottomNavigation: Listener set for bottom navigation.");
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && navigationView != null && drawerLayout.isDrawerOpen(navigationView)) {
            drawerLayout.closeDrawers();
            Log.d(TAG, "onBackPressed: Drawer was open, closing it.");
        } else {
            super.onBackPressed();
            Log.d(TAG, "onBackPressed: Drawer was closed, performing super onBackPressed.");
        }
    }
}
