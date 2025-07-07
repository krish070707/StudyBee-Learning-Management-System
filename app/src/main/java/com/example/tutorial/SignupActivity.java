package com.example.tutorial;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SignupActivity extends AppCompatActivity {

    EditText signupName, signupEmail, signupUsername, signupPassword, signupClass;
    TextView loginRedirectText;
    Button signupButton;
    FirebaseDatabase database;
    DatabaseReference reference;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        SharedPreferences sharedPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            String classValue = sharedPreferences.getString("class", "");
            Intent intent = new Intent(SignupActivity.this, MainActivity.class);
            intent.putExtra("ClassValue", classValue);
            startActivity(intent);
            finish();
            return;
        }

        // Initialize UI elements
        signupName = findViewById(R.id.signup_name);
        signupEmail = findViewById(R.id.signup_email);
        signupUsername = findViewById(R.id.signup_username);
        signupPassword = findViewById(R.id.signup_password);
        signupButton = findViewById(R.id.signup_button);
        loginRedirectText = findViewById(R.id.loginRedirectText);
        signupClass = findViewById(R.id.signup_Class);

        // Firebase reference
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("users");

        signupButton.setOnClickListener(view -> registerUser());

        loginRedirectText.setOnClickListener(view ->
                startActivity(new Intent(SignupActivity.this, LoginActivity.class)));
    }

    private void registerUser() {
        String name = signupName.getText().toString().trim();
        String email = signupEmail.getText().toString().trim();
        String username = signupUsername.getText().toString().trim();
        String password = signupPassword.getText().toString().trim();
        String classValue = signupClass.getText().toString().trim();

        // Input validation
        if (TextUtils.isEmpty(name)) {
            signupName.setError("Name is required");
            return;
        }
        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            signupEmail.setError("Enter a valid email");
            return;
        }
        if (TextUtils.isEmpty(username)) {
            signupUsername.setError("Username is required");
            return;
        }
        if (username.equalsIgnoreCase("users")) {
            signupUsername.setError("Invalid username");
            return;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            signupPassword.setError("Password must be at least 6 characters");
            return;
        }
        if (TextUtils.isEmpty(classValue)) {
            signupClass.setError("Class is required");
            return;
        }

        // Check if email exists
        reference.orderByChild("email").equalTo(email)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot emailSnapshot) {
                        if (emailSnapshot.exists()) {
                            signupEmail.setError("Email already in use");
                        } else {
                            // Check if username exists
                            reference.child(username)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot usernameSnapshot) {
                                            if (usernameSnapshot.exists()) {
                                                signupUsername.setError("Username already exists");
                                            } else {
                                                // Register new user
                                                HelperClass helperClass = new HelperClass(name, email, username, password, classValue);
                                                reference.child(username).setValue(helperClass)
                                                        .addOnSuccessListener(aVoid -> {
                                                            SharedPreferences.Editor editor = getSharedPreferences("loginPrefs", MODE_PRIVATE).edit();
                                                            editor.putBoolean("isLoggedIn", true);
                                                            editor.putString("username", username);
                                                            editor.putString("class", classValue);
                                                            editor.apply();

                                                            Toast.makeText(SignupActivity.this, "Signup successful!", Toast.LENGTH_SHORT).show();
                                                            Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                                                            intent.putExtra("ClassValue", classValue);
                                                            startActivity(intent);
                                                            finish();
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Toast.makeText(SignupActivity.this, "Signup failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                        });
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {
                                            Toast.makeText(SignupActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(SignupActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
