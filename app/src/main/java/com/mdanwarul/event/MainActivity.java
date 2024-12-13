package com.mdanwarul.event;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView signupLink;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadUsernameFromPreferences();
        emailEditText = findViewById(R.id.username498); // Username field for email
        passwordEditText = findViewById(R.id.password498);
        loginButton = findViewById(R.id.login_button498);
        signupLink = findViewById(R.id.signup_link498);

        // Initialize Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        // Navigate to Signup page
        signupLink.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SignupActivity.class);
            startActivity(intent);
            finish();  // Close MainActivity
        });

        // Handle login button click
        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            // Validate input
            if (!isValidEmail(email)) {
                Toast.makeText(MainActivity.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please enter a password", Toast.LENGTH_SHORT).show();
                return;
            }

            // Authenticate using Firebase
            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Successful login
                            Toast.makeText(MainActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(MainActivity.this, ContentActivity.class);
                            startActivity(intent);
                            finish(); // Finish the current activity to prevent going back to it
                            saveUsernameToPreferences(email);
                        } else {
                            // Failed login
                            Toast.makeText(MainActivity.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private void loadUsernameFromPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "");
        if (!username.isEmpty()) {
            EditText usernameField = findViewById(R.id.username498); // Replace with the actual ID of the username field
            usernameField.setText(username); // Prefill the username
        }
    }

    private void saveUsernameToPreferences(String username) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username", username);
        editor.apply();
    }

    /**
     * Validate email address using Patterns utility.
     *
     * @param email The email address to validate.
     * @return True if the email is valid, false otherwise.
     */
    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
