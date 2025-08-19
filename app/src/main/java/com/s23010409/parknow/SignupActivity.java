package com.s23010409.parknow;

import android.content.Intent;
import android.database.Cursor; // <--- Added this import
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

public class SignupActivity extends AppCompatActivity {

    private EditText signupName, signupEmail, signupPassword, signupConfirmPassword;
    private Button buttonSignup;
    private TextView textLogin;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        db = new DatabaseHelper(this);

        // Setup toolbar
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Sign Up");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        signupName = findViewById(R.id.signupName);
        signupEmail = findViewById(R.id.signupEmail);
        signupPassword = findViewById(R.id.signupPassword);
        signupConfirmPassword = findViewById(R.id.signupConfirmPassword);
        buttonSignup = findViewById(R.id.buttonSignup);
        textLogin = findViewById(R.id.textLogin);

        buttonSignup.setOnClickListener(v -> {
            String name = signupName.getText().toString().trim();
            String email = signupEmail.getText().toString().trim();
            String password = signupPassword.getText().toString().trim();
            String confirmPassword = signupConfirmPassword.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if email already exists
            Cursor cursor = null;
            try {
                cursor = db.getUserByEmail(email);
                if (cursor != null && cursor.getCount() > 0) {
                    Toast.makeText(this, "Email already registered", Toast.LENGTH_SHORT).show();
                    return;
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }


            boolean isInserted = db.insertUser(name, email, password);
            if (isInserted) {
                Toast.makeText(this, "Registration Successful", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Registration Failed", Toast.LENGTH_SHORT).show();
            }
        });

        textLogin.setOnClickListener(v -> {
            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
