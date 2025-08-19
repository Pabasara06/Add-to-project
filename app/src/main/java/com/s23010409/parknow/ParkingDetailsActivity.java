package com.s23010409.parknow;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.maps.model.LatLng; // Import LatLng
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParkingDetailsActivity extends AppCompatActivity {

    private static final String TAG = "ParkingDetailsActivity";
    private TextView locationView, priceView, availabilityView;
    private Button reserveButton, navigateToSpotButton; // Added navigateToSpotButton
    private ToggleButton favoriteButton;
    private String spotName;
    private String currentUserEmail;
    private double pricePerHour = 0.0; // To store extracted price
    private LatLng parkingSpotLatLng; // To store the LatLng for navigation
    private DatabaseHelper db;

    // Drawer and Toolbar elements
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MaterialToolbar topAppBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parking_details);
        Log.d(TAG, "onCreate: ParkingDetailsActivity started.");

        db = new DatabaseHelper(this);

        // Get user email from intent
        currentUserEmail = getIntent().getStringExtra("USER_EMAIL");
        if (currentUserEmail == null || currentUserEmail.isEmpty()) {
            Toast.makeText(this, "User email not found. Please login again.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "onCreate: User email not found in intent. Finishing activity.");
            finish();
            return;
        }
        Log.d(TAG, "Current user email: " + currentUserEmail);

        // Setup TopAppBar Toolbar
        topAppBar = findViewById(R.id.topAppBar);
        setSupportActionBar(topAppBar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Parking Spot Details");
            Log.d(TAG, "onCreate: Toolbar title set.");
        }
        topAppBar.setNavigationOnClickListener(v -> {
            Log.d(TAG, "Toolbar back button clicked. Finishing activity.");
            onBackPressed();
        });

        // View Initialization
        locationView = findViewById(R.id.textLocation);
        priceView = findViewById(R.id.textPrice);
        availabilityView = findViewById(R.id.textAvailability);
        reserveButton = findViewById(R.id.buttonReserve);
        favoriteButton = findViewById(R.id.buttonFavorite);

        // Get spot name, snippet, and LatLng from intent
        spotName = getIntent().getStringExtra("spot_name");
        String spotSnippet = getIntent().getStringExtra("spot_snippet");
        double lat = getIntent().getDoubleExtra("spot_lat", 0.0);
        double lng = getIntent().getDoubleExtra("spot_lng", 0.0);
        parkingSpotLatLng = new LatLng(lat, lng);

        if (spotName != null) {
            locationView.setText(spotName);
            Log.d(TAG, "Spot Name: " + spotName);
        } else {
            locationView.setText("Location: N/A");
            Log.e(TAG, "Spot Name not found in intent.");
        }
        Log.d(TAG, "Parking Spot LatLng: " + parkingSpotLatLng.latitude + ", " + parkingSpotLatLng.longitude);


        // Parse price and availability from snippet
        if (spotSnippet != null) {
            // Extract Price (Corrected regex for LKR)
            Pattern pricePattern = Pattern.compile("Price: (\\d+\\.?\\d*)\\s*LKR/hr");
            Matcher priceMatcher = pricePattern.matcher(spotSnippet);
            if (priceMatcher.find()) {
                try {
                    pricePerHour = Double.parseDouble(priceMatcher.group(1));
                    priceView.setText(String.format(Locale.getDefault(), "Price: LKR %.2f/hour", pricePerHour));
                    Log.d(TAG, "Price per hour extracted: " + pricePerHour);
                } catch (NumberFormatException e) {
                    priceView.setText("Price: N/A");
                    Log.e(TAG, "Error parsing price from snippet: " + e.getMessage(), e);
                }
            } else {
                priceView.setText("Price: N/A");
                Log.w(TAG, "Price not found in snippet.");
            }

            // Extract Availability
            Pattern availabilityPattern = Pattern.compile("Available: (\\d+)");
            Matcher availabilityMatcher = availabilityPattern.matcher(spotSnippet); // Corrected: Matcher from Pattern
            if (availabilityMatcher.find()) {
                availabilityView.setText("Availability: " + availabilityMatcher.group(1) + " spots");
                Log.d(TAG, "Availability extracted: " + availabilityMatcher.group(1));
            } else {
                availabilityView.setText("Availability: N/A");
                Log.w(TAG, "Availability not found in snippet.");
            }
        } else {
            priceView.setText("Price: N/A");
            availabilityView.setText("Availability: N/A");
            Log.e(TAG, "Spot Snippet not found in intent.");
        }

        // Initialize UI elements for drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // Check if drawerLayout or navigationView are null
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
                    intent = new Intent(ParkingDetailsActivity.this, AccelerometerTestActivity.class);
                } else if (id == R.id.nav_about) {
                    intent = new Intent(ParkingDetailsActivity.this, AboutUsActivity.class);
                } else if (id == R.id.nav_settings) {
                    intent = new Intent(ParkingDetailsActivity.this, SettingsActivity.class);
                }
                // Add more cases for other drawer menu items here

                if (intent != null) {
                    intent.putExtra("USER_EMAIL", currentUserEmail);
                    startActivity(intent);
                    drawerLayout.closeDrawers(); // Close the drawer after selection
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

        // Handle Favorite Button state and click
        int userIdInt = db.getUserIdByEmail(currentUserEmail);
        if (userIdInt != -1) {
            boolean isFavorite = db.isSpotFavorite(userIdInt, spotName);
            favoriteButton.setChecked(isFavorite);
            Log.d(TAG, "Spot '" + spotName + "' is favorite: " + isFavorite);
        } else {
            Log.e(TAG, "User ID not found for email: " + currentUserEmail);
            Toast.makeText(this, "Error: User data missing for favorites.", Toast.LENGTH_SHORT).show();
            favoriteButton.setEnabled(false); // Disable favorite if user ID isn't found
        }

        favoriteButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int currentUserId = db.getUserIdByEmail(currentUserEmail); // Re-fetch userId in case it was somehow lost
            if (currentUserId != -1) {
                if (isChecked) {
                    boolean success = db.insertFavoriteSpot(currentUserId, spotName);
                    if (success) {
                        Toast.makeText(ParkingDetailsActivity.this, spotName + " added to favorites!", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Added '" + spotName + "' to favorites for user " + currentUserId);
                    } else {
                        Toast.makeText(ParkingDetailsActivity.this, "Failed to add " + spotName + " to favorites.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Failed to add '" + spotName + "' to favorites for user " + currentUserId);
                    }
                } else {
                    boolean success = db.deleteFavoriteSpot(currentUserId, spotName);
                    if (success) {
                        Toast.makeText(ParkingDetailsActivity.this, spotName + " removed from favorites.", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Removed '" + spotName + "' from favorites for user " + currentUserId);
                    } else {
                        Toast.makeText(ParkingDetailsActivity.this, "Failed to remove " + spotName + " from favorites.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Failed to remove '" + spotName + "' from favorites for user " + currentUserId);
                    }
                }
            } else {
                Toast.makeText(ParkingDetailsActivity.this, "Error: User not found for favorite action.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error: User ID not found for email " + currentUserEmail + " during favorite action.");
            }
        });

        // Reserve Button click listener
        reserveButton.setOnClickListener(v -> {
            Log.d(TAG, "Reserve button clicked.");
            // Get current timestamp for reservation
            String currentTimestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

            Intent reserveIntent = new Intent(ParkingDetailsActivity.this, ReserveSpotActivity.class);
            reserveIntent.putExtra("USER_EMAIL", currentUserEmail);
            reserveIntent.putExtra("spot_name", spotName);
            reserveIntent.putExtra("TIMESTAMP", currentTimestamp);
            reserveIntent.putExtra("price_per_hour", pricePerHour); // Pass the extracted price
            reserveIntent.putExtra("spot_lat", parkingSpotLatLng.latitude); // Pass LatLng for ReserveSpot if needed later
            reserveIntent.putExtra("spot_lng", parkingSpotLatLng.longitude);
            startActivity(reserveIntent);
            Log.d(TAG, "Launched ReserveSpotActivity with price_per_hour: " + pricePerHour);
        });

        // Navigate To Spot Button click listener
        navigateToSpotButton.setOnClickListener(v -> {
            Log.d(TAG, "Navigate to Spot button clicked.");
            if (parkingSpotLatLng.latitude != 0.0 || parkingSpotLatLng.longitude != 0.0) {
                // Launch Google Maps navigation
                String uri = "google.navigation:q=" + parkingSpotLatLng.latitude + "," + parkingSpotLatLng.longitude + "&mode=d"; // mode=d for driving
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                mapIntent.setPackage("com.google.android.apps.maps"); // Ensure it opens in Google Maps

                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                    Log.d(TAG, "Launched Google Maps for navigation to " + parkingSpotLatLng.latitude + "," + parkingSpotLatLng.longitude);
                } else {
                    Toast.makeText(this, "Google Maps app not found.", Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "Google Maps app not found for navigation intent.");
                }
            } else {
                Toast.makeText(this, "Parking spot location not available for navigation.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Parking spot LatLng is 0,0. Cannot navigate.");
            }
        });


        // Setup Bottom Navigation
        setupBottomNavigation();
        Log.d(TAG, "onCreate: setupBottomNavigation completed.");
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        if (bottomNav == null) {
            Log.e(TAG, "setupBottomNavigation: bottom_navigation view not found!");
            return;
        }
        Log.d(TAG, "setupBottomNavigation: Bottom navigation view found.");


        // Highlight the appropriate item based on the current activity
        // ParkingDetailsActivity is not a primary bottom nav screen, so set to Home for consistency
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
                overridePendingTransition(0, 0); // Disable activity transition animation
                finish(); // Finish current activity to prevent deep back stack
                Log.d(TAG, "onBottomNavigationItemSelected: Navigated to " + intent.getComponent().getClassName());
                return true;
            }
            Log.w(TAG, "onBottomNavigationItemSelected: No intent created for selected item.");
            return false;
        });
        Log.d(TAG, "setupBottomNavigation: Listener set for bottom navigation.");
    }

    // Handle back arrow in toolbar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Log.d(TAG, "onOptionsItemSelected: Up button clicked. Calling onBackPressed.");
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        // You can decide if back button should close drawer or just finish activity.
        // For sub-activities like this, usually just finish.
        // Check if drawer is open before calling super.onBackPressed()
        if (drawerLayout != null && navigationView != null && drawerLayout.isDrawerOpen(navigationView)) {
            drawerLayout.closeDrawers();
            Log.d(TAG, "onBackPressed: Drawer was open, closing it.");
        } else {
            super.onBackPressed();
            Log.d(TAG, "onBackPressed: Drawer was closed, performing super onBackPressed.");
        }
    }
}
