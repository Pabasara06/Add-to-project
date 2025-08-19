package com.s23010409.parknow;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log; // Import Log for debugging
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;

public class MyReservationsActivity extends AppCompatActivity {

    private static final String TAG = "MyReservationsActivity"; // Tag for Logcat
    private ListView listView;
    private ArrayList<String> reservationsList;
    private DatabaseHelper db;
    private String currentUserEmail;

    // Drawer and Toolbar elements
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MaterialToolbar topAppBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: MyReservationsActivity started.");

        try {
            setContentView(R.layout.activity_my_reservations);
            Log.d(TAG, "onCreate: Layout set successfully.");

            db = new DatabaseHelper(this);
            listView = findViewById(R.id.listMyReservations);
            reservationsList = new ArrayList<>();

            // Get current user email from intent
            currentUserEmail = getIntent().getStringExtra("USER_EMAIL");
            if (currentUserEmail == null || currentUserEmail.isEmpty()) {
                Toast.makeText(this, "User email not found. Please login again.", Toast.LENGTH_LONG).show();
                Log.e(TAG, "onCreate: User email not found in intent.");
                finish();
                return;
            } else {
                Log.d(TAG, "onCreate: Current user email: " + currentUserEmail);
            }

            // Setup toolbar
            topAppBar = findViewById(R.id.topAppBar);
            setSupportActionBar(topAppBar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("My Reservations");
                Log.d(TAG, "onCreate: Toolbar title set.");
            }
            topAppBar.setNavigationOnClickListener(v -> onBackPressed());

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
                        intent = new Intent(MyReservationsActivity.this, AccelerometerTestActivity.class);
                    } else if (id == R.id.nav_about) {
                        intent = new Intent(MyReservationsActivity.this, AboutUsActivity.class);
                    } else if (id == R.id.nav_settings) {
                        intent = new Intent(MyReservationsActivity.this, SettingsActivity.class);
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

            loadReservations(); // Load reservations when activity is created
            Log.d(TAG, "onCreate: loadReservations() called.");

            // Setup Bottom Navigation
            setupBottomNavigation();
            Log.d(TAG, "onCreate: setupBottomNavigation completed.");

        } catch (Exception e) {
            Log.e(TAG, "onCreate: Error during activity creation: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading reservations: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish(); // Finish to prevent app from hanging
        }
    }

    private void loadReservations() {
        Log.d(TAG, "loadReservations() called.");
        reservationsList.clear();

        int userId = db.getUserIdByEmail(currentUserEmail);
        if (userId == -1) {
            Toast.makeText(this, "User not found in database.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "loadReservations: User ID not found for email: " + currentUserEmail);
            return;
        }
        Log.d(TAG, "loadReservations: User ID: " + userId);

        Cursor cursor = null;
        try {
            cursor = db.getReservationsByUser(userId);

            if (cursor != null && cursor.moveToFirst()) {
                Log.d(TAG, "loadReservations: Found " + cursor.getCount() + " reservations.");
                do {
                    String spot = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SPOT_ID));
                    String time = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TIMESTAMP));
                    reservationsList.add("📍 Spot: " + spot + "\n🕒 Time: " + time);
                    Log.d(TAG, "loadReservations: Added reservation - Spot: " + spot + ", Time: " + time);
                } while (cursor.moveToNext());
            } else {
                Toast.makeText(this, "No reservations found.", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "loadReservations: No reservations found for user.");
            }
        } catch (Exception e) {
            Log.e(TAG, "loadReservations: Error retrieving reservations: " + e.getMessage(), e);
            Toast.makeText(this, "Error loading reservations: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            if (cursor != null) {
                cursor.close();
                Log.d(TAG, "loadReservations: Cursor closed.");
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, reservationsList);
        listView.setAdapter(adapter);
        Log.d(TAG, "loadReservations: Adapter set.");
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        if (bottomNav == null) {
            Log.e(TAG, "setupBottomNavigation: bottom_navigation view not found!");
            return;
        }
        Log.d(TAG, "setupBottomNavigation: Bottom navigation view found.");

        bottomNav.setSelectedItemId(R.id.action_reservations); // Highlight Reservations
        Log.d(TAG, "setupBottomNavigation: Default selected item set to Reservations.");

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Intent intent = null;
            Log.d(TAG, "onBottomNavigationItemSelected: Item selected with ID: " + getResources().getResourceEntryName(id));

            if (id == R.id.action_home) {
                intent = new Intent(this, HomeActivity.class);
            } else if (id == R.id.action_profile) {
                intent = new Intent(this, UserProfileActivity.class);
            } else if (id == R.id.action_reservations) {
                // Already on My Reservations, can stay or refresh
                Log.d(TAG, "onBottomNavigationItemSelected: Already on My Reservations.");
                return true;
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
