package com.s23010409.parknow;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter; // Import for InputFilter
import android.text.TextWatcher;
import android.util.Log;
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
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PaymentActivity extends AppCompatActivity {

    private static final String TAG = "PaymentActivity";

    private TextView textPaymentSpotName, textPaymentTotalCost;
    private TextInputLayout textInputCardNumber, textInputCardHolderName, textInputExpiryDate, textInputCvv;
    private TextInputEditText editCardNumber, editCardHolderName, editExpiryDate, editCvv;
    private Button buttonPayNow;

    private DatabaseHelper db;
    private String userEmail;
    private String spotName;
    private String timestamp;
    private int durationHours;
    private double totalCost;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MaterialToolbar topAppBar;

    private boolean isUpdatingExpiryDate = false; // Flag to prevent infinite loop for expiry date formatting

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        db = new DatabaseHelper(this);

        // Get data from intent
        userEmail = getIntent().getStringExtra("USER_EMAIL");
        spotName = getIntent().getStringExtra("spot_name");
        timestamp = getIntent().getStringExtra("TIMESTAMP");
        durationHours = getIntent().getIntExtra("DURATION_HOURS", 1);
        totalCost = getIntent().getDoubleExtra("TOTAL_COST", 0.0);

        if (userEmail == null || userEmail.isEmpty() || spotName == null || timestamp == null || totalCost <= 0) {
            Toast.makeText(this, "Payment data incomplete or invalid.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Initialize UI elements
        textPaymentSpotName = findViewById(R.id.textPaymentSpotName);
        textPaymentTotalCost = findViewById(R.id.textPaymentTotalCost);
        textInputCardNumber = findViewById(R.id.textInputCardNumber);
        textInputCardHolderName = findViewById(R.id.textInputCardHolderName);
        textInputExpiryDate = findViewById(R.id.textInputExpiryDate);
        textInputCvv = findViewById(R.id.textInputCvv);
        editCardNumber = findViewById(R.id.editCardNumber);
        editCardHolderName = findViewById(R.id.editCardHolderName);
        editExpiryDate = findViewById(R.id.editExpiryDate);
        editCvv = findViewById(R.id.editCvv);
        buttonPayNow = findViewById(R.id.buttonPayNow);

        // Display received data
        textPaymentSpotName.setText("For: " + spotName + " (" + durationHours + " Hours)");
        textPaymentTotalCost.setText(String.format(Locale.getDefault(), "Total: LKR %.2f", totalCost));

        // Setup TopAppBar Toolbar
        topAppBar = findViewById(R.id.topAppBar);
        setSupportActionBar(topAppBar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Payment");
        }
        topAppBar.setNavigationOnClickListener(v -> onBackPressed());

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

                if (id == R.id.nav_accelerometer) {
                    intent = new Intent(PaymentActivity.this, AccelerometerTestActivity.class);
                } else if (id == R.id.nav_about) {
                    intent = new Intent(PaymentActivity.this, AboutUsActivity.class);
                } else if (id == R.id.nav_settings) {
                    intent = new Intent(PaymentActivity.this, SettingsActivity.class);
                }

                if (intent != null) {
                    intent.putExtra("USER_EMAIL", userEmail);
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
            navHeaderEmail.setText(userEmail);
        }

        // Apply input filters
        editCardNumber.setFilters(new InputFilter[] { new InputFilter.LengthFilter(16) });
        editCvv.setFilters(new InputFilter[] { new InputFilter.LengthFilter(3) });
        // MaxLength for Expiry Date is 5 (MM/YY)
        editExpiryDate.setFilters(new InputFilter[] { new InputFilter.LengthFilter(5) });


        // Add TextWatchers for real-time validation and formatting
        editCardNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textInputCardNumber.setError(null); // Clear error on text change
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        editCardHolderName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textInputCardHolderName.setError(null); // Clear error on text change
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        editExpiryDate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not used
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Not used for primary formatting, handled in afterTextChanged for more control
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (isUpdatingExpiryDate) {
                    return; // Prevent infinite loop
                }

                isUpdatingExpiryDate = true;
                String currentText = s.toString();
                String cleanedText = currentText.replaceAll("[^0-9]", ""); // Remove all non-digits

                if (cleanedText.length() >= 2 && !currentText.contains("/")) {
                    // Insert "/" after the second digit if not already present
                    String formattedText = cleanedText.substring(0, 2) + "/" + cleanedText.substring(2);
                    // Cap at MM/YY (5 characters)
                    if (formattedText.length() > 5) {
                        formattedText = formattedText.substring(0, 5);
                    }
                    editExpiryDate.setText(formattedText);
                    editExpiryDate.setSelection(editExpiryDate.getText().length());
                } else if (currentText.length() < 2 && currentText.contains("/")) {
                    // Remove "/" if user deletes digits before it
                    editExpiryDate.setText(cleanedText);
                    editExpiryDate.setSelection(editExpiryDate.getText().length());
                }
                isUpdatingExpiryDate = false;
                textInputExpiryDate.setError(null); // Clear error on text change
            }
        });


        editCvv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textInputCvv.setError(null); // Clear error on text change
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });


        buttonPayNow.setOnClickListener(v -> {
            Log.d(TAG, "Pay Now button clicked. Starting validation...");
            if (validatePaymentDetails()) {
                Log.d(TAG, "Validation successful. Proceeding to onPaymentConfirmed().");
                onPaymentConfirmed();
            } else {
                Log.w(TAG, "Validation failed. Displaying toast to correct details.");
                Toast.makeText(this, "Please correct the payment details.", Toast.LENGTH_SHORT).show();
            }
        });

        setupBottomNavigation();
    }

    private boolean validatePaymentDetails() {
        boolean isValid = true;
        Log.d(TAG, "Starting validatePaymentDetails().");

        String cardNumber = editCardNumber.getText().toString().trim();
        if (cardNumber.length() != 16) {
            textInputCardNumber.setError("Card number must be 16 digits");
            isValid = false;
            Log.d(TAG, "Card number invalid: " + cardNumber);
        } else {
            textInputCardNumber.setError(null);
        }

        String cardHolderName = editCardHolderName.getText().toString().trim();
        if (cardHolderName.isEmpty()) {
            textInputCardHolderName.setError("Card holder name is required");
            isValid = false;
            Log.d(TAG, "Card holder name empty.");
        } else {
            textInputCardHolderName.setError(null);
        }

        String expiryDate = editExpiryDate.getText().toString().trim();
        // Regex now matches MM/YY format specifically
        Pattern expiryPattern = Pattern.compile("(0[1-9]|1[0-2])/([0-9]{2})");
        Matcher expiryMatcher = expiryPattern.matcher(expiryDate);

        if (!expiryMatcher.matches()) {
            textInputExpiryDate.setError("Invalid format (MM/YY)");
            isValid = false;
            Log.d(TAG, "Expiry date format invalid: " + expiryDate);
        } else {
            int expMonth = Integer.parseInt(expiryMatcher.group(1));
            int expYear = Integer.parseInt(expiryMatcher.group(2)) + 2000; // Assuming YY is 20XX

            Calendar currentCalendar = Calendar.getInstance();
            int currentMonth = currentCalendar.get(Calendar.MONTH) + 1; // Calendar.MONTH is 0-indexed
            int currentYear = currentCalendar.get(Calendar.YEAR);

            Log.d(TAG, "Current Date: " + currentMonth + "/" + currentYear);
            Log.d(TAG, "Expiry Date: " + expMonth + "/" + expYear);

            if (expYear < currentYear || (expYear == currentYear && expMonth < currentMonth)) {
                textInputExpiryDate.setError("Card has expired");
                isValid = false;
                Log.d(TAG, "Expiry date expired.");
            } else {
                textInputExpiryDate.setError(null);
            }
        }

        String cvv = editCvv.getText().toString().trim();
        if (cvv.length() != 3) {
            textInputCvv.setError("CVV must be 3 digits");
            isValid = false;
            Log.d(TAG, "CVV invalid: " + cvv);
        } else {
            textInputCvv.setError(null);
        }

        Log.d(TAG, "validatePaymentDetails() returning: " + isValid);
        return isValid;
    }

    private void onPaymentConfirmed() {
        Log.d(TAG, "onPaymentConfirmed() called.");
        // Simulate payment success
        boolean paymentSuccess = true; // In a real app, integrate with a payment gateway

        if (paymentSuccess) {
            Log.d(TAG, "Payment simulation: SUCCESS.");
            // Save reservation to database
            int userIdInt = db.getUserIdByEmail(userEmail); // Get actual userId from email
            if (userIdInt != -1) {
                Log.d(TAG, "User ID found: " + userIdInt + ". Attempting to insert reservation.");
                boolean reservationInserted = db.insertReservation(userIdInt, spotName, timestamp);
                if (reservationInserted) {
                    Toast.makeText(this, "Payment successful and reservation confirmed!", Toast.LENGTH_LONG).show();
                    Log.d(TAG, "Payment successful, reservation inserted and confirmed.");

                    // Navigate to MyReservationsActivity and clear back stack
                    Intent intent = new Intent(PaymentActivity.this, MyReservationsActivity.class);
                    intent.putExtra("USER_EMAIL", userEmail);
                    // Clear all previous activities and start MyReservationsActivity as new task root
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish(); // Finish PaymentActivity
                    Log.d(TAG, "Navigated to MyReservationsActivity and finished PaymentActivity.");
                } else {
                    Toast.makeText(this, "Payment successful, but failed to record reservation.", Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Payment successful, but failed to insert reservation into DB.");
                }
            } else {
                Toast.makeText(this, "Payment successful, but user not found for reservation.", Toast.LENGTH_LONG).show();
                Log.e(TAG, "Payment successful, but userId not found for email: " + userEmail);
            }
        } else {
            Toast.makeText(this, "Payment failed. Please try again.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Payment failed (simulated).");
        }
    }


    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        if (bottomNav == null) return;

        bottomNav.setSelectedItemId(R.id.action_home); // Default to Home for sub-activities

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
                intent.putExtra("USER_EMAIL", userEmail);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
                return true;
            }
            return false;
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
