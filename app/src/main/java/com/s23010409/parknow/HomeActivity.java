package com.s23010409.parknow;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log; // Import Log for debugging
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

public class HomeActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "HomeActivity";
    private GoogleMap mMap;
    private static final int LOCATION_PERMISSION_REQUEST = 100;
    private LatLng selectedDestination; // Used for navigating to a selected parking spot
    private FloatingActionButton fabNavigate;
    private FusedLocationProviderClient fusedLocationClient;

    private String currentUserEmail;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MaterialToolbar topAppBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Log.d(TAG, "onCreate: HomeActivity started.");

        // Get current user email from intent
        currentUserEmail = getIntent().getStringExtra("USER_EMAIL");
        if (currentUserEmail == null || currentUserEmail.isEmpty()) {
            Toast.makeText(this, "User email not found. Please login again.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "onCreate: User email not found in intent. Finishing activity.");
            finish();
            return;
        } else {
            Log.d(TAG, "onCreate: Current user email: " + currentUserEmail);
        }

        // Initialize UI elements
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        topAppBar = findViewById(R.id.topAppBar);
        fabNavigate = findViewById(R.id.fabNavigate);


        // Check if drawerLayout or navigationView are null (layout inflation issue)
        if (drawerLayout == null) Log.e(TAG, "findViewById failed for drawer_layout");
        if (navigationView == null) Log.e(TAG, "findViewById failed for nav_view");
        if (topAppBar == null) Log.e(TAG, "findViewById failed for topAppBar");
        if (fabNavigate == null) Log.e(TAG, "findViewById failed for fabNavigate");


        // Set up the top app bar as the ActionBar
        setSupportActionBar(topAppBar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("ParkNow"); // Set the title for the Home screen
            Log.d(TAG, "onCreate: Toolbar title set to ParkNow.");
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
                    intent = new Intent(HomeActivity.this, AccelerometerTestActivity.class);
                } else if (id == R.id.nav_about) { // Now in drawer
                    intent = new Intent(HomeActivity.this, AboutUsActivity.class);
                } else if (id == R.id.nav_settings) {
                    intent = new Intent(HomeActivity.this, SettingsActivity.class);
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


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
            Log.d(TAG, "onCreate: Map fragment obtained and getMapAsync called.");
        } else {
            Toast.makeText(this, "Map fragment not found.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "onCreate: Map fragment not found by ID R.id.map.");
        }

        // Initialize FusedLocationProviderClient for GPS
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        Log.d(TAG, "onCreate: FusedLocationProviderClient initialized.");


        // Setup Floating Action Button for Navigation to current location
        fabNavigate.setOnClickListener(view -> {
            Log.d(TAG, "FAB (Navigate) clicked.");
            navigateToCurrentLocation(); // Call method to get current location and navigate
        });

        setupBottomNavigation();
        Log.d(TAG, "onCreate: setupBottomNavigation completed.");
    }

    private void navigateToCurrentLocation() {
        Log.d(TAG, "navigateToCurrentLocation() called.");
        // Check for location permission at runtime
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "navigateToCurrentLocation: Location permission not granted. Requesting...");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        } else {
            Log.d(TAG, "navigateToCurrentLocation: Location permission granted. Getting last location...");
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, location -> {
                        if (location != null) {
                            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            String uri = "google.navigation:q=" + currentLocation.latitude + "," + currentLocation.longitude + "&mode=d";
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                            intent.setPackage("com.google.android.apps.maps"); // Specify Google Maps package

                            if (intent.resolveActivity(getPackageManager()) != null) {
                                startActivity(intent);
                                Log.d(TAG, "navigateToCurrentLocation: Launched Google Maps for navigation.");
                            } else {
                                Toast.makeText(HomeActivity.this, "Google Maps is not installed.", Toast.LENGTH_SHORT).show();
                                Log.w(TAG, "navigateToCurrentLocation: Google Maps app not found.");
                            }
                        } else {
                            Toast.makeText(HomeActivity.this, "Unable to get current location. Please try again.", Toast.LENGTH_SHORT).show();
                            Log.w(TAG, "navigateToCurrentLocation: Last location is null.");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(HomeActivity.this, "Error getting location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "navigateToCurrentLocation: Error getting location: " + e.getMessage(), e);
                    });
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        Log.d(TAG, "onMapReady: Google Map is ready.");

        // Request location permission if not granted, then enable My Location layer
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            try {
                mMap.setMyLocationEnabled(true);
                Log.d(TAG, "onMapReady: My location layer enabled.");
            } catch (SecurityException e) {
                Log.e(TAG, "onMapReady: SecurityException enabling my location: " + e.getMessage());
            }
        } else {
            Log.d(TAG, "onMapReady: Location permission not granted. My location layer not enabled.");
        }

        // Focus camera on a central point in Sri Lanka
        LatLng sriLankaCenter = new LatLng(7.8731, 80.7718); // Center of Sri Lanka
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sriLankaCenter, 8)); // Zoom level 8 shows most of the island
        Log.d(TAG, "onMapReady: Camera moved to Sri Lanka center.");


        // Add sample parking spots in Sri Lanka
        addParkingSpot(mMap, new LatLng(6.9271, 79.8612), "Colombo Fort Parking", "Available: 15, Price: 150 LKR/hr");
        addParkingSpot(mMap, new LatLng(6.8967, 79.8660), "Galle Face Green Parking", "Available: 20, Price: 100 LKR/hr");
        addParkingSpot(mMap, new LatLng(6.8650, 79.8997), "Nugegoda Urban Parking", "Available: 10, Price: 120 LKR/hr");
        addParkingSpot(mMap, new LatLng(7.2906, 80.6337), "Kandy City Center Parking", "Available: 8, Price: 180 LKR/hr");
        addParkingSpot(mMap, new LatLng(6.0535, 80.2210), "Galle Fort Parking", "Available: 12, Price: 130 LKR/hr");
        addParkingSpot(mMap, new LatLng(7.9000, 79.8900), "Negombo Beach Parking", "Available: 25, Price: 90 LKR/hr");
        addParkingSpot(mMap, new LatLng(8.5878, 81.2152), "Trincomalee Dock Parking", "Available: 7, Price: 110 LKR/hr");
        addParkingSpot(mMap, new LatLng(9.6615, 80.0255), "Jaffna City Parking", "Available: 18, Price: 140 LKR/hr");
        Log.d(TAG, "onMapReady: Added sample parking markers.");


        // Set up info window click listener
        mMap.setOnInfoWindowClickListener(marker -> {
            Log.d(TAG, "onInfoWindowClick: Marker clicked: " + marker.getTitle());
            // When a parking spot marker's info window is clicked, open ParkingDetailsActivity
            Intent intent = new Intent(HomeActivity.this, ParkingDetailsActivity.class);
            intent.putExtra("spot_name", marker.getTitle());
            intent.putExtra("spot_snippet", marker.getSnippet()); // Pass the snippet (price, availability)
            intent.putExtra("USER_EMAIL", currentUserEmail); // Pass user email
            intent.putExtra("spot_lat", marker.getPosition().latitude); // Pass latitude
            intent.putExtra("spot_lng", marker.getPosition().longitude); // Pass longitude
            startActivity(intent);
            Log.d(TAG, "onInfoWindowClick: Launched ParkingDetailsActivity.");
        });
    }

    private void addParkingSpot(GoogleMap map, LatLng latLng, String title, String snippet) {
        map.addMarker(new MarkerOptions()
                .position(latLng)
                .title(title)
                .snippet(snippet));
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        if (bottomNav == null) {
            Log.e(TAG, "setupBottomNavigation: bottom_navigation view not found!");
            return;
        }
        Log.d(TAG, "setupBottomNavigation: Bottom navigation view found.");

        bottomNav.setSelectedItemId(R.id.action_home); // Highlight Home
        Log.d(TAG, "setupBottomNavigation: Default selected item set to Home.");


        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Intent intent = null;
            Log.d(TAG, "onBottomNavigationItemSelected: Item selected with ID: " + getResources().getResourceEntryName(id));


            if (id == R.id.action_home) {
                // Already on Home, can stay or refresh
                Log.d(TAG, "onBottomNavigationItemSelected: Already on Home.");
                return true;
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
                // Disable activity transition animation and clear back stack for bottom nav items
                overridePendingTransition(0, 0);
                finish(); // Finish current activity to prevent deep back stack
                Log.d(TAG, "onBottomNavigationItemSelected: Navigated to " + intent.getComponent().getClassName());
                return true;
            }
            Log.w(TAG, "onBottomNavigationItemSelected: No intent created for selected item.");
            return false;
        });
        Log.d(TAG, "setupBottomNavigation: Listener set for bottom navigation.");
    }

    // Handle location permission result
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionsResult: RequestCode=" + requestCode);

        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "onRequestPermissionsResult: Location permission GRANTED.");
                // Permission granted, try to get location and navigate again
                if (mMap != null) {
                    try {
                        mMap.setMyLocationEnabled(true); // Enable blue dot on map
                        Log.d(TAG, "onRequestPermissionsResult: My location layer enabled after permission.");
                        navigateToCurrentLocation(); // Try navigating now that permission is granted
                    } catch (SecurityException ignored) {
                        Log.e(TAG, "onRequestPermissionsResult: SecurityException when enabling my location.", ignored);
                    }
                }
            } else {
                Toast.makeText(this, "Location permission is required for navigation to current location.", Toast.LENGTH_SHORT).show();
                Log.w(TAG, "onRequestPermissionsResult: Location permission DENIED.");
            }
        }
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
