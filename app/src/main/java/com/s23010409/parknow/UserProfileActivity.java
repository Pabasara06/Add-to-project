package com.s23010409.parknow;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log; // Import Log for debugging
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

public class UserProfileActivity extends AppCompatActivity {

    private static final String TAG = "UserProfileActivity"; // Tag for Logcat
    private TextView profileName, profileEmail; // Corrected TextView variable names
    private Button buttonEditProfile, buttonLogout, buttonMyReservations;
    private DatabaseHelper db;
    private String currentUserEmail;

    // Drawer and Toolbar elements
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MaterialToolbar topAppBar;

    // Request code for EditProfileActivity
    private static final int EDIT_PROFILE_REQUEST_CODE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: UserProfileActivity started.");

        try {
            setContentView(R.layout.activity_user_profile);
            Log.d(TAG, "onCreate: Layout set successfully.");

            db = new DatabaseHelper(this);

            // Get user email from intent
            currentUserEmail = getIntent().getStringExtra("USER_EMAIL");
            if (currentUserEmail == null || currentUserEmail.isEmpty()) {
                Toast.makeText(this, "User email not found. Please login again.", Toast.LENGTH_LONG).show();
                Log.e(TAG, "onCreate: User email not found in intent.");
                finish();
                return;
            } else {
                Log.d(TAG, "onCreate: Current user email: " + currentUserEmail);
            }

            // Initialize UI elements
            profileName = findViewById(R.id.profileName); // Corrected ID
            profileEmail = findViewById(R.id.profileEmail); // Corrected ID
            buttonEditProfile = findViewById(R.id.buttonEditProfile);
            buttonMyReservations = findViewById(R.id.buttonMyReservations);
            buttonLogout = findViewById(R.id.buttonLogout);

            // Setup TopAppBar Toolbar
            topAppBar = findViewById(R.id.topAppBar);
            setSupportActionBar(topAppBar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("My Profile");
                Log.d(TAG, "onCreate: Toolbar title set.");
            }

            // Initialize UI elements for drawer
            drawerLayout = findViewById(R.id.drawer_layout);
            navigationView = findViewById(R.id.nav_view);

            if (drawerLayout == null) Log.e(TAG, "findViewById failed for drawer_layout");
            if (navigationView == null) Log.e(TAG, "findViewById failed for nav_view");

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
                        intent = new Intent(UserProfileActivity.this, AccelerometerTestActivity.class);
                    } else if (id == R.id.nav_about) {
                        intent = new Intent(UserProfileActivity.this, AboutUsActivity.class);
                    } else if (id == R.id.nav_settings) {
                        intent = new Intent(UserProfileActivity.this, SettingsActivity.class);
                    }

                    if (intent != null) {
                        intent.putExtra("USER_EMAIL", currentUserEmail);
                        startActivity(intent);
                        drawerLayout.closeDrawers();
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

            loadUserData();

            buttonEditProfile.setOnClickListener(v -> {
                Log.d(TAG, "Edit Profile button clicked.");
                Intent intent = new Intent(UserProfileActivity.this, EditProfileActivity.class);
                intent.putExtra("USER_EMAIL", currentUserEmail);
                startActivityForResult(intent, EDIT_PROFILE_REQUEST_CODE);
            });

            buttonMyReservations.setOnClickListener(v -> {
                Log.d(TAG, "My Reservations button clicked. Navigating to MyReservationsActivity.");
                Intent intent = new Intent(UserProfileActivity.this, MyReservationsActivity.class);
                intent.putExtra("USER_EMAIL", currentUserEmail);
                startActivity(intent);
            });


            buttonLogout.setOnClickListener(v -> {
                Log.d(TAG, "Logout button clicked. Logging out user: " + currentUserEmail);
                // Clear user session (if any) and navigate to LoginActivity
                Intent intent = new Intent(UserProfileActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Clear back stack
                startActivity(intent);
                finish(); // Finish current activity
                Toast.makeText(UserProfileActivity.this, "Logged out successfully!", Toast.LENGTH_SHORT).show();
            });

            setupBottomNavigation();
            Log.d(TAG, "onCreate: setupBottomNavigation completed.");

        } catch (Exception e) {
            Log.e(TAG, "onCreate: Error during activity creation: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading user profile: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish(); // Finish to prevent app from hanging
        }
    }

    private void loadUserData() {
        Log.d(TAG, "loadUserData() called for email: " + currentUserEmail);
        Cursor cursor = null;
        try {
            cursor = db.getUserByEmail(currentUserEmail);
            if (cursor != null && cursor.moveToFirst()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NAME));
                String email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_EMAIL));

                profileName.setText(name);
                profileEmail.setText(email);
                Log.d(TAG, "User data loaded: Name=" + name + ", Email=" + email);
            } else {
                Toast.makeText(this, "User data not found!", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "User data not found for email: " + currentUserEmail);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading user data: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading user data.", Toast.LENGTH_SHORT).show();
        } finally {
            if (cursor != null) {
                cursor.close();
                Log.d(TAG, "loadUserData: Cursor closed.");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_PROFILE_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null && data.hasExtra("UPDATED_NAME")) {
                String updatedName = data.getStringExtra("UPDATED_NAME");
                profileName.setText(updatedName); // Update displayed name
                Toast.makeText(this, "Profile name updated to: " + updatedName, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onActivityResult: Profile name updated to: " + updatedName);
            }
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        if (bottomNav == null) {
            Log.e(TAG, "setupBottomNavigation: bottom_navigation view not found!");
            return;
        }
        Log.d(TAG, "setupBottomNavigation: Bottom navigation view found.");

        bottomNav.setSelectedItemId(R.id.action_profile); // Highlight Profile
        Log.d(TAG, "setupBottomNavigation: Default selected item set to Profile.");


        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Intent intent = null;
            Log.d(TAG, "onBottomNavigationItemSelected: Item selected with ID: " + getResources().getResourceEntryName(id));

            if (id == R.id.action_home) {
                intent = new Intent(this, HomeActivity.class);
            } else if (id == R.id.action_profile) {
                // Already on User Profile, can stay or refresh
                Log.d(TAG, "onBottomNavigationItemSelected: Already on User Profile.");
                return true;
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
