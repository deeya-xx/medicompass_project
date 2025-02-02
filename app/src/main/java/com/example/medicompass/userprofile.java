package com.example.medicompass;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.medicompass.HomepageActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

public class userprofile extends AppCompatActivity {

    private EditText etName, etEmail, etPhone, etBirthday, etLatitude, etLongitude;
    private Button btnSubmit;
    private FirebaseAuth auth;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private ImageView imageView;
    private TextView name, mail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.userprofile);

        // Initialize UI components
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etBirthday = findViewById(R.id.etBirthday);
        etLatitude = findViewById(R.id.etLatitude);
        etLongitude = findViewById(R.id.etLongitude);
        btnSubmit = findViewById(R.id.btnSubmit);
        imageView = findViewById(R.id.profileImage);
        name = findViewById(R.id.nameTV);
        mail = findViewById(R.id.mailTV);

        // Initialize Firebase Authentication and Database
        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference("UserLocation");

        // Load user details
        loadUserProfile();

        // Make the email field read-only
        etEmail.setFocusable(false);

        // Set up Toolbar and enable back button
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("User Profile");
        }

        // Set up birthday field to show date picker dialog
        etBirthday.setOnClickListener(v -> showDatePicker());

        // Handle submit button click
        btnSubmit.setOnClickListener(v -> submitProfile());

        // Set up Google Sign-In options
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.client_id))
                .requestEmail()
                .build();
        GoogleSignIn.getClient(userprofile.this, options);

        // Check if user is signed in and load their profile
        if (auth.getCurrentUser() != null) {
            Glide.with(userprofile.this).load(Objects.requireNonNull(auth.getCurrentUser()).getPhotoUrl()).into(imageView);
            name.setText(auth.getCurrentUser().getDisplayName());
            mail.setText(auth.getCurrentUser().getEmail());
        }
    }

    private void loadUserProfile() {
        if (firebaseUser != null) {
            // Set basic user info
            etEmail.setText(firebaseUser.getEmail());
            etName.setText(firebaseUser.getDisplayName());

            // Retrieve additional details from Firebase Database
            databaseReference.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // Check if name exists and display it
                        if (snapshot.child("name").exists()) {
                            etName.setText(snapshot.child("name").getValue(String.class));
                        }

                        // Check if phone number exists and display it
                        if (snapshot.child("phone").exists()) {
                            etPhone.setText(snapshot.child("phone").getValue(String.class));
                        }

                        // Check if birthday exists and display it
                        if (snapshot.child("birthday").exists()) {
                            etBirthday.setText(snapshot.child("birthday").getValue(String.class));
                        }

                        // Retrieve latitude and longitude from Firebase
                        if (snapshot.child("latitude").exists()) {
                            Double latitude = snapshot.child("latitude").getValue(Double.class);
                            if (latitude != null) {
                                etLatitude.setText(String.valueOf(latitude));
                            } else {
                                etLatitude.setText("Not Available"); // Optional: Handle empty values
                            }
                        } else {
                            etLatitude.setText("Not Available");
                        }

                        if (snapshot.child("longitude").exists()) {
                            Double longitude = snapshot.child("longitude").getValue(Double.class);
                            if (longitude != null) {
                                etLongitude.setText(String.valueOf(longitude));
                            } else {
                                etLongitude.setText("Not Available"); // Optional: Handle empty values
                            }
                        } else {
                            etLongitude.setText("Not Available");
                        }
                    } else {
                        // Handle the case where user data doesn't exist
                        Toast.makeText(userprofile.this, "No user data found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(userprofile.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Handle the case where Firebase user is null (shouldn't happen)
            Toast.makeText(userprofile.this, "User not authenticated", Toast.LENGTH_SHORT).show();
        }
    }


    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    etBirthday.setText(selectedDate);
                },
                year, month, day
        );

        datePickerDialog.show();
    }

    private void submitProfile() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String birthday = etBirthday.getText().toString().trim();

        if (name.isEmpty() || phone.isEmpty() || birthday.isEmpty()) {
            Toast.makeText(userprofile.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (firebaseUser != null) {
            String userId = firebaseUser.getUid();

            HashMap<String, Object> userMap = new HashMap<>();
            userMap.put("name", name);
            userMap.put("email", email); // Updated email
            userMap.put("phone", phone);
            userMap.put("birthday", birthday);

            // Save data to Firebase Database
            databaseReference.child(userId).updateChildren(userMap)
                    .addOnSuccessListener(unused ->
                            Toast.makeText(userprofile.this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    )
                    .addOnFailureListener(e ->
                            Toast.makeText(userprofile.this, "Failed to update profile", Toast.LENGTH_SHORT).show()
                    );
        }
    }

    // Handle back button click to go to HomepageActivity
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(userprofile.this, HomepageActivity.class);
            startActivity(intent);
            finish(); // Close current activity
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
