package com.s23010409.parknow;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem; // Import MenuItem for drawer
import android.view.View; // Import View for nav header
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView; // Import TextView for nav header
import android.widget.Toast;

import androidx.annotation.NonNull; // Import for @NonNull
import androidx.appcompat.app.ActionBarDrawerToggle; // Import for drawer toggle
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout; // Import for DrawerLayout

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView; // Import for NavigationView

import java.util.ArrayList;

public class FavoriteSpotsActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayList<String> favoriteSpots;
    private DatabaseHelper db;
    private String currentUserEmail;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MaterialToolbar topAppBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_spots); // Ensure this layout uses DrawerLayout

        db = new DatabaseHelper(this);
        listView = findViewById(R.id.listFavoriteSpots);
        favoriteSpots = new ArrayList<>();

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
            getSupportActionBar().setTitle("Favorite Spots");
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
                    intent = new Intent(FavoriteSpotsActivity.this, AccelerometerTestActivity.class);
                } else if (id == R.id.nav_about) {
                    intent = new Intent(FavoriteSpotsActivity.this, AboutUsActivity.class);
                } else if (id == R.id.nav_settings) {
                    intent = new Intent(FavoriteSpotsActivity.this, SettingsActivity.class);
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

        loadFavoriteSpots();
        setupBottomNavigation();
    }

    private void loadFavoriteSpots() {
        favoriteSpots.clear();

        int userId = db.getUserIdByEmail(currentUserEmail);
        if (userId == -1) {
            Toast.makeText(this, "User not found in database.", Toast.LENGTH_SHORT).show();
            return;
        }

        Cursor cursor = db.getFavoriteSpotsByUser(userId);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String spot = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_SPOT_ID));
                favoriteSpots.add("⭐ " + spot);
            } while (cursor.moveToNext());
            cursor.close();
        } else {
            Toast.makeText(this, "No favorite spots found.", Toast.LENGTH_SHORT).show();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, favoriteSpots);
        listView.setAdapter(adapter);
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        if (bottomNav == null) return;

        bottomNav.setSelectedItemId(R.id.action_favorites); // Highlight Favorites

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
                // Already on Favorite Spots, can stay or refresh
                return true;
            } else if (id == R.id.action_feedback) {
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