package com.s23010409.parknow;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem; // Import MenuItem for drawer
import android.view.View; // Import View for nav header
import android.widget.TextView; // Import TextView for nav header
import android.widget.Toast;

import androidx.annotation.NonNull; // Import for @NonNull
import androidx.appcompat.app.ActionBarDrawerToggle; // Import for drawer toggle
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout; // Import for DrawerLayout

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView; // Import for NavigationView

public class AboutUsActivity extends AppCompatActivity {

    private String currentUserEmail;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MaterialToolbar topAppBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us); // Ensure this layout uses DrawerLayout

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
            getSupportActionBar().setTitle("About ParkNow");
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
                    intent = new Intent(AboutUsActivity.this, AccelerometerTestActivity.class);
                } else if (id == R.id.nav_about) {
                    // Already on About Us, can stay or refresh
                    drawerLayout.closeDrawers();
                    return true;
                } else if (id == R.id.nav_settings) {
                    intent = new Intent(AboutUsActivity.this, SettingsActivity.class);
                }

                if (intent != null) {
                    intent.putExtra("USER_EMAIL", currentUserEmail);
                    startActivity(intent);
                    drawerLayout.closeDrawers(); // Close the drawer after selection
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

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        if (bottomNav == null) return;

        // 'About' is now in the drawer, so set a different default selected item for bottom nav
        // For AboutUsActivity, if it's not in bottom nav, it shouldn't be selected.
        // If you want a default, choose one that is actually in the bottom nav, e.g., Home.
        bottomNav.setSelectedItemId(R.id.action_home); // Default to Home

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
            } else if (id == R.id.action_feedback) { // Feedback is now in the bottom nav
                intent = new Intent(this, FeedbackActivity.class);
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