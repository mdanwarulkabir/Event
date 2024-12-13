package com.mdanwarul.event;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignupActivity extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText;
    private Button signupButton;
    private TextView loginLink;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        usernameEditText = findViewById(R.id.username498);
        passwordEditText = findViewById(R.id.password498);
        signupButton = findViewById(R.id.signup_button498);
        loginLink = findViewById(R.id.login_link498);

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        // Navigate to login page
        loginLink.setOnClickListener(v -> {
            Intent intent = new Intent(SignupActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        // Handle signup button click
        signupButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            // Validate input
            if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
                Toast.makeText(SignupActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.length() < 6) {
                Toast.makeText(SignupActivity.this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create user with Firebase Authentication
            firebaseAuth.createUserWithEmailAndPassword(username, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Signup success
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            Toast.makeText(SignupActivity.this, "Signup successful! Welcome " + user.getEmail(), Toast.LENGTH_SHORT).show();

                            // Navigate to login page after successful signup
                            Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // Signup failed
                            Toast.makeText(SignupActivity.this, "Signup failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}
